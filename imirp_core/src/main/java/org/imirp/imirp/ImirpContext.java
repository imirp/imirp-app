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

package org.imirp.imirp;

import java.util.Collection;

import org.bson.types.ObjectId;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.tsp.Species;

import com.google.common.collect.Lists;

public class ImirpContext {

    public final Collection<TargetSiteType> invalidSiteTypes;
    public final ObjectId projectId;
    public final Species species;
    public final MultiSiteMutation mutParams;
    public final MutationStrategy strategy;
    public final boolean allowGUWobble;
    public final String wildSequence;
    
    public ImirpContext(TargetSiteType[] invalidSiteTypes, ObjectId projectId, Species species, MultiSiteMutation mutParams, MutationStrategy strategy, boolean allowGUWobble) {
        this(Lists.newArrayList(invalidSiteTypes), projectId, species, mutParams, strategy, allowGUWobble);
    }
    public ImirpContext(Collection<TargetSiteType> invalidSiteTypes, ObjectId projectId, Species species, MultiSiteMutation mutParams, MutationStrategy strategy, boolean allowGUWobble) {
        super();
        this.invalidSiteTypes = invalidSiteTypes;
        this.projectId = projectId;
        this.species = species;
        this.mutParams = mutParams;
        this.strategy = strategy;
        this.allowGUWobble = allowGUWobble;
        this.wildSequence = mutParams.getSequence();
    }

    public ImirpContext(ImirpContext copyFrom) {
        this(copyFrom.invalidSiteTypes, copyFrom.projectId, copyFrom.species, copyFrom.mutParams, copyFrom.strategy, copyFrom.allowGUWobble);
    }

    @Override
    public String toString() {
        return new StringBuilder("MSM{").append("strategy:").append(strategy).append(",gu:").append(allowGUWobble).append("}").toString();
    }

}
