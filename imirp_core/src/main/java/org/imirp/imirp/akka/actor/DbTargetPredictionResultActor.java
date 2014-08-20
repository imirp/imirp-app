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

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.db.ImirpDataStore;

import akka.actor.UntypedActor;

/**
 * This actor basically just accepts a Pita result and stores it in the database
 * 
 * @author torben
 * 
 */
public class DbTargetPredictionResultActor extends UntypedActor {

    private ImirpDataStore resultStore;

    @Inject
    DbTargetPredictionResultActor(ImirpDataStore resultStore) {
        this.resultStore = resultStore;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof StoreResults) {
            StoreResults sr = (StoreResults) msg;
            // Synchronously store results
            resultStore.storeMutationResultSet(sr.projectId, sr.sequence, sr.regionId, sr.results);

            // And tell the sender we have finished
            sender().tell(new ResultStored(sr.requestId), self());
        } else {
            unhandled(msg);
        }
    }

    /**
     * A message which this actor understands as a request to store a set of Pita results
     */
    public static class StoreResults {
        public static final AtomicLong STORE_REQUEST_COUNTER = new AtomicLong(0);
        public final ObjectId projectId;
        public final List<TargetPredictionResult> results;
        public final String regionId;
        public final String sequence;
        public final long requestId = STORE_REQUEST_COUNTER.incrementAndGet();

        public StoreResults(ObjectId projectId, String regionId, String sequence, List<TargetPredictionResult> results) {
            this.projectId = projectId;
            this.regionId = regionId;
            this.sequence = sequence;
            this.results = results;
        }

        public List<TargetPredictionResult> getResults() {
            return results;
        }

        public String getRegionId() {
            return regionId;
        }

        public String getSequence() {
            return sequence;
        }

        public ObjectId getprojectId() {
            return projectId;
        }

    }

    /**
     * A confirmation message to notify a requester that a result has been stored
     */
    public static class ResultStored {
        public final long requestId;

        public ResultStored(long requestId) {
            super();
            this.requestId = requestId;
        }

        public long getRequestId() {
            return requestId;
        }

    }
}
