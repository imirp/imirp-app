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

package dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.bson.types.ObjectId;
import org.imirp.imirp.db.model.Project;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.tsp.Species;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import common.JsonDateSerializer;

public final class ProjectDto implements Serializable {
    private static final long serialVersionUID = 735239150106801512L;        
    public String id;
    public String name;
    public String description;        
    @JsonSerialize(using=JsonDateSerializer.class)
    public Date dateCreated;
    public String sequence;
	public Collection<MutationSite> mutationSites;
	public Species species;
	
    public ProjectDto(){}

    public static ProjectDto fromModel(Project model){
        final ProjectDto projectDto = new ProjectDto();
        projectDto.name = model.name;
        projectDto.description = model.description;
        projectDto.id = model._id.toString();
        projectDto.dateCreated = model.dateCreated;
        projectDto.mutationSites = model.mutationSites;
        projectDto.sequence = model.wildSequence;
        projectDto.species = model.species;
        
        return projectDto;
    }
    public Project toModel() {
        Project project = new Project();
        project._id = (id == null) ? null : new ObjectId(id);
        project.name = name;
        project.dateCreated = dateCreated;
        project.description = description;
        project.mutationSites = mutationSites;
        project.wildSequence = sequence;
        project.species = species;
        return project;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public Date getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }        
}