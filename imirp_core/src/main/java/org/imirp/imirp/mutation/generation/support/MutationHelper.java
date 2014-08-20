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

package org.imirp.imirp.mutation.generation.support;

import java.util.List;

import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.rule.AbstractIndependentRegionRule;

public class MutationHelper {
    public String originalSequence;
    public MutationRegion region;
    public List<MutationSite> sites;
    public int[] groupStartPositions;
    public MutationCallback callback;
    public AbstractIndependentRegionRule filter;
}