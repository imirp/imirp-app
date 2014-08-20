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

import java.util.HashMap;

import javax.inject.Inject;

import org.imirp.imirp.akka.GuiceInjectedActorProducer;
import org.imirp.imirp.akka.messages.RunMutantSequenceMsg;
import org.imirp.imirp.akka.messages.TargetPredictionResultMsg;
import org.imirp.imirp.akka.messages.mutgen.StopRegionPredictions;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;

import com.google.inject.Injector;

public class MasterTargetSitePredictor extends UntypedActor {
	final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	public static final String STOP_MSG = "STOP";
	public static final String START_MSG = "START";
	
	private HashMap<String, ActorRef> regionActors = new HashMap<>();
	private ActorRef supervisor;
	private Injector injector;

	@Inject
	MasterTargetSitePredictor(Injector injector){
		this.injector = injector;
		getContext().become(ready);
	}
	
	Procedure<Object> ready = new Procedure<Object>() {
        @Override
        public void apply(Object msg) {
        	if(msg.equals(START_MSG)){
        		supervisor = sender();
        		getContext().become(processing);
        	}else{
        		unhandled(msg);
        	}
        }
	};
	
	Procedure<Object> processing = new Procedure<Object>() {
        @Override
        public void apply(Object msg) {
        	if(msg instanceof RunMutantSequenceMsg){
        		RunMutantSequenceMsg runMsg = (RunMutantSequenceMsg)msg;
                sendRegionWork(runMsg);
            } else if(msg instanceof StopRegionPredictions){
            	StopRegionPredictions stopMsg = (StopRegionPredictions)msg;
            	stopRegionWork(stopMsg.regionId);
            } else if(msg instanceof TargetPredictionResultMsg) {
            	supervisor.tell(msg, self());
            } else if(msg.equals(STOP_MSG)){
            	// Stop region actors
            	for(String regionId : regionActors.keySet()){
            		getContext().stop(regionActors.get(regionId));
            	}
            	supervisor = null;
            	getContext().become(ready);
            } else {
                unhandled(msg);
            }
        }
	};
	
	@Override
	public void onReceive(Object msg) throws Exception {
		
	}
	
	private void stopRegionWork(String regionId){
		log.info("Stopping further target predictions for region[" + regionId + "]");
		ActorRef regionActor = regionActors.remove(regionId);
		if(regionActor != null){
			getContext().stop(regionActor);
		}
	}
	
	private void sendRegionWork(RunMutantSequenceMsg msg){
		String regionId = msg.getMutationContext().getRegionId();
		ActorRef regionActor = regionActors.get(regionId);
		if(regionActor == null){
			regionActor = getContext().actorOf(Props.create(GuiceInjectedActorProducer.class, injector, TargetSitePredictor.class));
			regionActors.put(regionId, regionActor);
		}
		regionActor.tell(msg, self());
	}	

}
