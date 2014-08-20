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

package org.imirp.imirp.mutation;

import java.util.concurrent.atomic.AtomicLong;

import org.imirp.imirp.data.Nucleotide;

public abstract class MutationCallback {
	protected AtomicLong counter = new AtomicLong(0);
	protected String sequenceChunkBeforeMutation = "";
	protected String sequenceChunkAfterMutation = "";

	public MutationCallback() {}
	
	public void actOnMNSequence(Nucleotide[] mnSequence, String regionId){
		StringBuffer sb = new StringBuffer(sequenceChunkBeforeMutation);
		for(Nucleotide mn : mnSequence){
			sb.append(mn.toChar());
		}
		sb.append(sequenceChunkAfterMutation);
		actOn(sb.toString(), regionId);
	}
	
	public void actOn(String mutatedSequence, String regionId){
		counter.incrementAndGet();
		actOnSequence(mutatedSequence, regionId);
	}

	protected abstract void actOnSequence(String mutatedSequence, String regionId);
	
	public void stop(){
        onStop(counter.get());
    }

	protected void onStop(long totalMutations){}

	public void setSequenceChunkBeforeMutation(String sequenceChunkBeforeMutation) {
		this.sequenceChunkBeforeMutation = sequenceChunkBeforeMutation;
	}

	public void setSequenceChunkAfterMutation(String sequenceChunkAfterMutation) {
		this.sequenceChunkAfterMutation = sequenceChunkAfterMutation;
	}

}