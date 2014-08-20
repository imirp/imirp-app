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

package org.imirp.imirp.mutation.generation;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.rule.SequentialChangesOnlyPerSiteRule;

public class RandomSequentialChangesMutationStrategyImpl implements MutationGenerationStrategy {

    private int groupSize;
    private Nucleotide[] nucTypes;
    private AtomicBoolean cancelled = new AtomicBoolean(false);
    private Random random = new Random();
	private SequentialChangesOnlyPerSiteRule filter;

    public RandomSequentialChangesMutationStrategyImpl(int groupSize, Nucleotide[] nucTypes) {
        this(groupSize, nucTypes, false);
    }

    public RandomSequentialChangesMutationStrategyImpl(int groupSize, Nucleotide[] nucTypes, boolean allowGreaterGroups) {
        this.groupSize = groupSize;
        this.nucTypes = nucTypes;
        this.filter = new SequentialChangesOnlyPerSiteRule(groupSize, allowGreaterGroups);
    }

    @Override
    public void generateMutations(MultiSiteMutation msm, MutationCallback callback) {
        do {
            for (MutationRegion region : msm.getRegions()) {
                generateMutations(msm.getSequence(), region, null);
            }
        } while (!cancelled.get()); // This method never completes. It just generates random permutations forever
        callback.stop(); // If we get here we only could have been cancelled
    }

    @Override
    public void generateMutations(String wildSequence, MutationRegion region, MutationCallback callback) {
        generateMutations(wildSequence, region, callback, true);
        callback.stop(); // If we get here we only could have been cancelled
    }

    private void generateMutations(String wildSequence, MutationRegion region, MutationCallback callback, boolean loopUntilCancelled) {
        do{
            StringBuilder sb = new StringBuilder(wildSequence);
            // For each site, randomly choose a position to insert the sequential changes
            for (MutationSite site : region.getMutationSites()) {
                int seqPos = random.nextInt(site.endIndex - site.startIndex) + site.startIndex;
                // and for each spot in the neighboring nuc group, randomly choose a nucleotide type
                for (int i = 0; i < groupSize; i++) {
                    sb.setCharAt(seqPos++, nucTypes[random.nextInt(nucTypes.length)].nuc);
                }
            }
            String mutantSequence = sb.toString();
			if(filter.checkRegion(region, wildSequence, mutantSequence)){
				callback.actOn(mutantSequence, region.getRegionId());
			}
        }while(!cancelled.get() && loopUntilCancelled);
    }

    @Override
    public void stop() {
        cancelled.set(true);
    }
}
