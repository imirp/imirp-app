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

package org.imirp.imirp.tsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.imirp.imirp.data.Mirna;
import org.imirp.imirp.data.SiteType;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.data.TargetSiteType;

public class MirbaseScanner {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MirbaseScanner.class);

	public MirbaseScanner() {

	}
	
	public ScanResult findPredictedTargets(Mirbase mirbase, Species species, String inputSequence, boolean allowGUWobble) {
	    ScanResult result = new ScanResult();
		List<Mirna> mirnaFivePrimes = mirbase.getMirnaFivePrimes(species);
		for (Mirna mirna : mirnaFivePrimes) {
		    result.merge(matchSiteTypes(mirna.getMirna(), inputSequence, mirna.getFivePrime(), allowGUWobble));
		}

		return result;
	}

	/**
	 * Example:
	 * 
	 * @param mirna
	 * @param sequence
	 * @param fivePrime
	 * @param allowGUWobble
	 * @return
	 */
	private ScanResult matchSiteTypes(String mirna, String sequence, String fivePrime, boolean allowGUWobble) {
	    ScanResult predictions = new ScanResult();
		int begin = 0, end = 7, seqPos, fivePrimePos;
		boolean guWobbleFound; // Remember if we've already found a GU Wobble
		PairMatch matchResult;
		
		// Advances a sliding window of 8 nucleotides that get comapred to the fiveprime miR sequence
		do{
			guWobbleFound = false;
			seqPos = begin;
			fivePrimePos = 7;
			matchResult = isPairMatch(sequence.charAt(seqPos++), fivePrime.charAt(fivePrimePos--), allowGUWobble && !guWobbleFound);
			guWobbleFound |= matchResult.guWobbleMatched;
			boolean targetSitePredicted = true;
			// The first nucleotide match is important because it immediately let's us know 
			if(matchResult.isMatch){
				// We might have an OS-6mer, a 7mer-m8 or an 8mer match at this point
				/*
				 *  Check SEQ positions 2-6 (not 0-indexed).
				 *  For these positions, no target sites are identified, but rather if we don't have a pairmatch then we know no target site is possible
				 */
				for(int i = 0; i < 5; i++){
					matchResult = isPairMatch(sequence.charAt(seqPos++), fivePrime.charAt(fivePrimePos--), allowGUWobble && !guWobbleFound);
					guWobbleFound |= matchResult.guWobbleMatched;
					if(!matchResult.isMatch){
						// no matches possible at this point, move on in the sequence
						targetSitePredicted = false;
						break;
					}
				}
				// If we get to this state then we know some type of target site is present, we just have to find which one by checking the last few nucleotides
				if(targetSitePredicted){
					matchResult = isPairMatch(sequence.charAt(seqPos++), fivePrime.charAt(fivePrimePos--), allowGUWobble && !guWobbleFound);
					guWobbleFound |= matchResult.guWobbleMatched;
					if(matchResult.isMatch){
						// This means we either have a 7mer-m8 or an 8mer and need to check the last nuclotide to determine which
						// If the last nucleotide is an 'A', then it's an 8mer, otherwise, 7mer-m8
						if(sequence.charAt(seqPos) == 'A'){
							predictions.addResult(new TargetPredictionResult(mirna, begin, SiteType.EIGHT_MER, guWobbleFound));
						}else{
						    predictions.addResult(new TargetPredictionResult(mirna, begin, SiteType.SEVEN_MER_M8, guWobbleFound));
						}
					}else{
						// This means we have an OS-6mer, no further checks needed
						predictions.addResult(new TargetPredictionResult(mirna, begin, SiteType.OFFSET_SIX_MER, guWobbleFound));
					}
				}
			}else{
				// We might have a 6mer or a 7mer-A1 match at this point
				/*
				 *  Check SEQ positions 2-7 (not 0-indexed).
				 *  For these positions, no target sites are identified, but rather if we don't have a pairmatch then we know no target site is possible
				 */
				for(int i = 0; i < 6; i++){
					matchResult = isPairMatch(sequence.charAt(seqPos++), fivePrime.charAt(fivePrimePos--), allowGUWobble && !guWobbleFound);
					guWobbleFound |= matchResult.guWobbleMatched;
					if(!matchResult.isMatch){
						// no matches possible at this point, move on in the sequence
						targetSitePredicted = false;
						break;
					}
				}
				// If we get to this state then we know some type of target site is present, we just have to find which one by checking the last few nucleotides
				if(targetSitePredicted){
					// This means we either have a 7mer-m8 or an 8mer and need to check the last nuclotide to determine which
					// If the last nucleotide is an 'A', then it's a 7mer-A1, otherwise it's a 6mer
					// Note: For 6mer/7mer-a1 the position is offset by 1 since the first nucleotide in the sequence group of 8 doesn't match
					if(sequence.charAt(seqPos) == 'A'){
						predictions.addResult(new TargetPredictionResult(mirna, begin+1, SiteType.SEVEN_MER_A1, guWobbleFound));
					}else{
						predictions.addResult(new TargetPredictionResult(mirna, begin+1, SiteType.SIX_MER, guWobbleFound));
					}
				}
			}
			begin++;
		}while(++end < (sequence.length() - 1));
		
		return predictions;
	}
	
	/**
	 * Scans the last 6 nucleotides in the current 8-nucleotide sliding window of the input sequence.
	 * 
	 * Example:
	 * 
	 * - Input Sequence: A[AA{AAAAAA}]AAAAAAA
	 *                       ^      ^
	 *                       begin  end
	 * So the site's sliding window is designated by the region surrounded by '[' and the OS-6mer window is surrounded by '{'
	 * 
	 * @param sequence the current input sequence that we are scanning
	 * @param begin the beginning of a 6 mer window at the end of an 8 nucleotide window
	 * @param end the end of a 6 mer window at the end of an 8 nucleotide window
	 * @param fivePrime the 8 nucleotide 5' sequence
	 * @param allowGUWobble whether we allow GU Wobbles
	 * @return a Match instance if OS-6mer was present, null if not  
	 */
	private @Nullable TargetPredictionResult scanForOffsetSixMer(String mirna, String sequence, int begin, int end, String fivePrime, boolean allowGUWobble){
		int fivePrimePos = 7, sequencePos = begin-2; // Start at the end of the five prime
		boolean guWobbleFound = false, osSixMerPossible = false;
		do{
			PairMatch pairMatch = isPairMatch(sequence.charAt(sequencePos), fivePrime.charAt(fivePrimePos), allowGUWobble && !guWobbleFound);
			osSixMerPossible &= pairMatch.isMatch;
			// Only update whether GU wobble was found if we had a match
			if(osSixMerPossible){
				// This shouldn't happen more than once because after we have one GU wobble matched we won't allow a match
				// with another GU wobble
				guWobbleFound = pairMatch.guWobbleMatched;
			}else{
				// Return null to signify no OS-6mer present
				return null;
			}
			fivePrimePos--;
		}while(osSixMerPossible && (++sequencePos < end-2));
		// If we reach this point, we have found an OS-6MER
		return new TargetPredictionResult(mirna, begin - 2, SiteType.OFFSET_SIX_MER, guWobbleFound); // the position is the site's sliding window start 
	}

	/**
	 * Checks if the two nucleotides are a pair match.
	 * 
	 * @return PairMatch which will tell whether a match was found and if it was a GU Wobble match
	 */
	private PairMatch isPairMatch(char nucleotide, char fivePrimeNucleotide, boolean allowGUWobble) {
		switch (nucleotide) {
		case 'A':
			// A can pair with U
			switch (fivePrimeNucleotide) {
			case 'T':
				return new PairMatch(true, false);
			}
			break;
		case 'C':
			// C can pair with G
			switch (fivePrimeNucleotide) {
			case 'G':
				return new PairMatch(true, false);
			}
			break;
		case 'G':
			// G can pair with C and sometimes U (if GU wobble)
			switch (fivePrimeNucleotide) {
			case 'C':
				return new PairMatch(true, false);
			case 'T':
				return new PairMatch(allowGUWobble, true);
			}
			break;
		case 'T':
			// U can pair with A or sometimes G (if GU wobble)
			switch (fivePrimeNucleotide) {
			case 'A':
				return new PairMatch(true, false);
			case 'G':
				return new PairMatch(allowGUWobble, true);
			}
			break;
		default:
			throw new IllegalArgumentException("Unhandled nucleotide[" + nucleotide + "]!");
		}

		return new PairMatch(false, false);
	}
	
	public static class ScanResult {
	    public final Map<TargetSiteType, Integer> counts;
	    public final List<TargetPredictionResult> predictions;
        public ScanResult() {
            super();
            counts = new HashMap<>();
            predictions = new ArrayList<>();
        }
        
        public ScanResult(List<TargetPredictionResult> predictions) {
            counts = new HashMap<>();
            for(TargetPredictionResult prediction : predictions){
                addCount(prediction);
            }
            this.predictions = predictions;
        }
	    
        public void merge(ScanResult other) {
            for(TargetSiteType type : other.counts.keySet()){
                counts.put(type, getIncrementedCount(type, other.counts.get(type)));
            }
            predictions.addAll(other.predictions);
        }

        public void addResult(TargetPredictionResult result){
            addCount(result);
            predictions.add(result);
        }

        private void addCount(TargetPredictionResult result) {
            counts.put(result.type, getIncrementedCount(result.type, 1));
        }
        
        private int getIncrementedCount(TargetSiteType type, int incr){
            Integer count = counts.get(type);
            return (count == null) ? incr : count + incr;            
        }
	}
	
	/**
	 * Used to specify whether a pair match matched and if it matched using GU wobble
	 * 
	 * @author torben
	 *
	 */
	private static class PairMatch{
		private boolean isMatch;
		private boolean guWobbleMatched;

		public PairMatch(boolean isMatch, boolean guWobbleMatched){
			this.isMatch = isMatch;
			this.guWobbleMatched = guWobbleMatched;			
		}
	}

	/**
	 * Returns only the results that are not contained in removeResults
	 */
	public static ScanResult removeResults(ScanResult mutantResults, ScanResult removeResults) {
		// Copy the mutant results (we don't want mutable state)
		ArrayList<TargetPredictionResult> filteredResults = new ArrayList<TargetPredictionResult>(mutantResults.predictions);
        // Remove the results (this depends on how the Result equals/hashcode methods work!)
        filteredResults.removeAll(removeResults.predictions);
		return new ScanResult(filteredResults);
	}

}
