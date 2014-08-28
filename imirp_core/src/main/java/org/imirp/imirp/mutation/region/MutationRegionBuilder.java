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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.imirp.imirp.mutation.MutationSite;

public class MutationRegionBuilder {
	public static int INDEPENDENT_REGION_SEPARATION_LENGTH = 7;
	
	private ArrayList<MutationRegion> regions = new ArrayList<MutationRegion>();
	
	public MutationRegionBuilder(){}
	
	public MutationRegionBuilder addSites(MutationSite... sites){
		for(MutationSite site : sites){
			addSite(site);
		}
		return this;
	}
	
	public MutationRegionBuilder addSites(Collection<MutationSite> sites){
		for(MutationSite site : sites){
			addSite(site);
		}
		return this;
	}
	
	void addSite(MutationSite site){
		// Check that this site is valid for our current state
		
		if(regions.size() > 0){
			MutationRegion mr = regions.get(regions.size() - 1);			
			if(mr.getRegionStartIndex() > site.getStartIndex()){
				throw new IllegalArgumentException("The site [" + site + "] is invalid because it extends past the previous region[" + mr +"]'s start index.");
			}
		}
		
		// Check if we have any independent region in progress already
		if(regions.size() == 0 || !(regions.get(regions.size() - 1) instanceof IndependentMutationRegion)){
			// Nope, just start a new independent region and we're done
			regions.add(new IndependentMutationRegion(site));
			return;
		}
		
		// Subsequent sites check for overlap or new independent region sites
		MutationRegion lastRegion = regions.get(regions.size() - 1);		
		List<MutationSite> currentSites = new ArrayList<>(lastRegion.getMutationSites());
		List<OverlappingRegion> currentOverlaps = new ArrayList<>(lastRegion.getOverlapRegions());
		MutationRegion nextRegion = null;
		int regionStart = lastRegion.getRegionStartIndex(), regionEnd = lastRegion.getRegionEndIndex();
		if ((lastRegion.getRegionEndIndex() + INDEPENDENT_REGION_SEPARATION_LENGTH) >= site.getStartIndex()) {
			// We have a site that must be considered with the previous site(s)

			// Check for an overlap
			if(currentSites.size() > 0){
				MutationSite previousSite = currentSites.get(currentSites.size() - 1);
				OverlappingRegion overlap = OverlappingRegion.getSubRegion(previousSite, site);
				if (overlap != null) {
					currentOverlaps.add(overlap);
				}
			}

			// Add the next, dependent site
			currentSites.add(site);
			regionEnd = site.getEndIndex(); // extend our end index
			regionStart = lastRegion.getRegionStartIndex();			
		} else {
			// We have a new independent region
			nextRegion = new IndependentMutationRegion(site);
		}
		// Update the last region we were working on
		lastRegion = new IndependentMutationRegion(
				regionStart, 
				regionEnd, 
				currentOverlaps, 
				currentSites, 
				null
			);
		regions.set(regions.size() - 1, lastRegion);
		
		// If we are starting a new region, add it
		if(nextRegion != null){
			regions.add(nextRegion);
		}
	}
	
	public MutationRegionBuilder addEmptyRegion(int length){
		int start = 0, end = length - 1;
		if(regions.size() > 0){
			start = regions.get(regions.size() - 1).getRegionEndIndex() + 1;
			end = start + length - 1;
		}
		
		return addEmptyRegion(start, end);
	}
	
	public MutationRegionBuilder addEmptyRegion(int start, int end){
		regions.add(new EmptyIndependentMutationRegion(start, end));
		return this;
	}
	
	public MutationRegion build(){
		if(regions.size() == 0){
			throw new UnsupportedOperationException("Nothing to build. You need to add some regions/sites first.");
		}
		
		// Build a linked list, starting from the tail, and return the head of that list
		MutationRegion lastRegion = null;
		for(int i = regions.size() - 1; i >= 0; i--){
			if(lastRegion == null){
				// Start the tail of the linked list of regions
				lastRegion = regions.get(i);
			}else{
				// Update the head of the tail
				lastRegion = regions.get(i).newNext(lastRegion);
			}
		}
		
		return lastRegion;
	}
}
