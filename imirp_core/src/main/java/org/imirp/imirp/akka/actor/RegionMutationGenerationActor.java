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

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceResultMsg;
import org.imirp.imirp.akka.messages.mutgen.StopRegionMutationsMsg;
import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.mutation.MutationContext;
import org.imirp.imirp.mutation.generation.MutationGenerationStrategy;
import org.imirp.imirp.mutation.generation.RandomSequentialChangesMutationStrategyImpl;
import org.imirp.imirp.mutation.region.MutationRegion;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;

/**
 * Actor which generates mutations within a specific defined region of a sequence
 * 
 * @author twerner
 * 
 */
public class RegionMutationGenerationActor extends UntypedActor {
    private static final AtomicLong mutIdCtr = new AtomicLong(0); // generate unique mutation identifiers
    private static final long MAX_UNIQUE_MUTATIONS = 100000;
    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    
    private ExecutorService mutationExecutor = Executors.newSingleThreadExecutor();
    private MutationGenerationStrategy strategy;
    private ActorRef supervisor;

    @Inject
    public RegionMutationGenerationActor() {
        getContext().become(ready);
    }

    Procedure<Object> ready = new Procedure<Object>() {
        @Override
        public void apply(Object msg) {
            if (msg instanceof MutateSequenceRegionMsg) {
                final MutateSequenceRegionMsg mutMsg = (MutateSequenceRegionMsg) msg;
                supervisor = sender();
                log.info("Received mutate sequence work for region[{}].", mutMsg.regionId);
                // Grab the region we are supposed to mutate
                MutationRegion region = mutMsg.context.mutParams.getStartRegion();
                while (!region.getRegionId().equals(mutMsg.regionId)) {
                    region = region.getNextRegion();
                }
                // Setup the mutation strategy, start mutating and become processing
                strategy = new RandomSequentialChangesMutationStrategyImpl(mutMsg.context.strategy.changes, mutMsg.context.strategy.nucleotides);
                startMutating(mutMsg.context, region);
                getContext().become(processing);
            }else{
                unhandled(msg);
            }
        }

        private void startMutating(final ImirpContext iContext, final MutationRegion region) {
            final String regionId = region.getRegionId();
            mutationExecutor.submit(new Runnable() {
                private HashSet<String> mutationHistory = new HashSet<>();
                private AtomicInteger consecutiveDuplicates = new AtomicInteger(0);
                private int maxConsecutiveDuplicateMutationsThreshold = calculateConsecutiveDuplicateMutationsThreshold();
                
                @Override
                public void run() {
                    // Generate mutations and, for each successful mutation, tell our supervisor about it for further processing
                    strategy.generateMutations(iContext.wildSequence, region, new MutationCallback() {
                        @Override
                        public void actOnSequence(String mutatedSequence, String regionId) {
                        	// Avoid duplicate mutations
                            if (!mutationHistory.contains(mutatedSequence)) {
                            	consecutiveDuplicates.set(0);
                                mutationHistory.add(mutatedSequence);
                                // le' successful mutation! :D
                                MutationContext mContext = new MutationContext(iContext, mutatedSequence, regionId, mutIdCtr.incrementAndGet());
                                supervisor.tell(new MutateSequenceResultMsg(mContext), self());
                            }else{
                            	// If we hit the threshold for maximum, consecutive duplicate mutations, stop generating new mutations
                            	// because we've probably exhausted the problem space by now
                            	if(consecutiveDuplicates.incrementAndGet() >= maxConsecutiveDuplicateMutationsThreshold){
                            		log.info("At least [" + maxConsecutiveDuplicateMutationsThreshold + "] consecutive, duplicate mutations have already been generated for region[ " + regionId + "]. Stopping further mutations...");
                            		strategy.stop();
                            	}
                            }
                            // Stop mutating if we reach our maximum unique mutation threshold
                            if(mutationHistory.size() >= MAX_UNIQUE_MUTATIONS){
                            	log.info("At least [" + MAX_UNIQUE_MUTATIONS + "] unique mutations have been generated for region[ " + regionId + "]. Stopping further mutations...");
                            	strategy.stop();
                            }
                        }
                        @Override
                        protected void onStop(long totalMutations) {
                        	finished();
                        }
                        private void finished(){
                        	// le finished! :D
                            supervisor.tell(new MutateSequenceRegionFinishedMsg(regionId, mutationHistory.size()), self());
                            mutationHistory = new HashSet<>();
                            getContext().become(ready);
                        }
                    });
                }                
                /**
                 * Basically, this threshold calculation helps us avoid unnecessary computation. It does this by calculating a threshold
                 * that roughly correlates to our confidence that we have explored most of the mutation possibilities.
                 * 
                 * We want small mutations (i.e. 1 small site with only one nucleotide) to have a high threshold because the likelihood of
                 * seeing duplicated/repeated mutations is statistically higher given a small number of mutation attempts.
                 * 
                 * In the case of larger, or more complex mutation problems, the number of total mutations possible gets very large.
                 * Assuming our random mutation generation strategy is uniformly distributed, we would expect the likelihood of seeing
                 * repeated mutations to drop quite quickly and thus want our threshold to be low.
                 * 
                 * @return a threshold for how many duplicate mutations we will allow before we stop
                 */
                private int calculateConsecutiveDuplicateMutationsThreshold(){
                	int combinedSiteLength = region.getCombinedSitesLength();
                	if(combinedSiteLength <= 10){
                		return 100; // around the order of magnitude of 1,000,000 if we assume each of the 10 positions could have any of the 4 nucleotides
                	}else{
                		int consecFactor = 400000 / (combinedSiteLength * combinedSiteLength * combinedSiteLength * iContext.strategy.nucleotides.length);
                		return Math.max(2, consecFactor);
                	}
                }
            });

        }
    };

    Procedure<Object> processing = new Procedure<Object>() {
        @Override
        public void apply(Object msg) {
        	if(msg instanceof StopRegionMutationsMsg.RegionMutationsTimeoutMsg){
        		log.info("MUTATION TIMEOUT: Stopping further processing for region " + ((StopRegionMutationsMsg.RegionMutationsTimeoutMsg)msg).regionId);
        		stopMutating();
        	}else if (msg instanceof StopRegionMutationsMsg) {
        		stopMutating();
            }else{
                unhandled(msg);
            }
        }

        private void stopMutating() {
            strategy.stop();
        }
    };

    @Override
    public void onReceive(Object msg) throws Exception {
        // Nothing to do here, use Procedure and become()
    }

    public static final class MutateSequenceRegionFinishedMsg implements Serializable {
		private static final long serialVersionUID = 8051767752644736343L;
		public final String regionId;
        public final long totalMutationsGenerated;

        public MutateSequenceRegionFinishedMsg(String regionId, long total) {
            super();
            this.regionId = regionId;
            this.totalMutationsGenerated = total;
        }

        public String getRegionId() {
            return regionId;
        }

        public long getTotalMutationsGenerated() {
            return totalMutationsGenerated;
        }
    }

    public static final class MutateSequenceRegionMsg implements Serializable {
		private static final long serialVersionUID = -3337268555342668383L;
		private final ImirpContext context;
        private final String regionId;

        MutateSequenceRegionMsg() { // for serialization purposes (i.e. Jackson)
            super();
            context = null;
            regionId = null;
        }

        public MutateSequenceRegionMsg(ImirpContext context, String regionId) {
            super();
            this.context = context;
            this.regionId = regionId;
        }

    }
}
