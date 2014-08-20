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

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Types of sites that can be matched.
 * 
 * @author torben
 *
 */
@JsonDeserialize(using = SiteTypeDeserializer.class)
@JsonSerialize(using = SiteTypeSerializer.class)
public enum SiteType {
	SIX_MER("6mer"), SEVEN_MER_M8("7mer-m8"), SEVEN_MER_A1("7mer-A1"), EIGHT_MER("8mer"), OFFSET_SIX_MER("OS-6mer");
	public final String name;
	
	SiteType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static @Nullable SiteType fromName(String name) {
		for(SiteType st : values()){
			if(st.name.equals(name)){
				return st;
			}
		}
		return null;
	}
	
}
