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

package org.imirp.imirp.tsp;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Species implements Comparable<Species>, Serializable {
	private static final long serialVersionUID = 80245150101891518L;
	
	private String id;
	private String genus;
	private String species;

	public Species() {
		super();
	}

	public Species(String id, String genus, String species) {
		this.id = id;
		this.genus = genus;
		this.species = species;
	}

	public Species(String id) {
		this(id, null, null);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	@Override
	public int compareTo(Species that) {
		return this.id.compareTo(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Species) {
			Species that = (Species) obj;
			return this.id.equals(that.id);
		}
		return false;
	}

	@Override
	public String toString() {
		return id;
	}

	@JsonIgnore
	public String getDisplayName() {
		return getGenus() + " " + getSpecies();
	}

}
