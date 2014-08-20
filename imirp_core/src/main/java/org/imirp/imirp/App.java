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

import java.util.List;
import java.util.UUID;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.db.ImirpDataStore;
import org.imirp.imirp.db.model.Project;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.services.api.ImirpCoreApi;
import org.imirp.imirp.services.api.ImirpCoreApi.MutationRequestException;
import org.imirp.imirp.tsp.Species;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;


/**
 * 
 * 
 */
public class App {
	                                       
	public static final String SEQUENCE = "AGAGAGAAGGAGAGAGCATGTGATCGAGAGAGGAAATTGTGTTCACTCTGCCAATGACTATGTGGACACAGCAGTTGGGTATTCAGGAAAGAAAGAGAAATGGCGGTTAGAAGCACTTCACTTTGTAACTGTCCTGAACTGGAGCCCGGGAATGGACTAGAACCAAGGACCTTTGCGTACAGAAGGCACGGTATCAGTTGGAACAAATCTTCATTTTGGTATCCAAACTTTTATTCATTTTGGTGTATTATTTGTAAATGGGCATTGGTATGTTATAATGAAGAAAAGAACAACACAGGCTGTTGGATCGCGGATCTGTGTTGCTCATGTGGTTGTTTAAAGGAAACCATGATCGACAAGATTTGCCATGGATTTAAGAGTTTTATCAAGATATATCAAATACTTCTCCCCATCTGTTCATAGTTTATGGACTGATGTTCCAAGTTTGTATCATTCCTTTGCATATAATTGAACCTGGGACAACACACACTAGATATATGTAAAAACTATCTGTTGGTTTTCCAAAGGTTGTTAACAGATGAAGTTTATGTGCAAAAAAGGGTAAGATATGAATTCAAGGAGAAGTTGATAGCTAAAAGGTAGAGTGTGTCTTCGATATAATACAATTTGTTTTATGTCAAAATGTAAGTATTTGTCTTCCCTAGAAATCCTCAGAATGATTTCTATAATAAAGTTAATTTCATTTATATTTGACAAGAATACTCTATAGATGTTTTATACACATTTTCATGCAATCATTTGTTTCTTTCTTGGCCAGCAAAAGTTAATTGTTCTTAGATATAGCTGTATTACTGTTCACAGTCCAATCATTTTGTGCATCTAGAATTCATTCCTAATCAATTAAAAGTGCTTGCAAGAGTTTTAAACCTA";
	static final String INVALID_TARGET_PREDICTIONS_PROPERTY = "imirp.results.invalid_target_predictions";
	
	static final int[][] MUTATION_SITES = new int[][] {
		{111, 116}, {112, 117}, {168, 173}, {200, 205}, {242, 247}, {245, 250}, {260, 265}, {267, 272}, {287, 292}, {379, 384}, {391, 396}, {445, 450}, {461, 466}, {517, 522}, {519, 524}, {521, 526}, {545, 550}, 
		{547, 552}, {550, 555}, {551, 557}, {604, 609}, {607, 612}, {626, 631}, {632, 637}, {647, 652}, {655, 660}, {665, 670}, {668, 673}, {693, 695}, {697, 702}, {703, 708}, {704, 709}, {708, 713}, {719, 724}, 
		{731, 736}, {734, 739}, {736, 741}, {763, 768}, {810, 815}, {811, 816}, {834, 839}, {842, 847}
	};

    static TargetSiteType[] loadInvalidSiteTypes(Config config) {
        List<String> invalidTargetPredictions = config.getStringList(INVALID_TARGET_PREDICTIONS_PROPERTY);
        TargetSiteType[] invalidPredictions = new TargetSiteType[invalidTargetPredictions.size()];
        for(int i = 0; i < invalidPredictions.length; i++){
            invalidPredictions[i] = new TargetSiteType(invalidTargetPredictions.get(i));
        }
        return invalidPredictions;
    }
	
	public static void main(String[] args) throws MutationRequestException {
		// Start system
		Injector injector = Guice.createInjector(new _ImirpCoreModule());
		Config config = injector.getInstance(Config.class);
		ImirpDataStore datastore = injector.getInstance(ImirpDataStore.class);
		MultiSiteMutation msm = new MultiSiteMutation(SEQUENCE, MUTATION_SITES, null);
		Project newProject = new Project("Imirp Project - " + UUID.randomUUID(), "", SEQUENCE, msm.getSites(), new Species("mmu"));
		datastore.saveNewProject(newProject);
		TargetSiteType[] invalidSiteTypes = loadInvalidSiteTypes(config);
		injector.getInstance(ImirpCoreApi.class).projectMutate(
						new ImirpContext(
					        invalidSiteTypes,
					        newProject._id,
					        newProject.species,
							msm,
							new MutationStrategy(2, new Nucleotide[]{Nucleotide.GUANOSINE}),
							true
					));
	}
}
