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

import java.util.Set;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.imirp.imirp.tsp.Species;

import com.fasterxml.jackson.databind.JsonNode;

import play.Routes;
import play.data.DynamicForm;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.api.ApiService;
import services.api.ApiService.ImirpResultsDataDto;
import services.api.ApiService.ProjectsPageDto;
import dto.ProjectDto;
import dto.ProjectMutateConfigDto;
import dto.ProjectSummaryDto;

/**
 * 
 */
public class ViewController extends Controller {

	private ApiService apiService;

	@Inject
	public ViewController(ApiService apiService) {
		this.apiService = apiService;
	}

	public Result index() {		
		return ok("TODO: Implement new index page.");
	}

	public Promise<Result> retrieveProjects() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				return ok("TODO: Implement retrieve project page");
			}
		});
	}
	
	public Promise<Result> viewProject(final String projectId) {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				final ObjectId projectOId = new ObjectId(projectId);
				ProjectSummaryDto dto = apiService.getProjectSummary(projectOId);
				String jsInitData = Json.toJson(dto).toString();
				return ok(views.html.projectoverview.render(dto.project, jsInitData));
			}
		});
	}

	public Promise<Result> viewResults(final String projectId, final int limit) {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				final ObjectId projectOId = new ObjectId(projectId);
				Set<String> regionIds = apiService.getProjectRegionIds(projectOId);
				ImirpResultsDataDto results = apiService.getProjectResults(projectOId, regionIds, limit);
				return ok(views.html.projectresults.render(results.project, results.toString()));
			}
		});
	}

	public Promise<Result> projectMutateSetup(final String projectId) {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				ObjectId projectOid = new ObjectId(projectId);
				ProjectDto project = apiService.getProject(projectOid);
				ProjectMutatePageDto projectDto = new ProjectMutatePageDto(project);
				return ok(views.html.projectmutate.render(project, Json.toJson(projectDto).toString()));
			}
		});
	}
	
	public Promise<Result> analyzeMutant(final String projectId) {
		DynamicForm form = DynamicForm.form().bindFromRequest();
		final String mutantSequence = form.get("mutantSequence");
		final ObjectId projectOid = new ObjectId(projectId);
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {				
				response().setContentType("application/x-download");  
				response().setHeader("Content-disposition","attachment; filename=mutant_analysis.zip");
				return ok(apiService.createMutantAnalysisZip(projectOid, mutantSequence));
			}
		});
	}

	public Promise<Result> createProject() {
		ProjectCreatePageDto createPageDto = new ProjectCreatePageDto(apiService.getAvailableSpecies());
		return Promise.pure((Result) ok(views.html.projectcreate.render(Json.toJson(createPageDto).toString())));
	}
	public Promise<Result> visualizeSequence() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				return ok(views.html.tools.visualizer.render());
			}
		});
	}
	
	public Result about() {
		return ok(views.html.about.render());
	}
	
	public Promise<Result> targetPredict() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				
				return ok(views.html.tools.targetscanner.render(Json.toJson(apiService.getAvailableSpecies()).toString()));
			}
		});
	}
	
	public Promise<Result> targetPredictSubmit() {
		DynamicForm form = DynamicForm.form().bindFromRequest();
		final Species species = Json.fromJson(Json.parse(form.get("species")), Species.class);
		final String sequence = form.get("sequence");;
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				response().setContentType("application/x-download");  
				response().setHeader("Content-disposition","attachment; filename=results.csv");
				return ok(apiService.createTargetScanCSV(sequence, species));
			}			
		});
	}

	public Result jsRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter(
				"appRoutes", // appRoutes will be the JS object available in our view
				routes.javascript.ApiController.createProject(), routes.javascript.ApiController.getProjectRequests(), routes.javascript.ApiController.getRegionMutants(),
				routes.javascript.ApiController.projectMutate(), routes.javascript.ViewController.createProject(), routes.javascript.ViewController.projectMutateSetup(),
				routes.javascript.ViewController.viewResults(), 
				routes.javascript.ViewController.viewProject(), routes.javascript.ViewController.analyzeMutant(), routes.javascript.ApiController.getResults(),
				routes.javascript.ViewController.visualizeSequence(), routes.javascript.ViewController.targetPredict(), routes.javascript.ViewController.targetPredictSubmit()));
	}

	public static final class ProjectCreatePageDto {
		public Set<Species> species;
		public ProjectCreatePageDto() {
			super();
		}
		public ProjectCreatePageDto(Set<Species> species) {
			super();
			this.species = species;
		}		
	}
	
	public static final class ProjectMutatePageDto {		
		public ProjectMutateConfigDto config = new ProjectMutateConfigDto();

		public ProjectMutatePageDto(ProjectDto project) {
			super();
			config.project = project;
		}
	}
	
}
