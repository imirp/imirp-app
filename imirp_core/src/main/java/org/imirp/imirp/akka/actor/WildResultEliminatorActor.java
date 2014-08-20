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

package org.imirp.imirp.akka.actor;

import java.io.IOException;

import javax.inject.Inject;

import org.imirp.imirp.akka.messages.TargetPredictionResultMsg.EmptyWildResultEliminatedMsg;
import org.imirp.imirp.akka.messages.TargetPredictionResultMsg.WildResultEliminatedMsg;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.mutation.MutationContext;
import org.imirp.imirp.tsp.Mirbase;
import org.imirp.imirp.tsp.MirbaseScanner;
import org.imirp.imirp.tsp.Species;
import org.imirp.imirp.tsp.MirbaseScanner.ScanResult;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 
 * 
 * @author torben
 *
 */
public class WildResultEliminatorActor extends UntypedActor {
	@SuppressWarnings("unused")
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private ScanResult wildResults = null;
	private String wildSequence = null;
	private Boolean allowGUWobble = null;
	private Mirbase mirbase;
	private MirbaseScanner targetFinder;
	
	@Inject
	public WildResultEliminatorActor(Mirbase mirbase) {
		super();
		this.mirbase = mirbase;
		targetFinder = new MirbaseScanner();
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if(msg instanceof EliminateWildResultMsg){
			EliminateWildResultMsg procMsg = (EliminateWildResultMsg) msg;
			initializeWildResults(procMsg.context.species, procMsg.context.wildSequence, procMsg.context.allowGUWobble);
			// No new, invalid types were generated, so now we can just filter out the wild ones
			ScanResult filteredResult = MirbaseScanner.removeResults(procMsg.result, wildResults);
			for(TargetSiteType tst : procMsg.context.invalidSiteTypes){
			    // If we have any NEW (i.e. unfiltered) invalid target site types, then we can just stop now and send a sort've empty reply
			    if(filteredResult.counts.get(tst) != null){
			        sender().tell(new EmptyWildResultEliminatedMsg(), self());
			        return;
			    }
			}
			// Send a Pita result back that has the filtered results (no wild ones)
			sender().tell(new WildResultEliminatedMsg(procMsg.context, filteredResult.predictions), self());
		}
	}
	
	/**
	 * Initializes the list of wild results if they haven't already been or if the parameters for computing them have changed.
	 */
	private void initializeWildResults(Species species, String wildSequence, boolean allowGUWobble) throws IOException {
		boolean wildSame = this.wildSequence != null && this.wildSequence.equals(wildSequence);
		boolean wobblesSame = this.allowGUWobble != null && this.allowGUWobble == allowGUWobble;
		// Compute the wild results if we haven't already or if the parameters have changed
		if(wildResults == null || (!wildSame || !wobblesSame)){
			wildResults = targetFinder.findPredictedTargets(mirbase, species, wildSequence, allowGUWobble);
			// Sort them lexicographically based on mRNA name so we can search the list more efficiently
			this.wildSequence = wildSequence;
			this.allowGUWobble = allowGUWobble;
		}
	}

	public static class EliminateWildResultMsg {
	    public final MutationContext context;
		public final ScanResult result;
		
		public EliminateWildResultMsg(MutationContext context, ScanResult result) {
			super();
            this.context = context;
            this.result = result;
		}
		
	}
}
