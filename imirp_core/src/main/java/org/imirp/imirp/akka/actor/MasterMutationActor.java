/**
*   Copyright 2014 Torben Werner, Bridget Ryan
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package org.imirp.imirp.akka.actor;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.akka.GuiceInjectedActorProducer;
import org.imirp.imirp.akka.actor.DbTargetPredictionResultActor.ResultStored;
import org.imirp.imirp.akka.actor.WildResultEliminatorActor.EliminateWildResultMsg;
import org.imirp.imirp.akka.messages.RunMutantSequenceMsg;
import org.imirp.imirp.akka.messages.TargetPredictionResultMsg;
import org.imirp.imirp.akka.messages.TargetPredictionResultMsg.WildResultEliminatedMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceFinishedMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceResultMsg;
import org.imirp.imirp.akka.messages.mutgen.StopRegionMutationsMsg;
import org.imirp.imirp.akka.messages.mutgen.StopRegionPredictions;
import org.imirp.imirp.db.ImirpDataStore;
import org.imirp.imirp.db.model.ImirpProjectMutationRequest;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;

import com.google.inject.Injector;
import com.typesafe.config.Config;

/**
 * Primary actor that is responsible for generating mutations for a given sequence.
 * 
 * @author torben
 * 
 */
public class MasterMutationActor extends UntypedActor {
	protected static final int MAX_RESULTS_PER_REGION = 5;

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private final ImirpDataStore datastore;
	private final ActorRef targetPredictor;
	private final ActorRef mutationGenerator;
	private final ActorRef dbResultStore;
	private final ActorRef targetPredictionResultPostProcessor;
	private HashMap<String, Long> outstandingTargetPredictionRuns;
	private long outstandingDbSaves = 0;
	private long outstandingWildEliminations = 0;
	private boolean finishedMutating = false;
	protected ImirpContext currentContext;
	private ObjectId currentMutationRequestId;
	private long totalMutations = 0L;
	private HashMap<String, AtomicInteger> validRegionResultCount;

	@Inject
	MasterMutationActor(Injector injector, Config config, ImirpDataStore datastore) {
		this.datastore = datastore;
        targetPredictor = getContext().actorOf(Props.create(GuiceInjectedActorProducer.class, injector, MasterTargetSitePredictor.class));
		mutationGenerator = getContext().actorOf(Props.create(GuiceInjectedActorProducer.class, injector, MutationGenerationActor.class));
		dbResultStore = getContext().actorOf(Props.create(GuiceInjectedActorProducer.class, injector, DbTargetPredictionResultActor.class));
		targetPredictionResultPostProcessor = getContext().actorOf(Props.create(GuiceInjectedActorProducer.class, injector, WildResultEliminatorActor.class));
		// Initialize state to accepting new processing
		getContext().become(ready);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		// Don't need to do anything here (we use context states to replace the default receive)
	}

