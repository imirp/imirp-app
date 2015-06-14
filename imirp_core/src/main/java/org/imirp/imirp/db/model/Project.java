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

package org.imirp.imirp.db.model;

import java.util.Collection;
import java.util.Date;

import org.bson.types.ObjectId;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.tsp.Species;
import org.jongo.marshall.jackson.oid.Id;

/**
 * 
 * @author twerner
 * 
 */
public class Project {
	@Id
	public ObjectId _id;
	public String email;
	public Date dateCreated;
	public String name;
	public String description;
	public String wildSequence;
	public Collection<MutationSite> mutationSites;
	public Species species;

	public Project() {
		super();
	}

	public Project(String email, String name, String description, String wildSequence, Collection<MutationSite> mutationSites, Species species) {
		super();
		this.email = email;
		this.dateCreated = new Date();
		this.name = name;
		this.description = description;
		this.wildSequence = wildSequence;
		this.mutationSites = mutationSites;
		this.species = species;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getWildSequence() {
		return wildSequence;
	}

	public void setWildSequence(String wildSequence) {
		this.wildSequence = wildSequence;
	}

	public Collection<MutationSite> getMutationSites() {
		return mutationSites;
	}

	public void setMutationSites(Collection<MutationSite> mutationSites) {
		this.mutationSites = mutationSites;
	}

	public Species getSpecies() {
		return species;
	}

	public void setSpecies(Species species) {
		this.species = species;
	}

}
