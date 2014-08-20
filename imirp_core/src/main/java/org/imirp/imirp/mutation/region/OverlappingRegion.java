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

import org.imirp.imirp.mutation.MutationSite;

/**
 * Describes an overlapping region
 * 
 * @author Torben
 * 
 */
public class OverlappingRegion {
	int overlapStart;
	int overlapEnd;

	public OverlappingRegion(int overlapStart, int overlapEnd) {
		super();
		this.overlapStart = overlapStart;
		this.overlapEnd = overlapEnd;
	}

	/**
	 * @param site1
	 *            a mutation site which may or may not overlap with another site
	 * @param site2
	 *            a mutation site which may or may not overlap with the other site
	 * @return a SubRegion where the two sites overlap, or null if no sub region exists
	 */
	public static OverlappingRegion getSubRegion(MutationSite site1, MutationSite site2) {
		// Check which site comes first
		if (site1.getStartIndex() > site2.getStartIndex()) {
			// Site 1 starts after site 2
			return getSubRegion0(site2, site1);
		} else {
			// Site 2 starts after site 1
			return getSubRegion0(site1, site2);
		}
	}

	private static OverlappingRegion getSubRegion0(MutationSite firstSite, MutationSite laterSite) {
		if (firstSite.getEndIndex() <= laterSite.getEndIndex() && firstSite.getEndIndex() >= laterSite.getStartIndex()) {
			return new OverlappingRegion(laterSite.getStartIndex(), firstSite.getEndIndex());
		}

		return null;
	}
}