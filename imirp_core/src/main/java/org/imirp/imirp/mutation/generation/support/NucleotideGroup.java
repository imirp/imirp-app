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

import java.util.Arrays;

import org.imirp.imirp.data.Nucleotide;

/**
 * @author torben
 *
 */
public class NucleotideGroup {
	private Nucleotide[] nucGroup;
	
	public NucleotideGroup(Nucleotide[] nucGroup) {
		this.nucGroup = Arrays.copyOf(nucGroup, nucGroup.length);
	}

	public int getGroupSize() {
		return nucGroup.length;
	}

	boolean next() {
		for (int i = 0; i < nucGroup.length; i++) {
			if (nucGroup[i] != Nucleotide.last()) {
				nucGroup[i] = nucGroup[i].next();
				return true;
			}else if(i < nucGroup.length - 1){
				if(nucGroup[i + 1] != Nucleotide.last()){
					nucGroup[0] = Nucleotide.first();
					nucGroup[i + 1] = nucGroup[i + 1].next();
					return true;
				}
			}
		}

		return false;
	}
	
	public void setNucleotidesToType(Nucleotide type) {
		for(int i = 0; i < nucGroup.length; i++){
			nucGroup[i] = type;
		}
	}

	public Nucleotide[] getNucleotides() {
		return nucGroup;
	}

	/**
	 * Resets to A's
	 */
	NucleotideGroup reset() {
		for (int i = 0; i < nucGroup.length; i++) {
			nucGroup[i] = Nucleotide.first();
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nucGroup.length);
		for (Nucleotide mn : nucGroup) {
			sb.append(mn.toChar());
		}

		return sb.toString();
	}

	public void next(int numTimes) {
		for(int i = 0; i < numTimes; i++){
			next();
		}
	}
}