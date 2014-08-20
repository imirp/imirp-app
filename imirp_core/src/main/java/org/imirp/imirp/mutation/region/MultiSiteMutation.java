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

package org.imirp.imirp.mutation.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.rule.MutationRule;
import org.imirp.imirp.mutation.rule.support.MutationRuleObserver;

/**
 * Contains meta information about a mutli-site mutation
 * 
 * @author Torben
 * 
 */
public class MultiSiteMutation implements MutationRuleObserver {
	private MutationRegion startRegion = null;
	private String sequence;
	private List<MutationRule> mutationRules;

	public MultiSiteMutation(String sequence, int[][] mutationSites, List<MutationRule> mutationRules) {
		this.sequence = sequence;
		this.mutationRules = mutationRules;
		ArrayList<MutationSite> mutSites = new ArrayList<MutationSite>();
		for (int[] site : mutationSites) {
			mutSites.add(new MutationSite(site));
		}
		initSites(mutSites);
	}
	
	public MultiSiteMutation(String sequence, Collection<MutationSite> mutationSites, List<MutationRule> mutationRules) {
		this.sequence = sequence;
		this.mutationRules = mutationRules;
		initSites(mutationSites);
	}
	
	public MultiSiteMutation(String sequence, MutationRegion startRegion, List<MutationRule> mutationRules) {
		this.sequence = sequence;
		this.mutationRules = mutationRules;
		this.startRegion = startRegion;
	}
	
	public Collection<MutationSite> getSites(){
	    Collection<MutationSite> sites = new ArrayList<>();
	    MutationRegion region = startRegion;
	    do{
	        sites.addAll(region.getMutationSites());
	    }while((region = region.getNextRegion()) != null);
	    return sites;
	}

	protected void initSites(Collection<MutationSite> mutationSites) {
		// Analyze list of mutation site indexes
		assert mutationSites.size() > 0;
		startRegion = new MutationRegionBuilder().addSites(mutationSites).build();
	}

	public MutationRegion getStartRegion() {
		return startRegion;
	}
	
	public Iterable<MutationRegion> getRegions(){
	    return new Iterable<MutationRegion>(){
            @Override
            public Iterator<MutationRegion> iterator() {
                return getRegionIterator();
            }	        
	    };
	}
	
	public Iterator<MutationRegion> getRegionIterator(){
	    return new Iterator<MutationRegion>(){
	        MutationRegion currentRegion = getStartRegion();
            @Override
            public boolean hasNext() {
                return currentRegion != null;
            }

            @Override
            public MutationRegion next() {
                final MutationRegion region = currentRegion;
                currentRegion = currentRegion.getNextRegion();
                return region;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
	    };
	}

	public String getSequence() {
		return sequence;
	}
	
	/**
	 * @param mutatedSequence
	 *            the new, mutated sequence
	 * @return true if the new, mutated sequence satisfies the multi-site mutation's rules, false otherwise
	 */
	public boolean doesMutationSatisfyRules(String mutatedSequence) {
		// Check each rule and if any rule fails then the mutation is not satisfactory
		for(MutationRule rule : mutationRules){
			if(!rule.checkRule(this, sequence, mutatedSequence)){
				return false;
			}
		}
		
		// All the rules have been satisfied, we are go!
		return true;
	}
	
	/**
	 * @return the list of mutation rules for this multi-site mutation
	 */
	public List<MutationRule> getMutationRules() {
		return mutationRules;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SEQ: ").append(sequence).append("\n");
		MutationRegion region = startRegion;
		while(region != null){
			sb.append("R").append(region.getRegionId()).append("[");
			sb.append(sequence.substring(region.getRegionStartIndex(), region.getRegionEndIndex()));
			sb.append("]\n");
			sb.append("Mutation Sites: [\n");
			for(MutationSite ms: region.getMutationSites()){
				sb.append("(").append(ms.getStartIndex()).append(",").append(ms.getEndIndex()).append(")=>(").append(sequence.substring(ms.getStartIndex(), ms.getEndIndex()+1)).append(")\n");
			}
			sb.append("]\n");
			region = region.getNextRegion();
		}
		sb.append("\n");		
		sb.append(startRegion);
		
		return sb.toString();
	}

	public static void main(String[] args) {
		String testSeq = "GTCAGCTAGCATGCTGACGAGCATGCATGCGCGACAGGTACTTCTCGAGCATGCATGCATGCTGACTGATCGATCGTAGCTAGCGTAGTCTAGCTAGCTAGCTACGTAGCTA";
		List<MutationSite> testSites = new ArrayList<MutationSite>();
		testSites.add(new MutationSite(0, 7));
		testSites.add(new MutationSite(6, 13));
		testSites.add(new MutationSite(16, 23));
		testSites.add(new MutationSite(32, 39));
		testSites.add(new MutationSite(50, 57));
		testSites.add(new MutationSite(52, 59));
		testSites.add(new MutationSite(80, 87));
		testSites.add(new MutationSite(92, 99));
		
		System.out.println(new MultiSiteMutation(testSeq, testSites, new ArrayList<MutationRule>()));
	}
}