	/**
	 * State for when we are actively processing a sequence
	 */
	Procedure<Object> processing = new Procedure<Object>() {
		private void incRegionTargetPredictionCount(String regionId, int incr){
			Long currentCount = outstandingTargetPredictionRuns.get(regionId);
			currentCount = currentCount == null ? incr : currentCount + incr;
			if(currentCount <= 0){
				outstandingTargetPredictionRuns.remove(regionId);
			}else{
				outstandingTargetPredictionRuns.put(regionId, currentCount);
			}
		}
		private void stopProcessingRegion(final String regionId) {
			log.info("Region[" + regionId + "] has achieved[" + MAX_RESULTS_PER_REGION + "] valid results. Cancelling further mutations...");
			mutationGenerator.tell(new StopRegionMutationsMsg(regionId), self());
			targetPredictor.tell(new StopRegionPredictions(regionId), self());
			outstandingTargetPredictionRuns.remove(regionId);
		}
		@Override
		public void apply(Object msg) {			
			if (msg instanceof MutateSequenceResultMsg) {
				MutateSequenceResultMsg mutResultMsg = (MutateSequenceResultMsg) msg;
				totalMutations += 1;
				// Scan the mutant sequence for predicted target sites 
				incRegionTargetPredictionCount(mutResultMsg.getContext().regionId, 1);
				targetPredictor.tell(
					new RunMutantSequenceMsg(mutResultMsg.context),
					self()
				);
			} else if (msg instanceof MutateSequenceFinishedMsg) {
				// We have finished all mutations (this message should never be received before the last results)
				finishedMutating = true;
				checkIfProcessingFinished();
			} else if (msg instanceof TargetPredictionResultMsg) {
				// Save the target prediction result into the database
				TargetPredictionResultMsg resultsMsg = (TargetPredictionResultMsg) msg;				
				incRegionTargetPredictionCount(resultsMsg.getContext().regionId, -1); // A target prediction run finished
				outstandingWildEliminations += 1;
				EliminateWildResultMsg filterRequest = new EliminateWildResultMsg(resultsMsg.getContext(), resultsMsg.getResults());
				targetPredictionResultPostProcessor.tell(filterRequest, self());
			}else if(msg instanceof WildResultEliminatedMsg){
				WildResultEliminatedMsg resultsMsg = (WildResultEliminatedMsg) msg;
				outstandingWildEliminations -= 1;
				if(resultsMsg.isHadValidResults()){
				    final String regionId = resultsMsg.context.regionId;
				    
				    // Keep track of how many valid results we have received, per-region
                    AtomicInteger regionValidCount = validRegionResultCount.get(regionId);
				    if(regionValidCount == null){
				        regionValidCount = new AtomicInteger(0);
				        validRegionResultCount.put(regionId, regionValidCount);
				    }
				    
				    // Store this result
				    outstandingDbSaves += 1; // We will have an additional save outstanding
				    dbResultStore.tell(new DbTargetPredictionResultActor.StoreResults(resultsMsg.context.projectId, regionId, resultsMsg.context.mutantSequence, resultsMsg.filteredResults), self());
				    
				    // Check if we have reached the threshold for the results per region that we want so we can save resources by finishing early.
				    if(regionValidCount.incrementAndGet() == MAX_RESULTS_PER_REGION){
				    	stopProcessingRegion(regionId);
				    }
				}
			} else if (msg instanceof ResultStored) {
				// A save finished, decrement the count of outstanding saves
				outstandingDbSaves -= 1;				
			} else {
				unhandled(msg);
			}
			checkIfProcessingFinished();
		}		
	};
	
	/**
	 * State for when we are ready to start processing a sequence (i.e. not currently processing)
	 */
	Procedure<Object> ready = new Procedure<Object>() {
		@Override
		public void apply(Object msg) {
			if (msg instanceof MutateSequenceMsg) {
				currentContext = ((MutateSequenceMsg)msg).getContext();
				validRegionResultCount = new HashMap<>();
				outstandingTargetPredictionRuns = new HashMap<>();
			    outstandingDbSaves = 0;
			    outstandingWildEliminations = 0;
			    finishedMutating = false;
				ImirpProjectMutationRequest historyItem = new ImirpProjectMutationRequest(
				        currentContext.invalidSiteTypes, 
				        currentContext.projectId,  
				        currentContext.strategy, 
				        currentContext.allowGUWobble
				);
				datastore.saveMutationRequest(historyItem);
				currentMutationRequestId = historyItem.id;
				totalMutations = 0L;
				// Forward this on to our generator and wait for mutations messages to start coming in...
				mutationGenerator.tell(msg, self());
				targetPredictor.tell(MasterTargetSitePredictor.START_MSG, self());
				getContext().become(processing);
			} else {
				unhandled(msg);
			}
		}
	};
	
	/**
	 * Helper method that checks if we are finished processing and resets our state to a ready state when we are finished
	 */
	private void checkIfProcessingFinished(){
		// We are finished processing when we are done mutating, have no outstanding target prediction runs and have no outstanding db saves
		if(finishedMutating && outstandingTargetPredictionRuns.size() == 0 && outstandingWildEliminations == 0 && outstandingDbSaves == 0){
		    log.info("Finished run for request[" + currentContext.projectId + "]");
		    int totalValidMutations = 0;
		    for(String regionId : validRegionResultCount.keySet()){
		    	totalValidMutations += validRegionResultCount.get(regionId).get();
		    }
		    datastore.completeMutationRequest(currentMutationRequestId, totalMutations, totalValidMutations);
		    targetPredictor.tell(MasterTargetSitePredictor.STOP_MSG, self());
			// Revert to our ready state
			getContext().become(ready);
		}// else do nothing (stay in our current state)
	}
}
