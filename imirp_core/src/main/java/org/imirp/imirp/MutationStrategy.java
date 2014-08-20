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

import java.io.Serializable;

import org.imirp.imirp.data.Nucleotide;

public class MutationStrategy implements Serializable {
	private static final long serialVersionUID = 3298591084801L;
    public final int changes;
    public final Nucleotide[] nucleotides;
    
	public MutationStrategy() {
		this(0, new Nucleotide[]{});
	}

	public MutationStrategy(int changes, Nucleotide[] nucleotides) {
		super();
		this.changes = changes;
		this.nucleotides = nucleotides;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{changes:");
		sb.append(changes);
		sb.append(", nucs:");
		for(Nucleotide nuc : nucleotides){
			sb.append(nuc.toString());
			sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}
}