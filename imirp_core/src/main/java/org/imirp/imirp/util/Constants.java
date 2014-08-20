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

public final class Constants {
	public static final int UPSTREAM_FLANK_LENGTH = 8;
	public static final int MUTATION_LENGTH = 6;
	public static final int MIN_RESULTDATA_LENGTH = 7 * 2; // should at least have one character followed by a tab for each column
	public static final int TOTAL_MUTATIONS_POSSIBLE = (int)Math.pow(Nucleotide.values().length, MUTATION_LENGTH);
	public static final Nucleotide FIRST_NUCLEOTIDE = Nucleotide.values()[0];
	public static final Nucleotide LAST_NUCLEOTIDE = Nucleotide.values()[Nucleotide.values().length - 1];
}
