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

package org.imirp.imirp.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Torben Werner
 *
 */
@JsonSerialize(using = NucleotideSerializer.class)
@JsonDeserialize(using = NucleotideDeserializer.class)
public enum Nucleotide {
	ADENOSINE('A'), CYTIDINE('C'), GUANOSINE('G'), THYMIDINE('T'), ;

	public final char nuc;

	Nucleotide(char nuc) {
		this.nuc = nuc;
	}

	public static Nucleotide[] readSequence(String sequence) {
		Nucleotide[] result = new Nucleotide[sequence.length()];
		for (int i = 0; i < sequence.length(); i++) {
			result[i] = getNucleotide(sequence.charAt(i));
		}

		return result;
	}

	public static Nucleotide[] createSequence(int length) {
		Nucleotide[] sequence = new Nucleotide[length];
		for (int i = 0; i < length; i++) {
			sequence[i] = first();
		}
		return sequence;
	}

	public static Nucleotide getNucleotide(char c) {
		for (Nucleotide n : Nucleotide.values()) {
			if (n.nuc == c) {
				return n;
			}
			// Handle alternative 'U' notation
			if(c == 'U'){
				return THYMIDINE;
			}
		}

		return null;
	}

	public Nucleotide next() {
		Nucleotide[] values = Nucleotide.values();
		return values[(ordinal() + 1) % values.length];
	}

	public static Nucleotide last() {
		Nucleotide[] values = Nucleotide.values();
		return values[(values.length - 1)];
	}

	public static Nucleotide first() {
		return Nucleotide.values()[0];
	}

	public char toChar() {
		return nuc;
	}

	@Override
	public String toString() {
		return toChar() + "";
	}
}
