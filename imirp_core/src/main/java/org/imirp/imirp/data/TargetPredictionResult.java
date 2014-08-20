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

package org.imirp.imirp.data;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class TargetPredictionResult {

	public final String microRNA;
	/**
	 * The start position of the site (the index of the first of 8 nucleotides within the input sequence)
	 */
	public final int position;
	public final TargetSiteType type;

	TargetPredictionResult(){
		this.position = 0;
		this.type = null;
		this.microRNA = null;
	}
	
	public TargetPredictionResult(String microRNA, int position, TargetSiteType type) {
		super();
		this.microRNA = microRNA;
		this.position = position;
		this.type = type;
	}
	
	public TargetPredictionResult(String microRNA, int position, SiteType siteType, boolean guWobble) {
		this(microRNA, position, new TargetSiteType(siteType, guWobble));
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TargetPredictionResult){
			TargetPredictionResult other = (TargetPredictionResult)obj;
			return 
					this.position == other.position
					&&
					this.type.equals(other.type)
					&&
					this.microRNA.equals(other.microRNA);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 37)
				   .append(microRNA)
			       .append(type)
			       .append(position)
			       .toHashCode();
	}

	/**
	 * Checks if this result is a hit on the other result.
	 * 
	 * @param result
	 *            the result we want to check for a "hit"
	 * @return true if the result is a hit on this result
	 */
	public boolean isHit(TargetPredictionResult result) {
		// If they have the same position...
		if (result.position == this.position) {
			// And the same MiRNA name
			if (result.microRNA.equals(this.microRNA)) {
				// Then they are a hit
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return "mirna:" + microRNA + ",pos:" + position + "," + type; 
	}
	
}
