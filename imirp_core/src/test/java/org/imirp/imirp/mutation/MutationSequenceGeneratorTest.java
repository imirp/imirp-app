package org.imirp.imirp.mutation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.util.MutantSequenceGenerator;
import org.junit.Test;

public class MutationSequenceGeneratorTest {
	
	@Test
	public void testGeneratorSingleNucleotide(){
		final AtomicInteger ai = new AtomicInteger(0);
		MutantSequenceGenerator.mutateAndOperate("A", 0, 0, "0", new MutationCallback(){
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {
				ai.incrementAndGet();
			}			
		});
		
		Assert.assertEquals(4, ai.get());
	}
	
	@Test
	public void testGeneratorFourT(){
		final AtomicInteger ai = new AtomicInteger(0);
		MutantSequenceGenerator.mutateAndOperate("TTTT", 0, 3, "0", new MutationCallback(){
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {
				ai.incrementAndGet();
			}			
		});
		
		Assert.assertEquals((int)Math.pow(4, 4), ai.get());
	}
	
	@Test
	public void testSpecificSequencesAppear(){
		final AtomicInteger ai = new AtomicInteger(0);
		final AtomicBoolean aga = new AtomicBoolean(false);
		final AtomicBoolean act = new AtomicBoolean(false);
		MutantSequenceGenerator.processMutation(
				new Nucleotide[]{Nucleotide.ADENOSINE,Nucleotide.ADENOSINE,Nucleotide.ADENOSINE}, 
				new Nucleotide[]{Nucleotide.ADENOSINE,Nucleotide.GUANOSINE,Nucleotide.THYMIDINE}, 
				"0",
				new MutationCallback(){

			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {
				if(mutatedSequence.equals("AGA")){
					aga.set(true);
				}else if(mutatedSequence.equals("ACT")){
					act.set(true);
				}
				ai.incrementAndGet();
			}
			
		}, 0);
		
		Assert.assertTrue(aga.get());
		Assert.assertTrue(act.get());
		Assert.assertEquals(12, ai.get());
	}
}
