package org.imirp.imirp.mutation.generation;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.generation.RandomSequentialChangesMutationStrategyImpl;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.region.MutationRegionBuilder;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.junit.Test;

public class RandomSequentialChangesMutationStrategyImplTests {
    MutationRegion testRegion = null;
    
    @Test
    public void testCancel() throws InterruptedException {
        final RandomSequentialChangesMutationStrategyImpl strategy = new RandomSequentialChangesMutationStrategyImpl(2, new Nucleotide[] { Nucleotide.GUANOSINE, Nucleotide.ADENOSINE, Nucleotide.CYTIDINE });
        initData(new int[] { 0, 5, 1, 6, 7, 12, 13, 18, 19, 24, 25, 30 }); // make it complex so we know cancellation has to work
        final CountDownLatch mutationsStartedLatch = new CountDownLatch(1);
        final CountDownLatch cancelledLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                strategy.generateMutations("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", testRegion, new MutationCallback() {
                    @Override
                    protected void actOnSequence(String mutatedSequence, String regionId) {
                        mutationsStartedLatch.countDown();
                    }
                    @Override
                    protected void onStop(long totalMutations) {
                        cancelledLatch.countDown();
                    }
                });
            }
        }).start();
        
        // Wait to allow mutations to occur
        mutationsStartedLatch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue("Mutations should start generating before we try cancelling.", mutationsStartedLatch.getCount() == 0);
        
        // Cancel the mutations and wait a little bit for that to occur
        strategy.stop();
        cancelledLatch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue("We should have received a cancelled notification in our callback.", cancelledLatch.getCount() == 0);
    }
    
    @Test
    public void testSmallSiteEventuallyGetsAllPermutations() throws InterruptedException {
        final RandomSequentialChangesMutationStrategyImpl strategy = new RandomSequentialChangesMutationStrategyImpl(2, new Nucleotide[] { Nucleotide.GUANOSINE });
        initData(new int[] { 0, 5});
        final Set<String> possibleMutations = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        possibleMutations.add("GGAAAAAAAAAAA");
        possibleMutations.add("AGGAAAAAAAAAA");
        possibleMutations.add("AAGGAAAAAAAAA");
        possibleMutations.add("AAAGGAAAAAAAA");
        possibleMutations.add("AAAAGGAAAAAAA");
        new Thread(new Runnable() {
            @Override
            public void run() {
                strategy.generateMutations("AAAAAAAAAAAAA", testRegion, new MutationCallback() {
                    @Override
                    protected void actOnSequence(String mutatedSequence, String regionId) {
                        possibleMutations.remove(mutatedSequence);
                    }
                });
            }
        }).start();
        int maxSleeps = 1000; // 1000 x 10 ms = 10s maximum wait for this test
        do{
            Thread.sleep(10);
        }while(possibleMutations.size() > 0 && --maxSleeps > 0);
        Assert.assertTrue("Expected all possible permutations.", possibleMutations.size() ==0);
    }

    public void initData(int... siteIndexes) {
        MutationRegionBuilder builder = new MutationRegionBuilder();
        for (int i = 0; i < siteIndexes.length; i += 2) {
            builder.addSites(new MutationSite(siteIndexes[i], siteIndexes[i + 1]));
        }
        testRegion = builder.build();
    }
}
