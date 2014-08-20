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

import java.util.List;

import org.imirp.imirp.mutation.MutationSite;

/**
 * Defines a region for mutation within a sequence.
 * 
 * @author torben
 *
 */
public interface MutationRegion {

	/**
	 * @return the list of overlapping mutation regions
	 */
	List<OverlappingRegion> getOverlapRegions();

	/**
	 * @return the list of mutation sites
	 */
	List<MutationSite> getMutationSites();

	/**
	 * @return the next region, or null if there is none
	 */
	MutationRegion getNextRegion();

	/**
	 * @return the start index of this region
	 */
	Integer getRegionStartIndex();

	/**
	 * @return the end index of this region
	 */
	Integer getRegionEndIndex();
	
	/**
	 * 
	 * @return the length of all mutation sites combined (without re-counting the overlapped portions)
	 */
	Integer getCombinedSitesLength();

	/**
	 * @param sequence
	 * @return a subset of the original, wild sequence that contains all areas within this region's mutation sites
	 */
	String createCombinedSiteSubSequenceForMutation(String sequence);

	/**
	 * @param wildSequence the original, wild sequence which the mutated sequence was generated from
	 * @param mutatedRegionSubSequence a mutated sub sequence derived from a wild sequence
	 * @return the full length sequence with mutation modifications
	 */
	String reassembleSequence(String wildSequence, String mutatedRegionSubSequence);

	/**
	 * Returns a new version of the MutationRegion having the next region set to the specified one
	 */
	MutationRegion newNext(MutationRegion nextRegion);

	/**
	 * @return the id of this region
	 */
	String getRegionId();

	/**
	 * @return the length of this region (end-start)
	 */
	int getRegionLength();
}
