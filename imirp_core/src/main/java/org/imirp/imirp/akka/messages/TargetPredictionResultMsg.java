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

package org.imirp.imirp.akka.messages;

import java.util.List;

import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.mutation.MutationContext;
import org.imirp.imirp.tsp.MirbaseScanner.ScanResult;

public class TargetPredictionResultMsg {
    private final ScanResult results;
    private final MutationContext context;

    public TargetPredictionResultMsg(MutationContext context, ScanResult results) {
        this.context = context;
        this.results = results;
    }

    public ScanResult getResults() {
        return results;
    }

    public MutationContext getContext() {
        return context;
    }

    public static class WildResultEliminatedMsg {
        public final List<TargetPredictionResult> filteredResults;
        public final boolean hadValidResults;
        public final MutationContext context;

        WildResultEliminatedMsg() {
            this.filteredResults = null;
            this.context = null;
            hadValidResults = false;
        }

        public WildResultEliminatedMsg(MutationContext context, List<TargetPredictionResult> filteredResults) {
            this.filteredResults = filteredResults;
            this.context = context;
            hadValidResults = true;
        }

        public boolean isHadValidResults() {
            return hadValidResults;
        }

    }

    public static class EmptyWildResultEliminatedMsg extends WildResultEliminatedMsg {
        public EmptyWildResultEliminatedMsg() {
            super();
        }
    }
}
