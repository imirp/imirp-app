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

package org.imirp.imirp.mutation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 
 * 
 * @author Torben
 *
 */
@JsonSerialize(using = MutationSiteSerializer.class)
@JsonDeserialize(using = MutationSiteDeserializer.class)
public class MutationSite {
	public final int startIndex;
	public final int endIndex;
	
    public MutationSite(){
	    startIndex = 0;
	    endIndex = 0;
	}

	public MutationSite(int startIndex, int endIndex) {
		super();
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	public MutationSite(int[] site) {
		this(site[0], site[1]);
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * @return the size of this mutation site
	 */
	public int size(){
		return endIndex - startIndex + 1;
	}

	/**
	 * Checks whether a given sequence index falls within this {@link MutationSite}
	 * 
	 * @param seqIndex any index of a sequence
	 * @return true if the index is within the mutation site, false otherwise
	 */
	public boolean containsSequenceIndex(int seqIndex) {
		return seqIndex >= startIndex && seqIndex <= endIndex;
	}

	@Override
	public String toString() {
		return "(" + startIndex + "," + endIndex + ")";
	}
	
}
