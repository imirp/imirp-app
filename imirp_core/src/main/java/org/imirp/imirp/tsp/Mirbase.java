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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.imirp.imirp.data.Mirna;

/**
 * Loads a FASTA formatted data set containing known miRNA's for different species
 * 
 * @author torben
 * 
 */
public class Mirbase {
	private static Logger logger = Logger.getLogger(Mirbase.class);

	private Map<Species, List<Mirna>> mirsets = new ConcurrentHashMap<>(); // make it threadsafe :)

	public Mirbase() {
	}

	public void addMirset(String description, String mirna) {
		// The species identification should be the first 3 characters following the '>' comment character
		String[] descriptionTokens = description.split("\\s");
		Species species = new Species(descriptionTokens[0].substring(1, 4), descriptionTokens[2], descriptionTokens[3]);
		List<Mirna> mirnas = mirsets.get(species);
		// Initialize list of the Micro RNA's for this species if necessary
		if (mirnas == null) {
			mirnas = new ArrayList<>();
			mirsets.put(species, mirnas);
		}
		// We only want the 5' portion of the miRNA
		mirnas.add(new Mirna(description, mirna.substring(0, 8).replaceAll("U", "T")));
	}
	
	public void load(InputStream input) throws IOException {
		readFile(new BufferedReader(new InputStreamReader(input)));
	}

	public void load(String filepath) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) { // J7 fanciness :)
			readFile(br);
		}
		logger.info("Loaded " + mirsets.size() + " species from miRNA dataset " + filepath);
	}

	private void readFile(BufferedReader br) throws IOException {
		String description, mirna;
		while ((description = br.readLine()) != null && (mirna = br.readLine()) != null) {
			// The description line has to start with a '>' comment char
			if (!description.startsWith(">") || description.length() < 4) {
				throw new IllegalArgumentException("Unrecognized description line in mirbase input");
			}
			// The mirna line MUST be at least 8 characters long
			if (mirna.length() < 8) {
				throw new IllegalArgumentException("Micro RNA[" + mirna + "] is too short in mirbase input");
			}
			addMirset(description, mirna);
		}
	}

	public @Nonnull	Set<Species> getSpecies() {
		return mirsets.keySet();
	}
	
	public @Nullable Species getSpeciesById(String id) {
		for(Species species : getSpecies()){
			if(species.getId().equals(id)){
				return species;
			}
		}
		return null;
	}

	public List<Mirna> getMirnaFivePrimes(Species species) {
		List<Mirna> fivePrimes = mirsets.get(species);
		return fivePrimes == null ? Collections.<Mirna>emptyList() : fivePrimes;
	}

}
