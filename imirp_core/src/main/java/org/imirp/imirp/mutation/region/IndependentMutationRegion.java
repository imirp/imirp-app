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
import java.util.Collections;
import java.util.List;

import org.imirp.imirp.mutation.MutationSite;

/**
 * A mutation region that is independent of other regions and is linked to the next independent region. Builds itself and following regions by adding
 * {@link MutationSite}'s incrementally.
 * 
 * @author Torben
 * 
 */
public class IndependentMutationRegion implements MutationRegion {
	/**
	 * The region start index, inclusive
	 */
	final int regionStartIndex;
	/**
	 * The region end index, inclusive.
	 */
	final int regionEndIndex;
	/**
	 * Areas where at least two sites overlap each other
	 */
	final List<OverlappingRegion> overlapRegions;
	/**
	 * A list of all the co-dependant mutation sites this region includes
	 */
	final List<MutationSite> mutationSites;
	/**
	 * The next, mutation region, if any
	 */
	final MutationRegion nextRegion;

	final String regionId;

	public IndependentMutationRegion(int regionStartIndex, int regionEndIndex, List<OverlappingRegion> overlapRegions, List<MutationSite> mutationSites, MutationRegion nextRegion) {
		super();
		this.regionStartIndex = regionStartIndex;
		this.regionEndIndex = regionEndIndex;
		this.regionId = generateRegionId();
		this.overlapRegions = Collections.unmodifiableList(overlapRegions);
		this.mutationSites = Collections.unmodifiableList(mutationSites);
		this.nextRegion = nextRegion;
	}
	
	public IndependentMutationRegion(int regionStartIndex, int regionEndIndex, MutationRegion nextRegion) {
		this(regionStartIndex, regionEndIndex, new ArrayList<OverlappingRegion>(), new ArrayList<MutationSite>(), nextRegion);
	}
	
	public IndependentMutationRegion(int regionStartIndex, int regionEndIndex) {
		this(regionStartIndex, regionEndIndex, null);
	}
	
	public IndependentMutationRegion(MutationSite site) {
		this.regionStartIndex = site.getStartIndex();
		this.regionEndIndex = site.getEndIndex();
		this.regionId = generateRegionId();
		ArrayList<MutationSite> sitesList = new ArrayList<>();
		sitesList.add(site);
		this.mutationSites = Collections.unmodifiableList(sitesList);
		this.overlapRegions = Collections.unmodifiableList(new ArrayList<OverlappingRegion>());
		this.nextRegion = null;
	}
	
	private String generateRegionId(){
		return generateRegionId(regionStartIndex, regionEndIndex);
	}
	
	public static String generateRegionId(int startIdx, int endIdx){
		return startIdx + "_" + endIdx;
	}

	@Override
	public List<OverlappingRegion> getOverlapRegions() {
		return overlapRegions;
	}

	@Override
	public List<MutationSite> getMutationSites() {
		return mutationSites;
	}

	@Override
	public MutationRegion getNextRegion() {
		return nextRegion;
	}
	
	@Override
	public Integer getRegionStartIndex() {
		return regionStartIndex;
	}

	@Override
	public Integer getRegionEndIndex() {
		return regionEndIndex;
	}
	
	@Override
	public String getRegionId() {
		return regionId;
	}

	public String createCombinedSiteSubSequenceForMutation(String sequence) {
		assert sequence.length() >= regionEndIndex; // Sequence must be at least as long as the region

		StringBuilder sb = new StringBuilder();
		int lastEnd = 0; // keep track of the last area we ended so that we don't duplicate overlapping zones
		for (MutationSite site : mutationSites) {
			sb.append(sequence.substring(Math.max(lastEnd, site.getStartIndex()), // take the max so we don't duplicate
					site.getEndIndex() + 1));
			lastEnd = site.getEndIndex() + 1;
		}

		return sb.toString();
	}
	
	public String reassembleSequence(String wildSequence, String mutatedRegionSubSequence) {
		StringBuilder sb = new StringBuilder();
		int lastEnd = 0; // keep track of the last area we ended so that we don't duplicate overlapping zones
		int regionIdx = 0, overlapLength;
		for (MutationSite site : mutationSites) {
			// Append some of the wild sequence from our last position up to the current mutation site
			if(site.getStartIndex() > lastEnd){
				sb.append(wildSequence.substring(lastEnd, site.getStartIndex()));
			}
			
			// See how much overlap we had with the last site, if any
			overlapLength = Math.min(0, site.getStartIndex() - lastEnd); // should always be a negative value or 0
			
			// Append the mutation site's mutated sequence
			int nextRegionChunkLength = overlapLength + site.size();
			sb.append(
					mutatedRegionSubSequence.substring(regionIdx, regionIdx + nextRegionChunkLength)
				);
			regionIdx += nextRegionChunkLength;
			
			// Continue at the next character after our last mutation nucleotide
			lastEnd = site.getEndIndex() + 1;
		}
		
		// Append any last bits of the wild sequence
		if(lastEnd < wildSequence.length()){
			sb.append(wildSequence.substring(lastEnd));
		}
		
		assert sb.length() == wildSequence.length();

		return sb.toString(); // finished!
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IR").append(regionId).append(": (").append(regionStartIndex).append(",").append(regionEndIndex).append(") => OL: [");
		for (OverlappingRegion ol : overlapRegions) {
			sb.append("(").append(ol.overlapStart).append(",").append(ol.overlapEnd).append(")");
		}
		sb.append("]");
		
		// Also append the next region
		if (nextRegion != null) {
			sb.append("\n");
			sb.append(nextRegion.toString());
		}
		
		return sb.toString();
	}

	@Override
	public MutationRegion newNext(MutationRegion nextRegion) {
		return new IndependentMutationRegion(regionStartIndex, regionEndIndex, overlapRegions, mutationSites, nextRegion);
	}

	@Override
	public int getRegionLength() {
		return regionEndIndex - regionStartIndex + 1;
	}

	@Override
	public Integer getCombinedSitesLength() {
		int lastEnd = 0;
		int length = 0;
		for(MutationSite site : getMutationSites()){
			if(site.getStartIndex() > lastEnd){
				lastEnd = site.getStartIndex();
			}
			length += site.getEndIndex() - lastEnd;
			lastEnd = site.getEndIndex();
		}
		return length;
	}
}
