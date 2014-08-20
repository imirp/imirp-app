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

package org.imirp.imirp.util;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationCallback;

/**
 * Generates mutant sequences from a given sequence
 * 
 * @author Torben Werner
 * 
 */
public class MutantSequenceGenerator {
	/**
	 * @param sequence
	 * @param mutationStartIndex
	 * @param mutationEndIndex
	 * @param mutationAction
	 */
	public static void mutateAndOperate(String sequence, int mutationStartIndex, int mutationEndIndex, String regionId,  MutationCallback mutationAction) {
		mutationAction.setSequenceChunkBeforeMutation(sequence.substring(0, mutationStartIndex));
		mutationAction.setSequenceChunkAfterMutation(sequence.substring(mutationEndIndex + 1, sequence.length()));
		
		processMaxMutations(createRepeatedSequence(Constants.FIRST_NUCLEOTIDE, mutationEndIndex - mutationStartIndex + 1), regionId, mutationAction);
	}

	public static void processMaxMutations(Nucleotide[] startSequence, String regionId, MutationCallback mutationAction) {
		processMutation(startSequence, createRepeatedSequence(Constants.LAST_NUCLEOTIDE, startSequence.length), regionId, mutationAction, 0);
	}

	public static void processMutation(Nucleotide[] startSequence, Nucleotide[] endSequence, String regionId, MutationCallback mutationAction, int mutIdx) {
		// Check for base condition
		if (mutIdx == startSequence.length) {
			mutationAction.actOnMNSequence(startSequence, regionId);
			return;
		}

		// We haven't hit our base condition yet, more recursion!
		for (Nucleotide mn : Nucleotide.values()) {
			if (mn.ordinal() <= endSequence[mutIdx].ordinal()) {
				startSequence[mutIdx] = mn;
				processMutation(startSequence, endSequence, regionId, mutationAction, mutIdx + 1);
			}
		}
	}

	private static Nucleotide[] createRepeatedSequence(Nucleotide nuc, int mutationLength) {
		Nucleotide[] mutation = new Nucleotide[mutationLength];
		for (int i = 0; i < mutationLength; i++) {
			mutation[i] = nuc;
		}
		return mutation;
	}
}
