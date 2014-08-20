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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MirbaseNucleotideCounter {
	static final HashMap<Character, AtomicInteger> counts = new HashMap<>();
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Must specify a fasta file as an argument.");
		}

		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
		String line;
		while ((line = br.readLine()) != null) {
			// Ignore comment lines (denoted by the '>' character)
			if (line.startsWith(">")) {
				continue;
			}
			
			// Count the Nucleotide occurrences at indexes 1-7 (inclusive)
			for(int i = 1; i <= 7; i++){
				// Initialize the counter for this character, if necessary
				AtomicInteger charCounter = counts.get(line.charAt(i));
				if(charCounter == null){
					// Initialize to 0 because we will increment after
					charCounter = new AtomicInteger(0);
					counts.put(line.charAt(i), charCounter);
				}
				charCounter.incrementAndGet();
			}
		}
		br.close();
		System.out.println("Counts: \n");
		int total = 0;
		for(Character c : counts.keySet()){
			int thisNucCount = counts.get(c).get();
			System.out.println(c + ":" + thisNucCount);
			total += thisNucCount;
		}
		System.out.println("Total nucleotides counted: " + total);
	}
}
