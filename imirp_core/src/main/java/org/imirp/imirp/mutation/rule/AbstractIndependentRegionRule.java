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

package org.imirp.imirp.mutation.rule;

import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.rule.support.MutationRuleObserver;

/**
 * A generic rule for checking independent regions iteratively.
 * 
 * @author torben
 *
 */
public abstract class AbstractIndependentRegionRule implements MutationRule {

	@Override
	public boolean checkRule(MutationRuleObserver observer,	String wildSequence, String mutantSequence) {
		MultiSiteMutation multiSites = (MultiSiteMutation)observer;
		
		// Iterate through the list of regions and check them against the rule(s) one at a time
		MutationRegion region = multiSites.getStartRegion();
		boolean regionSatisfiesRules = region != null;
		do{
			regionSatisfiesRules &= checkRegion(region, wildSequence, mutantSequence);
		}while(regionSatisfiesRules != false && (region = region.getNextRegion()) != null); // keep checking while we have another region and the rules have been satisfied so far
		
		return regionSatisfiesRules;		
	}

	/**
	 * @param msm a region that needs to be checked
	 * @param wildSequence the wild sequence
	 * @param mutantSequence the mutant sequence
	 * @return true if the mutation satisfies this rule for the given region, false otherwise
	 */
	public abstract boolean checkRegion(MutationRegion region, String wildSequence, String mutantSequence);
}
