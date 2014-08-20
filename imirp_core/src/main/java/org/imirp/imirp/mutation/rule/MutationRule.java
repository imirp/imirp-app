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

package org.imirp.imirp.mutation.rule;

import org.imirp.imirp.mutation.rule.support.MutationRuleObserver;

/**
 * A rule for mutations which checks whether a particular mutation meets the specified criteria
 * 
 * @author Torben
 *
 */
public interface MutationRule {
	/**
	 * Checks whether the mutation satisfies this rule
	 * 
	 * @param observer the observer of this rule
	 * @param wildSequence the original, wild sequence
	 * @param mutantSequence a mutation of the wild sequence
	 * @return true if the mutated sequence satisfies this rule, false otherwise
	 */
	boolean checkRule(MutationRuleObserver observer, String wildSequence, String mutantSequence);
}
