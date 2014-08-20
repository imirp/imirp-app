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

import java.util.List;

import javax.annotation.Nullable;

import org.bson.types.ObjectId;
import org.imirp.imirp.MutationStrategy;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.db.model.ImirpProjectMutationRequest;
import org.imirp.imirp.db.model.MutationResultSet;
import org.imirp.imirp.db.model.Project;

public interface ImirpDataStore {
	void saveMutationRequest(ImirpProjectMutationRequest requestItem);
	List<ImirpProjectMutationRequest> getMutationRequestsForProject(ObjectId projectId, @Nullable Boolean completed);
	Iterable<ImirpProjectMutationRequest> getMutationRequestsForProject(ObjectId projectId);
	void storeMutationResultSet(ObjectId projectId, String sequence, String regionId, List<TargetPredictionResult> results);
	long countValidResultsForRegion(ObjectId projectId, String regionId, TargetSiteType... invalidTypes);
	boolean hasStrategyRunForProject(ObjectId projectId, MutationStrategy strategy);
	Iterable<MutationResultSet> getResultSets(ObjectId projectId, String regionId, int limit);
    Project saveNewProject(Project newProject);
    Iterable<Project> getProjects(int page, int limit);
	Project getProject(ObjectId projectId);
	long countProjects();
	void completeMutationRequest(ObjectId currentMutationRequestId, long totalMutations, long totalValidMutations);
}
