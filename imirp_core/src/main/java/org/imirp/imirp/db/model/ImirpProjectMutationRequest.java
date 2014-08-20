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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.bson.types.ObjectId;
import org.imirp.imirp.MutationStrategy;
import org.imirp.imirp.data.TargetSiteType;
import org.jongo.marshall.jackson.oid.Id;

public class ImirpProjectMutationRequest implements Serializable {
	private static final long serialVersionUID = -3627023238965373955L;
	
	@Id
	public ObjectId id;
	public Collection<TargetSiteType> invalidSiteTypes;
	public ObjectId projectId;	
	public MutationStrategy strategy;
	public boolean allowGUWobble;
	public Date dateRequested = new Date();
	public long totalMutationsGenerated = 0L;
	public long validMutationsGenerated = 0L;
	public Date dateCompleted;

	public ImirpProjectMutationRequest() {
	}

	public ImirpProjectMutationRequest(Collection<TargetSiteType> invalidSiteTypes, ObjectId projectId, MutationStrategy strategy, boolean allowGUWobble) {
		super();
		this.invalidSiteTypes = invalidSiteTypes;
		this.projectId = projectId;
		this.strategy = strategy;
		this.allowGUWobble = allowGUWobble;
	}

	public Collection<TargetSiteType> getInvalidSiteTypes() {
		return invalidSiteTypes;
	}

	public void setInvalidSiteTypes(Collection<TargetSiteType> invalidSiteTypes) {
		this.invalidSiteTypes = invalidSiteTypes;
	}

	public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getProjectId() {
        return projectId;
    }

    public void setProjectId(ObjectId projectId) {
        this.projectId = projectId;
    }

	public MutationStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(MutationStrategy strategy) {
		this.strategy = strategy;
	}

	public boolean isAllowGUWobble() {
		return allowGUWobble;
	}

	public void setAllowGUWobble(boolean allowGUWobble) {
		this.allowGUWobble = allowGUWobble;
	}
}
