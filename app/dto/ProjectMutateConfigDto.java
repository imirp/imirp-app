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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.MutationStrategy;
import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.data.SiteType;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.mutation.region.MultiSiteMutation;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class ProjectMutateConfigDto implements Serializable {
	private static final long serialVersionUID = 892398190189871L;
	
	public static final Set<TargetSiteType> DEFAULT_INVALID_SITE_TYPES = new HashSet<>();
	static {
		DEFAULT_INVALID_SITE_TYPES.add(new TargetSiteType(SiteType.EIGHT_MER, false));
		DEFAULT_INVALID_SITE_TYPES.add(new TargetSiteType(SiteType.SEVEN_MER_M8, false));
		DEFAULT_INVALID_SITE_TYPES.add(new TargetSiteType(SiteType.SEVEN_MER_A1, false));
	};
	public static final Set<TargetSiteType> DEFAULT_VALID_SITE_TYPES = new HashSet<>();
	static {
		// Add all possibilities
		Collections.addAll(DEFAULT_VALID_SITE_TYPES, TargetSiteType.values());
		// Remove the ones we put in INVALID
		DEFAULT_VALID_SITE_TYPES.removeAll(DEFAULT_INVALID_SITE_TYPES);
	};
	
	public ProjectDto project;
	public Set<TargetSiteType> invalidSiteTypes;
	public Set<TargetSiteType> validSiteTypes;
	public MutationStrategy strategy;

	public ProjectMutateConfigDto() {
		this(null);
	}

	public ProjectMutateConfigDto(String projectId) {
		super();
		invalidSiteTypes = DEFAULT_INVALID_SITE_TYPES;
		validSiteTypes = DEFAULT_VALID_SITE_TYPES;
		strategy = new MutationStrategy(2, new Nucleotide[] { Nucleotide.GUANOSINE });
	}

	public MutationStrategy getStrategy() {
		return strategy;
	}

	@JsonIgnore
	public ImirpContext getImirpContext() {
		MultiSiteMutation msm = new MultiSiteMutation(project.sequence, project.mutationSites, null);
		return new ImirpContext(invalidSiteTypes, new ObjectId(project.id), project.species, msm, strategy, true);
	}
}