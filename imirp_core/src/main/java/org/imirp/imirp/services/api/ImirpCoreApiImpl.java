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

package org.imirp.imirp.services.api;

import javax.inject.Inject;

import org.imirp.imirp.ImirpContext;
import org.imirp.imirp.akka.GuiceInjectedActorProducer;
import org.imirp.imirp.akka.actor.MasterMutationActor;
import org.imirp.imirp.akka.messages.mutgen.MutateSequenceMsg;
import org.imirp.imirp.db.ImirpDataStore;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.google.inject.Injector;

public class ImirpCoreApiImpl implements ImirpCoreApi {
	public static final int MAX_ACTIVE_REQUESTS_PER_PROJECT = 1;
	private ActorSystem actorSystem;
	private Injector injector;
	private ImirpDataStore datastore;

	@Inject
	ImirpCoreApiImpl(ActorSystem actorSystem, Injector injector, ImirpDataStore datastore) {
		this.actorSystem = actorSystem;
		this.injector = injector;
		this.datastore = datastore;
	}

	@Override
	public void projectMutate(ImirpContext context) throws MutationRequestException {
		int activeRequestCount = datastore.getMutationRequestsForProject(context.projectId, false).size();
		if(activeRequestCount >= MAX_ACTIVE_REQUESTS_PER_PROJECT){
			throw new MutationRequestException("Too many mutation requests for this project. Wait until existing requests complete before making any additional ones.");
		}
		ActorRef masterActorRef = actorSystem.actorOf(Props.create(GuiceInjectedActorProducer.class, injector, MasterMutationActor.class));
		masterActorRef.tell(new MutateSequenceMsg(context), null);
	}

}
