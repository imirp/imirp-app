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

package controllers;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.imirp.imirp.db.ImirpDataStore;
import org.imirp.imirp.db.model.ImirpProjectMutationRequest;
import org.imirp.imirp.services.api.ImirpCoreApi;
import org.imirp.imirp.services.api.ImirpCoreApi.MutationRequestException;

import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.api.ApiService;
import services.api.ApiService.ImirpResultsDataDto;
import services.api.ApiService.ProjectsPageDto;
import services.api.ApiService.RegionMutantsDto;

import com.fasterxml.jackson.databind.JsonNode;

import dto.ProjectDto;
import dto.ProjectMutateConfigDto;

/**
 * 
 */
public class ApiController extends Controller {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ApiController.class);
    
	private final ImirpDataStore datastore;
	private final ApiService apiService;
	private final ImirpCoreApi coreApi;

	@Inject
    public ApiController(ApiService apiService, ImirpDataStore resultStore, ImirpCoreApi coreApi){
		this.apiService = apiService;
		this.datastore = resultStore;
		this.coreApi = coreApi;
    }

	public Promise<Result> getRegionMutants(final String projectId, final String regionId, final int limit){
		return Promise.promise(new Function0<JsonNode>(){
			@Override
			public JsonNode apply() throws Throwable {
				RegionMutantsDto results = apiService.getMutantsForRegion(new ObjectId(projectId), regionId, limit);
				return Json.toJson(results);
			}
		}).map(new Function<JsonNode, Result>(){
			@Override
			public Result apply(JsonNode json) throws Throwable {
				return ok(json);
			}			
		});
	}
	
	public Promise<Result> getResults(final String projectId, final int limit){
		return Promise.promise(new Function0<Result>(){
			@Override
			public Result apply() throws Throwable {
				final ObjectId projectOId = new ObjectId(projectId);
				Set<String> regionIds = apiService.getProjectRegionIds(projectOId);
				ImirpResultsDataDto results = apiService.getProjectResults(projectOId, regionIds, limit);
				return ok(Json.toJson(results));
			}			
		});
	}

    public Promise<Result> getProjectRequests(final String projectId, final @Nullable Boolean completed) {
    	return Promise.promise(new Function0<Result>(){
			@Override
			public Result apply() throws Throwable {
				ObjectId projectOId = new ObjectId(projectId);
				List<ImirpProjectMutationRequest> requests = datastore.getMutationRequestsForProject(projectOId, completed);
				return ok(Json.toJson(requests));
			}
		});
    }
    
    public Promise<Result> createProject(){
    	JsonNode requestJson = request().body().asJson();
    	final ProjectDto projectData = Json.fromJson(requestJson, ProjectDto.class);
        return Promise.promise(new Function0<Result>(){
			@Override
			public Result apply() throws Throwable {
				ProjectDto newProject = apiService.saveNewProject(projectData);
				return ok(Json.toJson(newProject));
			}
		});
    }
    
    public Promise<Result> projectMutate(){
    	JsonNode requestJson = request().body().asJson();
		final ProjectMutateConfigDto configDto = Json.fromJson(requestJson, ProjectMutateConfigDto.class);
    	return Promise.promise(new Function0<Result>(){
			@Override
			public Result apply() throws Throwable {
				try{
					coreApi.projectMutate(configDto.getImirpContext());
				}catch(MutationRequestException e){
					return badRequest(Json.toJson(new ResponseMessage(e.getMessage())));
				}
				return ok(Json.toJson(new ResponseMessage("Mutation request accepted.")));
			}
		});
    }
    
    public Promise<Result> getProjects(final int page, final int limit) {
    	return Promise.promise(new Function0<Result>(){
			@Override
			public Result apply() throws Throwable {
				ProjectsPageDto projects = apiService.getProjects(page, limit);
				return ok(Json.toJson(projects));
			}
		});
    }
    
    public static final class ResponseMessage implements Serializable {
		private static final long serialVersionUID = 7645951594143155359L;
		
		public String message;

		public ResponseMessage() {
			super();
		}

		public ResponseMessage(String message) {
			super();
			this.message = message;
		}
    	
    }
}
