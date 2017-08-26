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

package org.imirp.imirp.db;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.imirp.imirp.MutationStrategy;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.db.model.ImirpProjectMutationRequest;
import org.imirp.imirp.db.model.MutationResultSet;
import org.imirp.imirp.db.model.Project;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MongoDataStoreImpl implements ImirpDataStore {
	private static final Logger logger = Logger.getLogger(MongoDataStoreImpl.class);
	private static final String RESULTS_COL_NAME = "results";
	private static final String IMIRP_PROJECTS_COL_NAME = "projects";
	private static final String IMIRP_PROJECT_REQUESTS_COL_NAME = "project_requests";
	private final Jongo jongo;

	@Inject
	MongoDataStoreImpl(Jongo jongo){
		this.jongo = jongo;
	}
	
	@Override
	public void storeMutationResultSet(ObjectId projectId, String sequence, String regionId, List<TargetPredictionResult> results) {
		logger.debug("Storing " + results.size() + " results for sequence[" + sequence + "]");
		MongoCollection resultsCol = jongo.getCollection(RESULTS_COL_NAME);
		MutationResultSet mrs = new MutationResultSet(projectId, sequence, regionId, results);
		resultsCol.save(mrs);
	}

	@Override
	public long countValidResultsForRegion(ObjectId projectId, String regionId, TargetSiteType... invalidTypes) {
		MongoCollection resultsCol = jongo.getCollection(RESULTS_COL_NAME);
		// For the given region, are there any results that do not have the invalid types present
		long validResultCount = resultsCol.count("{projectId: #, regionId: #, results.type: {$not: {$in: #}}}", projectId, regionId, invalidTypes);
		return validResultCount;
	}

	@Override
	public boolean hasStrategyRunForProject(ObjectId projectId, MutationStrategy strategy) {
		MongoCollection requestsCol = jongo.getCollection(IMIRP_PROJECT_REQUESTS_COL_NAME);		
		return requestsCol.count("{strategy: #, projectId: #}", strategy, projectId) > 0;
	}
	
	public static class MutationStrategyPojo {
		public final MutationStrategy strat;

		public MutationStrategyPojo(MutationStrategy strat) {
			super();
			this.strat = strat;
		}
		
	}

	@Override
	public void saveMutationRequest(ImirpProjectMutationRequest request) {
		MongoCollection requestsCol = jongo.getCollection(IMIRP_PROJECT_REQUESTS_COL_NAME);
		requestsCol.insert(request);
	}
	
	@Override
	public List<ImirpProjectMutationRequest> getMutationRequestsForProject(ObjectId projectId, @Nullable Boolean completed) {
		MongoCollection requestsCol = jongo.getCollection(IMIRP_PROJECT_REQUESTS_COL_NAME);
		Iterable<ImirpProjectMutationRequest> results;
		if(completed != null){
			results = requestsCol.find("{projectId: #, dateCompleted: {$exists: #}}", projectId, completed).as(ImirpProjectMutationRequest.class);
		}else{
			results = requestsCol.find().as(ImirpProjectMutationRequest.class);
		}
		return Lists.newArrayList(results);
	}

	@Override
	public Iterable<MutationResultSet> getResultSets(ObjectId projectId, String regionId, int limit) {
		MongoCollection resultsCol = jongo.getCollection(RESULTS_COL_NAME);
		return resultsCol.find("{projectId: #, regionId: #}", projectId, regionId).limit(limit).as(MutationResultSet.class);
	}

    @Override
    public Iterable<ImirpProjectMutationRequest> getMutationRequestsForProject(ObjectId projectId) {
        MongoCollection requestsCol = jongo.getCollection(IMIRP_PROJECT_REQUESTS_COL_NAME);
        return requestsCol.find("{projectId: #}", projectId).as(ImirpProjectMutationRequest.class);
    }

	@Override
	public void completeMutationRequest(ObjectId currentMutationRequestId, long totalMutations, long totalValidMutations) {
		MongoCollection requestsCol = jongo.getCollection(IMIRP_PROJECT_REQUESTS_COL_NAME);
        requestsCol
        .update("{_id: #}", currentMutationRequestId)
        .with("{$set: {dateCompleted: #, totalMutationsGenerated: #, validMutationsGenerated: #}}", new Date(), totalMutations, totalValidMutations);
	}

    @Override
    public Project saveNewProject(Project newProject) {
        MongoCollection projectsCol = jongo.getCollection(IMIRP_PROJECTS_COL_NAME);
        assert newProject._id == null;
        assert newProject.dateCreated == null;
        newProject.dateCreated = new Date();
        projectsCol.save(newProject);
        return newProject;
    }

	@Override
	public Iterable<Project> getProjects(int page, int limit) {
		MongoCollection projectsCol = jongo.getCollection(IMIRP_PROJECTS_COL_NAME);
		return projectsCol.find().limit(limit).skip(page * limit).sort("{dateCreated:-1}").as(Project.class);
	}

	@Override
	public Project getProject(ObjectId projectId) {
		MongoCollection projectsCol = jongo.getCollection(IMIRP_PROJECTS_COL_NAME);
		return projectsCol.findOne(projectId).as(Project.class);
	}

	@Override
	public long countProjects() {
		MongoCollection projectsCol = jongo.getCollection(IMIRP_PROJECTS_COL_NAME);
		return projectsCol.count();
	}

}
