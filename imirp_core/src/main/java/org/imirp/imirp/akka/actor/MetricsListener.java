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

//#metrics-listener
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.ClusterMetricsChanged;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.NodeMetrics;
import akka.cluster.StandardMetrics;
import akka.cluster.StandardMetrics.Cpu;
import akka.cluster.StandardMetrics.HeapMemory;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MetricsListener extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	Cluster cluster = Cluster.get(getContext().system());

	// subscribe to ClusterMetricsChanged
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterMetricsChanged.class);
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	@Override
	public void onReceive(Object message) {
		if (message instanceof ClusterMetricsChanged) {
			ClusterMetricsChanged clusterMetrics = (ClusterMetricsChanged) message;
			for (NodeMetrics nodeMetrics : clusterMetrics.getNodeMetrics()) {
				if (nodeMetrics.address().equals(cluster.selfAddress())) {
					logHeap(nodeMetrics);
					logCpu(nodeMetrics);
				}
			}

		} else if (message instanceof CurrentClusterState) {
			// ignore

		} else {
			unhandled(message);
		}
	}

	void logHeap(NodeMetrics nodeMetrics) {
		HeapMemory heap = StandardMetrics.extractHeapMemory(nodeMetrics);
		if (heap != null) {
			log.info("Used heap: {} MB", ((double) heap.used()) / 1024 / 1024);
		}
	}

	void logCpu(NodeMetrics nodeMetrics) {
		Cpu cpu = StandardMetrics.extractCpu(nodeMetrics);
		if (cpu != null && cpu.systemLoadAverage().isDefined()) {
			log.info("Load: {} ({} processors)", cpu.systemLoadAverage().get(), cpu.processors());
		}
	}

}
// #metrics-listener