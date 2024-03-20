package com.revproxy.model.loadbalancers;

import com.revproxy.model.ProxyDestination;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


// In this context, burst time means the number of units a process remains active.
// Since we're not dealing with processes but individual requests to destinations, we can consider that a request
// to a destination consumes a number of units (quantums) and we keep using the same destination until the "process"
// is exhausted (remainingBurstTime = 0). At that point, we roll to the next destination with fresh burst time.
//
// This architecture supports assigning different units depending on the scenario. For instance, a POST request
// with body may consume more quantums (because it's more resource intensive) and a downstream service with less
// resources may have less burst time (to stay less time as active). Also, all this settings may be configurable
// in the `rules.json` file.
//
// See constants `REQUEST_UNITS` and `DESTINATION_TOTAL_UNITS` below.

public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "round-robin";

    private static class DestinationProcess {

        private int remainingBurstTime;
        private final int initialBurstTime;

        private final ProxyDestination destination;

        public DestinationProcess(ProxyDestination destination, int remainingBurstTime) {
            this.destination = destination;
            this.initialBurstTime = remainingBurstTime;
            this.remainingBurstTime = remainingBurstTime;
        }

        public void execute(int quantum) {
            if (remainingBurstTime > quantum) {
                remainingBurstTime -= quantum;
            } else {
                remainingBurstTime = 0;
            }
        }

        public boolean isComplete() {
            return remainingBurstTime == 0;
        }

        public void reset() {
            this.remainingBurstTime = initialBurstTime;
        }
    }

    private static class DestinationScheduler {
        private final Queue<DestinationProcess> destinationsQueue;
        private final int quantum;

        private static final Object SCHEDULING_LOCK = new Object();

        public DestinationScheduler(int quantum) {
            this.quantum = quantum;
            this.destinationsQueue = new LinkedList<>();
        }

        public void addDestinationProcess(DestinationProcess process) {
            destinationsQueue.add(process);
        }

        public DestinationProcess schedule() {
            // Another undesirable lock. This is worse because it occurs on every request, so there are high chances
            // that this becomes a real bottleneck.
            synchronized (SCHEDULING_LOCK) {
                if (destinationsQueue.isEmpty()) {
                    return null;
                }
                var currentDestination = destinationsQueue.peek();

                currentDestination.execute(this.quantum);

                if (currentDestination.isComplete()) {
                    // exhausted, remove
                    destinationsQueue.poll();
                    // reset
                    currentDestination.reset();
                    // re-add
                    destinationsQueue.add(currentDestination);
                }

                return currentDestination;
            }
        }
    }

    private static final int REQUEST_UNITS = 1;
    private static final int DESTINATION_TOTAL_UNITS = 3;

    private DestinationScheduler scheduler;
    private static final Object CREATION_LOCK = new Object();

    @Setter @Getter
    private int requestUnits = REQUEST_UNITS;
    @Setter @Getter
    private int destinationTotalUnits =  DESTINATION_TOTAL_UNITS;

    @Override
    public ProxyDestination selectDestination(List<ProxyDestination> destinations) {
        // This is an undesirable lock, but not a global lock at least.
        // Since each rule has its own load balancer instance, only destinations within same rule will be locked here.
        // Good news is that this lock only occurs once per rule.
        synchronized (CREATION_LOCK) {
            if (scheduler == null) {
                this.scheduler = createScheduler(destinations);
            }
        }

        var process = this.scheduler.schedule();
        return process != null ? process.destination : null;
    }

    private DestinationScheduler createScheduler(List<ProxyDestination> destinations) {
        scheduler = new DestinationScheduler(requestUnits);

        destinations.stream()
                .map(d -> new DestinationProcess(d, destinationTotalUnits))
                .forEach(p -> scheduler.addDestinationProcess(p));

        return scheduler;
    }

}
