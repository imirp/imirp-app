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

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.imirp.imirp.data.TargetPredictionResult;
import org.jongo.marshall.jackson.oid.Id;

public class MutationResultSet {
    ObjectId projectId;
	String sequence;
	List<TargetPredictionResult> results;
	Date dateRun = new Date();
	String regionId;
	@Id
	ObjectId id;

	MutationResultSet() {}

	public MutationResultSet(ObjectId projectId, String sequence, String regionId, List<TargetPredictionResult> results, Date dateRun) {
		super();
		this.projectId = projectId;
		this.regionId = regionId;
		this.sequence = sequence;
		this.results = results;
		this.dateRun = dateRun;
	}

	public MutationResultSet(ObjectId projectId, String sequence, String regionId, List<TargetPredictionResult> results) {
		super();
		this.projectId = projectId;
		this.regionId = regionId;
		this.sequence = sequence;
		this.results = results;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public List<TargetPredictionResult> getResults() {
		return results;
	}

	public void setResults(List<TargetPredictionResult> results) {
		this.results = results;
	}

	public Date getDateRun() {
		return dateRun;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public void setDateRun(Date dateRun) {
		this.dateRun = dateRun;
	}

	public ObjectId getProjectId() {
        return projectId;
    }

    public void setProjectId(ObjectId projectId) {
        this.projectId = projectId;
    }

    public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

}
