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

public class NaiveMutationRule implements MutationRule {

	private int minDifferences;

	public NaiveMutationRule(int minDifferences){
		this.minDifferences = minDifferences;		
	}
	
	@Override
	public boolean checkRule(MutationRuleObserver observer, String wildSequence, String mutantSequence) {
		return checkRule(wildSequence, mutantSequence, minDifferences);
	}
	public static boolean checkRule(String wildSequence, String mutantSequence, int minDifferences){
		int differences = 0;
		int i = 0;
		do{
			if(wildSequence.charAt(i) != mutantSequence.charAt(i)){
				differences++;
			}
			i++;
		}while(differences < minDifferences && i < wildSequence.length());
		
		return differences >= minDifferences;
	}

}
