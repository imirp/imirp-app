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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MutationRegion;

/**
 * A rule which requires each site to have the specified minimum number of changes in sequence of each other
 * 
 * @author torben
 *
 */
public class IdenticalNeighboringChangesPerSiteRule extends AbstractIndependentRegionRule {


	private Nucleotide nucType;
	private int minNeghbors;

	public IdenticalNeighboringChangesPerSiteRule(Nucleotide nucType, int minNeghbors){
		this.nucType = nucType;
		this.minNeghbors = minNeghbors;
	}
	
	@Override
	public boolean checkRegion(MutationRegion region, String wildSequence, String mutantSequence) {
		List<MutationSite> mutationSites = region.getMutationSites();
		
		// These structures keep track of what positions we last saw a difference within a site and how many consecutive differences we have seen per site
		List<AtomicInteger> siteChanges = new ArrayList<>(mutationSites.size());
		Map<Integer, Integer> siteLastNeighborIndex = new HashMap<>(mutationSites.size());
		initLists(mutationSites, siteChanges, siteLastNeighborIndex); // initialize these structures
		
		// Simply iterate through each nucleotide character and check if there is a difference
		for(int seqIndex = 0; seqIndex < wildSequence.length(); seqIndex++){
			if(mutantSequence.charAt(seqIndex) == nucType.toChar() && wildSequence.charAt(seqIndex) != mutantSequence.charAt(seqIndex)){
				// Update our neighboring site information and check that the neighbors are still valid
				if(!updateSiteNeighborIndex(siteChanges, siteLastNeighborIndex, mutationSites, seqIndex)){
					return false;
				}
			}
		}
		
		// Ensure all sites had at least one match, otherwise insta-fail
		if(mutationSites.size() != siteChanges.size()){
			return false; // at least one site was missing - FAIL!
		}
		
		// Now make sure each site had the appropriate number of neighbors
		for(int i = 0; i < mutationSites.size(); i++){
			if(siteChanges.get(i).get() < minNeghbors){
				return false; // This site had an incorrect number of neighbors
			} 			
		}
		
		// Okay, all sites had the correct number of neighbors!
		return true;
	}
	
	/**
	 * Initializes our internal data structures to make things easier later (i.e. we don't have to null check in a bunch of different areas)
	 */
	protected static void initLists(List<MutationSite> mutationSites, List<AtomicInteger> sitesValidated, Map<Integer, Integer> siteLastNeighborIndex){
		for(int i = 0; i < mutationSites.size(); i++){
			sitesValidated.add(new AtomicInteger(0));
			siteLastNeighborIndex.put(i, -1);// -1 so we know when it hasn't been initialized
		}
	}

	/**
	 * Checks each site for whether it contains
	 */
	protected static boolean updateSiteNeighborIndex(List<AtomicInteger> sitesChanges, Map<Integer, Integer> siteNeighborIndexes, List<MutationSite> mutationSites, int mutationDiffIndex) {
		// Iterate through mutation sites
		for(int siteNum = 0; siteNum < mutationSites.size(); siteNum++){
			// First, check that the current site contains the given mutation difference
			if(mutationSites.get(siteNum).containsSequenceIndex(mutationDiffIndex)){
				// Now check whether this index is next to a previous difference index
				if(siteNeighborIndexes.get(siteNum) == -1 || (siteNeighborIndexes.get(siteNum) + 1) == mutationDiffIndex){
					// Okay, this is another neighbor for the current site
					siteNeighborIndexes.put(siteNum, mutationDiffIndex);
					sitesChanges.get(siteNum).incrementAndGet();
				}else{
					return false;
				}
			}
		}
		
		return true;
	}

}
