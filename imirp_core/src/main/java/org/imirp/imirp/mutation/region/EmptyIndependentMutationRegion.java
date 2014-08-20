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
 * An "empty" independant mutation region doesn't contain any sites but defines a chunk of the sequence
 * length which it represents.
 * 
 * @author torben
 *
 */
public class EmptyIndependentMutationRegion implements MutationRegion {
	private IndependentMutationRegion wrappedRegion;

	public EmptyIndependentMutationRegion(int start, int end) {
		this(start, end, null);
	}
	
	public EmptyIndependentMutationRegion(int start, int end, MutationRegion next) {
		wrappedRegion = new IndependentMutationRegion(start, end, next);
	}

	@Override
	public List<OverlappingRegion> getOverlapRegions() {
		return wrappedRegion.getOverlapRegions();
	}

	@Override
	public List<MutationSite> getMutationSites() {
		return wrappedRegion.getMutationSites();
	}

	@Override
	public MutationRegion getNextRegion() {
		return wrappedRegion.getNextRegion();
	}

	@Override
	public Integer getRegionStartIndex() {
		return wrappedRegion.getRegionStartIndex();
	}

	@Override
	public Integer getRegionEndIndex() {
		return wrappedRegion.getRegionEndIndex();
	}

	@Override
	public String createCombinedSiteSubSequenceForMutation(String sequence) {
		return "";
	}

	@Override
	public String reassembleSequence(String wildSequence, String mutatedRegionSubSequence) {
		return "";
	}

	@Override
	public MutationRegion newNext(MutationRegion nextRegion) {
		return new EmptyIndependentMutationRegion(wrappedRegion.getRegionStartIndex(), wrappedRegion.getRegionEndIndex(), nextRegion);
	}

	@Override
	public String getRegionId() {
		return wrappedRegion.getRegionId();
	}

	@Override
	public int getRegionLength() {
		return wrappedRegion.getRegionLength();
	}

	@Override
	public String toString() {
		return "EIR: " + getRegionId();
	}

	@Override
	public Integer getCombinedSitesLength() {
		return 0;
	}
	
}
