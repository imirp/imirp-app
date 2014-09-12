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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.akka.actor.RegionMutationGenerationActor.MutateSequenceRegionFinishedMsg;
import org.imirp.imirp.akka.actor.RegionMutationGenerationActor.MutateSequenceRegionMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceFinishedMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceResultMsg;
import org.imirp.imirp.akka.messages.mutgen.StopRegionMutationsMsg;
import org.imirp.imirp.mutation.region.MutationRegion;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;

/**
 * This actor is responsible for generating all possible mutations given a mutation strategy.
 * For each valid mutation it generates, it sends a message including the original mutation parameters
 * as well as the mutated sequence, including the region that contains the change(s) used to produce
 * the mutated sequence.
 * 
 * @author torben
 *
 */
public class MutationGenerationActor extends UntypedActor {
	final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private static final long REGION_PROCESSING_TIMEOUT_MINUTES = 15; // 20 minutes
	
	private ImirpContext currentWork;
	private ActorRef supervisor;
	private HashMap<String, ActorRef> regionsBeingProcessed;
	private AtomicLong totalMutationsCtr;
	
	@Inject
	public MutationGenerationActor() {
		super();
		log.info("Starting mutation generation actor");
		getContext().become(ready);
		
	}
	
	Procedure<Object> ready = new Procedure<Object>() {
        @Override
        public void apply(Object msg) {
            if (msg instanceof MutateSequenceMsg) {
                // Begin processing a sequence...
                log.info("Received mutate sequence work[{}].", msg);
                currentWork = ((MutateSequenceMsg) msg).getContext();
                regionsBeingProcessed = new HashMap<>();
                // Spawn child actors for each region
                final Iterable<MutationRegion> regions = currentWork.mutParams.getRegions();
                for(MutationRegion region : regions){
                    final ActorRef regionActor = getContext().actorOf(Props.create(RegionMutationGenerationActor.class));
                    regionsBeingProcessed.put(region.getRegionId(), regionActor); // keep track of our region actors
                    regionActor.tell(new MutateSequenceRegionMsg(currentWork, region.getRegionId()), self()); // tell them to start right away
                    // Schedule a timeout
                    getContext().system().scheduler().scheduleOnce(
                    		Duration.create(REGION_PROCESSING_TIMEOUT_MINUTES, TimeUnit.MINUTES),
                    		regionActor,
                    		new StopRegionMutationsMsg.RegionMutationsTimeoutMsg(region.getRegionId()),
                    		getContext().dispatcher(),
                    		self()
                    	);
                }
                totalMutationsCtr = new AtomicLong(0);
                supervisor = sender();
                getContext().become(processing);
            } else {
                unhandled(msg);
            }
        }
    };
	
	Procedure<Object> processing = new Procedure<Object>() {
        @Override
        public void apply(Object msg) {
            if(msg instanceof StopRegionMutationsMsg){
                StopRegionMutationsMsg stopMsg = ((StopRegionMutationsMsg)msg);
                log.info("Stopping further mutations for region[" + stopMsg.regionId + "]");
                ActorRef regionActor = regionsBeingProcessed.get(stopMsg.regionId);
                if(regionActor != null){
                    regionActor.tell(msg, self());
                }
            } else if(msg instanceof MutateSequenceResultMsg){
                totalMutationsCtr.getAndIncrement();
                // Notify supervisor of a new mutation result
                supervisor.tell(msg, self());
            } else if(msg instanceof MutateSequenceRegionFinishedMsg){
                // A region is finished generating mutations
                completeRegion(((MutateSequenceRegionFinishedMsg)msg).regionId);
            } else {
                unhandled(msg);
            }
        }
	};
	
	private void completeRegion(String regionId){
	    // Mark region as completed
	    ActorRef aref = regionsBeingProcessed.remove(regionId);
	    getContext().stop(aref); // stop actor
	    
	    // Check if we are done all regions
	    if(regionsBeingProcessed.size() == 0){
    	    // All regions are now complete.
    	    supervisor.tell(new MutateSequenceFinishedMsg(currentWork.projectId, totalMutationsCtr.get()), self());
    	    getContext().become(ready); // We're ready for the next task now!
	    }
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		// No need to implement (using procedure state instead)
	}
	
}
