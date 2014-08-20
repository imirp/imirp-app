package org.imirp.imirp.akka.actor;

import java.io.IOException;

import junit.framework.Assert;

import org.bson.types.ObjectId;
import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.akka.actor.WildResultEliminatorActor;
import org.imirp.imirp.akka.actor.WildResultEliminatorActor.EliminateWildResultMsg;
import org.imirp.imirp.akka.messages.TargetPredictionResultMsg.EmptyWildResultEliminatedMsg;
import org.imirp.imirp.akka.messages.TargetPredictionResultMsg.WildResultEliminatedMsg;
import org.imirp.imirp.data.SiteType;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.mutation.MutationContext;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.tsp.Mirbase;
import org.imirp.imirp.tsp.MirbaseScannerResultsUtil;
import org.imirp.imirp.tsp.MirbaseScannerResultsUtil.LoadedTargetResults;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;

public class WildResultEliminatorTest {
    private static final String WILD_SEQUENCE = "AGAGAGAAGGAGAGAGCATGTGATCGAGAGAGGAAATTGTGTTCACTCTGCCAATGACTATGTGGACACAGCAGTTGGGTATTCAGGAAAGAAAGAGAAATGGCGGTTAGAAGCACTTCACTTTGTAACTGTCCTGAACTGGAGCCCGGGAATGGACTAGAACCAAGGACCTTTGCGTACAGAAGGCACGGTATCAGTTGGAACAAATCTTCATTTTGGTATCCAAACTTTTATTCATTTTGGTGTATTATTTGTAAATGGGCATTGGTATGTTATAATGAAGAAAAGAACAACACAGGCTGTTGGATCGCGGATCTGTGTTGCTCATGTGGTTGTTTAAAGGAAACCATGATCGACAAGATTTGCCATGGATTTAAGAGTTTTATCAAGATATATCAAATACTTCTCCCCATCTGTTCATAGTTTATGGACTGATGTTCCAAGTTTGTATCATTCCTTTGCATATAATTGAACCTGGGACAACACACACTAGATATATGTAAAAACTATCTGTTGGTTTTCCAAAGGTTGTTAACAGATGAAGTTTATGTGCAAAAAAGGGTAAGATATGAATTCAAGGAGAAGTTGATAGCTAAAAGGTAGAGTGTGTCTTCGATATAATACAATTTGTTTTATGTCAAAATGTAAGTATTTGTCTTCCCTAGAAATCCTCAGAATGATTTCTATAATAAAGTTAATTTCATTTATATTTGACAAGAATACTCTATAGATGTTTTATACACATTTTCATGCAATCATTTGTTTCTTTCTTGGCCAGCAAAAGTTAATTGTTCTTAGATATAGCTGTATTACTGTTCACAGTCCAATCATTTTGTGCATCTAGAATTCATTCCTAATCAATTAAAAGTGCTTGCAAGAGTTTTAAACCTA";
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    static ActorSystem system;
    static Mirbase mirbase = new Mirbase();

    @BeforeClass
    public static void setup() throws IOException {
        system = ActorSystem.create();
        mirbase.load(WildResultEliminatorTest.class.getResourceAsStream("/com/torbinsky/imirp/testmature.fa"));
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testWildResultElimination_NoInvalidTargetSiteTypes() throws IOException {
        new JavaTestKit(system) {
            {                
                final Props props = Props.create(WildResultEliminatorActor.class, mirbase);
                final ActorRef subject = system.actorOf(props);
                // Load test results
                LoadedTargetResults loadedResults = MirbaseScannerResultsUtil.loadResults(getClass().getResourceAsStream("non_eliminated_results"));
                ImirpContext imirpContext = new ImirpContext(new TargetSiteType[]{}, new ObjectId(), loadedResults.species, new MultiSiteMutation(WILD_SEQUENCE, new int[][]{{0,1}}, null), null, loadedResults.allowGUWobble);
                MutationContext context = new MutationContext(imirpContext, loadedResults.sequence, null, 0L);
                EliminateWildResultMsg msg = new EliminateWildResultMsg(context, loadedResults.scanResult);
                // Tell actor to eliminate the wild results
                send(subject, msg);
                // When we get a response, we need to load the expected results and compare them
                WildResultEliminatedMsg eliminated = expectMsgClass(duration("2 second"), WildResultEliminatedMsg.class);
                loadedResults = MirbaseScannerResultsUtil.loadResults(getClass().getResourceAsStream("wild_eliminated_results"));
                // Make sure we get the same number
                Assert.assertEquals(loadedResults.scanResult.predictions.size(), eliminated.filteredResults.size());
                // Scan each predicted target and make sure it exists in the list of loaded targets
                for(TargetPredictionResult tpr : eliminated.filteredResults){
                    Assert.assertTrue("Could not find predicted target site[" + tpr + "]", MirbaseScannerResultsUtil.checkTargetSiteExists(tpr, loadedResults.scanResult.predictions));
                }
            }
        };
    }
    
    @Test
    public void testWildResultElimination_NoPerfect6Mer() throws IOException {
        new JavaTestKit(system) {
            {
                final Props props = Props.create(WildResultEliminatorActor.class, mirbase);
                final ActorRef subject = system.actorOf(props);
                // Load test results
                LoadedTargetResults loadedResults = MirbaseScannerResultsUtil.loadResults(getClass().getResourceAsStream("non_eliminated_results"));
                ImirpContext imirpContext = new ImirpContext(new TargetSiteType[]{new TargetSiteType(SiteType.SIX_MER, false)}, new ObjectId(), loadedResults.species, new MultiSiteMutation(WILD_SEQUENCE, new int[][]{{0,1}}, null), null, loadedResults.allowGUWobble);
                MutationContext context = new MutationContext(imirpContext, loadedResults.sequence, null, 0L);
                EliminateWildResultMsg msg = new EliminateWildResultMsg(context, loadedResults.scanResult);
                // Tell actor to eliminate the wild results
                send(subject, msg);
                // Exepct an "Empty" response which indicates an invalid site type was found
                EmptyWildResultEliminatedMsg result = expectMsgClass(duration("2 second"), EmptyWildResultEliminatedMsg.class);
                Assert.assertFalse("Expected no valid results because an invalid type was present.", result.isHadValidResults());
            }
        };
    }
}
