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

package org.imirp.imirp.mutation.generation;

import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.mutation.region.MutationRegion;

/**
 * @author twerner
 *
 */
public interface MutationGenerationStrategy {
	/**
	 * Generates the mutation possibilities given the {@link MultiSiteMutation} and notifies
	 * the caller via the {@link MutationCallback} callback.
	 * 
	 * @param msm mutation meta information
	 * @param callback called upon a successful mutation
	 */
	void generateMutations(MultiSiteMutation msm, MutationCallback callback);

    /**
     * Generates the mutation possibilities given the 
     * 
     * @param sequence
     * @param region
     * @param callback
     */
    void generateMutations(String sequence, MutationRegion region, MutationCallback callback);
    
    /**
     * Stops generating further mutations
     */
    void stop();
}
