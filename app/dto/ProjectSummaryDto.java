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

import org.imirp.imirp.db.model.ImirpProjectMutationRequest;

public class ProjectSummaryDto implements Serializable {
	private static final long serialVersionUID = 5898542493012555424L;
	public ProjectDto project;
	public Collection<ImirpProjectMutationRequest> requests;
	
	public ProjectSummaryDto() {
		super();
	}

	public ProjectSummaryDto(ProjectDto project) {
		super();
		this.project = project;
	}

}
