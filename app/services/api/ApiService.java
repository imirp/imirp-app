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

package services.api;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bson.types.ObjectId;
import org.imirp.imirp.db.model.ImirpProjectMutationRequest;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.tsp.Species;

import play.libs.Json;
import dto.ProjectDto;
import dto.ProjectSummaryDto;

public interface ApiService {
	
	ImirpResultsDataDto getProjectResults(ObjectId projectId, Set<String> regionIds, int limitPerRegion);
	RegionMutantsDto getMutantsForRegion(ObjectId projectId, String regionId, int limit);
	/**
	 * 
     * @param newProject the data for the new project
     * @return an updated copy of the project that was saved
     */
    ProjectDto saveNewProject(ProjectDto newProject);
    ProjectsPageDto getProjects(int page, int limit);
	Set<Species> getAvailableSpecies();
	ProjectDto getProject(ObjectId projectId);
	Iterable<MutationRegion> getProjectMutationRegions(ObjectId projectId);
	Set<String> getProjectRegionIds(ObjectId projectId);
	long getProjectCount();
	ProjectSummaryDto getProjectSummary(ObjectId projectOId);
	File createMutantAnalysisZip(ObjectId projectOid, String mutantSequence) throws IOException;
	List<ImirpProjectMutationRequest> getActiveRequestsForProject(ObjectId projectId);	
	List<ImirpProjectMutationRequest> getRequestsForProject(ObjectId projectId, @Nullable Boolean completed);
	
	public static final class ProjectsPageDto {
		public List<ProjectDto> projects;
		public int pageNum;
		public int totalPages;
		public long totalProjects;
		public ProjectsPageDto() {
		}
		public ProjectsPageDto(List<ProjectDto> projects, int pageNum, int totalPages, long totalProjects) {
			this.projects = projects;
			this.pageNum = pageNum;
			this.totalPages = totalPages;
			this.totalProjects = totalProjects;
		}
	}
    
    public static final class ImirpResultsDataDto implements Serializable {
		private static final long serialVersionUID = 2731269592106196682L;
		
    	public List<RegionDetailsDto> regions = new ArrayList<>();
    	public Map<String, RegionMutantsDto> regionMutants = new HashMap<>();
    	public Collection<ImirpProjectMutationRequest> completedRequests;
    	public Collection<ImirpProjectMutationRequest> activeRequests;
    	public ProjectDto project;
    	public boolean hasResults = false;
    	
		public ImirpResultsDataDto() {}
		
		public ImirpResultsDataDto(ProjectDto project, String wildSequence, List<RegionDetailsDto> regions) {
			this.project = project;
			this.regions = regions;
		}
		
		public void addRegion(RegionDetailsDto region){
			if(regions == null){
			    regions = new ArrayList<>();
			}
			regions.add(region);
		}
		
		public void addRegionMutants(String regionId, RegionMutantsDto mutants){
			if(mutants.size() > 0){
				hasResults = true;
			}
			if(!regionMutants.containsKey(regionId)){
				regionMutants.put(regionId, mutants);				
			}else{
				regionMutants.get(regionId).addAll(mutants);
			}
        }
		
		@Override
		public String toString(){
		    return Json.toJson(this).toString();
		}
    }
    
    public static final class RegionDetailsDto implements Serializable {
		private static final long serialVersionUID = -8014344208834605435L;
		public String regionId;
        public int regionStart;
        public int regionEnd;
        public Collection<MutationSite> sites;
        
        public RegionDetailsDto(){}
        
        public void setSites(Collection<MutationSite> mSites){
            this.sites = mSites;
        }
    }
    
    public static final class RegionMutantsDto extends ArrayList<MutantDto> {
		private static final long serialVersionUID = -2600901299137493558L;

		public RegionMutantsDto(){}    	
    }
    
    public static final class MutantDto implements Serializable {
		private static final long serialVersionUID = -4277358596877914622L;
		public String sequence;
        public String id;
        public MutantDto() {}
        public MutantDto(String sequence, String id) {
            this.sequence = sequence;
            this.id = id;
        }        
    }

	File createTargetScanCSV(String sequence, Species species) throws IOException;

}
