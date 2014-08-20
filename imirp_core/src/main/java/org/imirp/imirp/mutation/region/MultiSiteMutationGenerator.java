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

package org.imirp.imirp.mutation.region;

import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.util.MutantSequenceGenerator;

public class MultiSiteMutationGenerator {
	public static void mutateAndOperate(final MultiSiteMutation msm, final MutationCallback maction) {		
		// Take a region
		// Run mutations on all sites at once
		MutationRegion region = msm.getStartRegion();
		do{			
			String subSequence = region.createCombinedSiteSubSequenceForMutation(msm.getSequence());
			final MutationRegion currentRegion = region;
			MutantSequenceGenerator.mutateAndOperate(subSequence, 0, subSequence.length() - 1, currentRegion.getRegionId(), new MutationCallback(){
				@Override
				public void actOnSequence(String mutatedRegionSubSequence, String regionId) {
					// Reassemble the full sequence with the mutation modifications
					String fullMutatedSequence = currentRegion.reassembleSequence(msm.getSequence(), mutatedRegionSubSequence);
					// Check if this mutated sequence satisfies our rule set
					if(msm.doesMutationSatisfyRules(fullMutatedSequence)){
						// The mutation satisfies rules, act on it
						maction.actOn(fullMutatedSequence, regionId);
					}else{
						// Ignore mutations that don't satisfy the rules
					}					
				}
			});
			region = region.getNextRegion();
		}while(region != null);
	}
}
