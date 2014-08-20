package org.imirp.imirp.akka.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;

import org.bson.types.ObjectId;
import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.MutationStrategy;
import org.imirp.imirp.akka.actor.MutationGenerationActor;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceFinishedMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceMsg;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceResultMsg;
import org.imirp.imirp.akka.messages.mutgen.StopRegionMutationsMsg;
import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.tsp.Species;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MutationGenerationActorTest {
    static Injector INJECTOR = Guice.createInjector(new AbstractModule(){
        @Override
        protected void configure() {
            // This test shouldn't actually need any components
        }        
    });
    
	HashSet<String> EXPECTED_MUTATIONS = new HashSet<>();
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		EXPECTED_MUTATIONS.clear();
	}

	static ActorSystem system;

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create("TestSystem");
	}

	@AfterClass
	public static void teardown() {
		JavaTestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testGGStrategy() {
		final String WILD_SEQUENCE = "AAAAAAAAAAAAAAAA";
		final int[][] MUTATION_SITES = {{0,3},{2,5}};
		EXPECTED_MUTATIONS.addAll(Arrays.asList(new String[]{
		        "GGAAGGAAAAAAAAAA",
		        "AAGGAAAAAAAAAAAA"
			})
		);		
		
		new JavaTestKit(system) {
			{
				final Props props = Props.create(MutationGenerationActor.class, INJECTOR);
				final ActorRef subject = system.actorOf(props);
				MultiSiteMutation msm = new MultiSiteMutation(WILD_SEQUENCE, MUTATION_SITES, null);
				send(subject, new MutateSequenceMsg(new ImirpContext(new ArrayList<TargetSiteType>(), new ObjectId(), new Species("mmu"), msm, new MutationStrategy(2, new Nucleotide[]{Nucleotide.GUANOSINE}), true)));
				MutateSequenceResultMsg msg;
				do{
					msg = expectMsgClass(duration("1 second"), MutateSequenceResultMsg.class);
					EXPECTED_MUTATIONS.remove(msg.context.mutantSequence);
				}while(!EXPECTED_MUTATIONS.isEmpty());
			}
		};
	}
	
	@Test
    public void testGTStrategy() {
        final String WILD_SEQUENCE = "AAAAAAAAAAAAAAAA";
        final int[][] MUTATION_SITES = {{0,3},{2,5}};
        EXPECTED_MUTATIONS.addAll(Arrays.asList(new String[]{
                "GGAAGGAAAAAAAAAA",
                "GGAAGTAAAAAAAAAA",
                "GGAATGAAAAAAAAAA",
                "GGAATTAAAAAAAAAA",
                "GTAAGGAAAAAAAAAA",
                "GTAAGTAAAAAAAAAA",
                "GTAATGAAAAAAAAAA",
                "GTAATTAAAAAAAAAA",
                "TGAAGGAAAAAAAAAA",
                "TGAAGTAAAAAAAAAA",
                "TGAATGAAAAAAAAAA",
                "TGAATTAAAAAAAAAA",
                "TTAAGGAAAAAAAAAA",
                "TTAAGTAAAAAAAAAA",
                "TTAATGAAAAAAAAAA",
                "TTAATTAAAAAAAAAA",
                "AAGGAAAAAAAAAAAA",
                "AAGTAAAAAAAAAAAA",
                "AATGAAAAAAAAAAAA",
                "AATTAAAAAAAAAAAA",
                "AAGGAAAAAAAAAAAA",
                "AAGTAAAAAAAAAAAA",
                "AATGAAAAAAAAAAAA",
                "AATTAAAAAAAAAAAA",
                "AAGGAAAAAAAAAAAA",
                "AAGTAAAAAAAAAAAA",
                "AATGAAAAAAAAAAAA",
                "AATTAAAAAAAAAAAA",
                "AAGGAAAAAAAAAAAA",
                "AAGTAAAAAAAAAAAA",
                "AATGAAAAAAAAAAAA",
                "AATTAAAAAAAAAAAA"
            })
        );      
        
        new JavaTestKit(system) {
            {
                final Props props = Props.create(MutationGenerationActor.class, INJECTOR);
                final ActorRef subject = system.actorOf(props);
                MultiSiteMutation msm = new MultiSiteMutation(WILD_SEQUENCE, MUTATION_SITES, null);
                send(subject, new MutateSequenceMsg(new ImirpContext(new ArrayList<TargetSiteType>(), new ObjectId(), new Species("mmu"), msm, new MutationStrategy(2, new Nucleotide[]{Nucleotide.GUANOSINE, Nucleotide.THYMIDINE}), true)));
                MutateSequenceResultMsg msg;
                do{
                    msg = expectMsgClass(duration("1 second"), MutateSequenceResultMsg.class);
                    EXPECTED_MUTATIONS.remove(msg.context.mutantSequence);
                }while(!EXPECTED_MUTATIONS.isEmpty());
            }
        };
    }
	
    @Test
    public void testStopMutations() {
	    // Make a big sequence so we can ensure it doesn't just complete normally
        final String WILD_SEQUENCE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        final int[][] MUTATION_SITES = { {0, 5}, {1, 6}, {7, 12}, {13, 18}, {19, 24}, {25, 30} }; // This complex region should take a loooooooong time
        
        new JavaTestKit(system) {
            {
                final Props props = Props.create(MutationGenerationActor.class, INJECTOR);
                final ActorRef subject = system.actorOf(props);
                MultiSiteMutation msm = new MultiSiteMutation(WILD_SEQUENCE, MUTATION_SITES, null);
                send(subject, new MutateSequenceMsg(new ImirpContext(new ArrayList<TargetSiteType>(), new ObjectId(), new Species("mmu"), msm, new MutationStrategy(2, new Nucleotide[]{ Nucleotide.GUANOSINE, Nucleotide.ADENOSINE, Nucleotide.CYTIDINE }), true)));
                expectMsgClass(duration("5 second"), MutateSequenceResultMsg.class);
                send(subject, new StopRegionMutationsMsg(msm.getStartRegion().getRegionId())); // Abort the region (we should only have 1)
                Object msg = null;
                int maxWaits = 100;
                do{
                    msg = expectMsgAnyClassOf(duration("50 millisecond"), MutateSequenceFinishedMsg.class, MutateSequenceResultMsg.class);
                }while(--maxWaits > 0 && msg instanceof MutateSequenceResultMsg);
                Assert.assertTrue(msg instanceof MutateSequenceFinishedMsg);
                expectNoMsg(duration("1 second"));
            }
        };
    }
    
    @Test
    public void testConsecutiveRepeatedMutationsStopEventually() {
        final String WILD_SEQUENCE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        final int[][] MUTATION_SITES = { {0, 5}, {1, 6}};
        
        new JavaTestKit(system) {
            {
                final Props props = Props.create(MutationGenerationActor.class, INJECTOR);
                final ActorRef subject = system.actorOf(props);
                MultiSiteMutation msm = new MultiSiteMutation(WILD_SEQUENCE, MUTATION_SITES, null);
                send(subject, new MutateSequenceMsg(new ImirpContext(new ArrayList<TargetSiteType>(), new ObjectId(), new Species("mmu"), msm, new MutationStrategy(2, new Nucleotide[]{ Nucleotide.GUANOSINE, Nucleotide.ADENOSINE, Nucleotide.CYTIDINE }), true)));
                expectMsgClass(duration("5 second"), MutateSequenceResultMsg.class);
                Object msg = null;
                int maxWaits = 100;
                do{
                    msg = expectMsgAnyClassOf(duration("50 millisecond"), MutateSequenceFinishedMsg.class, MutateSequenceResultMsg.class);
                }while(--maxWaits > 0 && msg instanceof MutateSequenceResultMsg);
                Assert.assertTrue(msg instanceof MutateSequenceFinishedMsg);
                expectNoMsg(duration("1 second"));
            }
        };
    }
	
}
