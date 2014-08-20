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

/**
 * Defines a type of target site prediction
 * 
 * @author Torben
 *
 */
public class TargetSiteType {
	public final SiteType siteType;
	public final boolean guWobble;

	TargetSiteType(){
		this.siteType = null;
		this.guWobble = false;
	}
	
	public TargetSiteType(SiteType siteType, boolean guWobble) {
		super();
		this.siteType = siteType;
		this.guWobble = guWobble;
	}

	/**
	 * Creates an instance from a config string which is a ":" separated tuple of the
	 * {@link SiteType} and whether a GU wobble is allowed
	 */
	public TargetSiteType(String configString) {
		this(
				SiteType.fromName(configString.split(":")[0]), 
				Boolean.parseBoolean(configString.split(":")[1])
		);
	}

	public SiteType getSiteType() {
		return siteType;
	}

	public boolean isGuWobble() {
		return guWobble;
	}
	
	public static TargetSiteType[] values(){
		SiteType[] siteTypes = SiteType.values();
		TargetSiteType[] combinations = new TargetSiteType[siteTypes.length * 2];
		int i = 0;
		for(SiteType st : siteTypes){
			combinations[i++] = new TargetSiteType(st, false);
			combinations[i++] = new TargetSiteType(st, true);
		}
		
		return combinations;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TargetSiteType){
			TargetSiteType other = (TargetSiteType)obj;
			return 
					this.guWobble == other.guWobble
					&&
					this.siteType == other.siteType;
					
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 37)
				   .append(guWobble)
			       .append(siteType)
			       .toHashCode();
	}

	@Override
	public String toString() {
		return siteType.toString() + (guWobble ? "-GU" :"");
	}
}
