# RevProxy

# Features
  - Forward incoming requests to a set of downstream services
  - Supports configuration of rules: incoming host -> downstream services
  - Supports timeout configuration per rule
  - Supports retries configuration per rule
  - Supports several load balancing strategies: random and round robin
  - Supports docker, docker-compose and kubernetes deployment

# Stack
  - Java 21.
  - Gradle.
  - Springboot.
  - Webflux: as reactive networking framework.
  - Docker, docker compose and Docker registry (see https://hub.docker.com/repository/docker/josemanuelnavarro/revproxy/general)
  - Helm.
  - Junit 4 and [Lambda Behave](https://richardwarburton.github.io/lambda-behave/) for specs testing.
  - [Vegeta](https://github.com/tsenart/vegeta) for load testing.

# Thread-based vs Event-based Servers
I started the project as a regular Springboot server, but very early in the development I realized that a reverse proxy requires a level of concurrency much higher than other servers.
Because of that, I changed the approach from a thread-based server (the regular one in Spring world, using Tomcat or similar) to an event-based one (using Webflux as framework and Netty as app server).

The main benefits of this reactive approach are:

1. __Performance and Efficiency__
 - **Non-blocking I/O operations**: traditional threaded models, like the ones used with Tomcat, block the thread while waiting for I/O operations (network, disk, database calls) to complete. In contrast, an event-driven approach with WebFlux and Netty uses non-blocking I/O, allowing a single thread to handle multiple connections simultaneously using an event loop. This model reduces the overhead associated with context switching and thread management, leading to higher throughput and lower latency.
 - **Event-loop mechanism**: Netty employs an event loop mechanism that efficiently manages and dispatches events to be processed. This allows the reverse proxy to handle a higher number of simultaneous connections with a small fixed number of threads, reducing the overhead of thread management and optimizing CPU usage.

2. __Scalability__
 - **Horizontal and vertical scalability**: the non-blocking and event-driven nature of WebFlux and Netty enables better utilization of existing hardware, allowing for more efficient vertical scaling. Additionally, the lightweight nature of handling connections makes it easier to scale horizontally across multiple instances, as the overhead per connection is significantly reduced.
 - **Backpressure handling**: reactive streams in WebFlux support backpressure, a mechanism that allows consumers to control and contain the flow of requests to prevent the server to be overloaded and to become unresponsive. This feature is crucial for reverse proxies, which often act as intermediaries between clients and services, to maintain stability under high workloads.

3. __Resilience__
 - **Built-in support for resilience patterns**: the reactive stack encourages and simplifies the implementation of resilience patterns such as retries and timeouts. These patterns are essential for maintaining the uptime and reliability of a reverse proxy, ensuring that it can gracefully handle failures in downstream services.
 - **Error handling and recovery**: the reactive paradigm provides sophisticated mechanisms for error handling and recovery, allowing for errors to be managed in a non-blocking manner and at the right level of abstraction. This capability is essential for a reverse proxy, which must deal with a variety of failures without impacting client requests.

4. __Better Integration with Modern Technologies__
- **Real-time data processing**: WebFlux and Netty are particularly well-suited for scenarios requiring real-time data processing and streaming. For a reverse proxy, this capability can be leveraged to implement features like real-time monitoring, logging, and analytics with minimal impact on performance.
- **Microservices and cloud-native environments**: The reactive stack aligns well with the principles of microservices and cloud-native architectures, offering better resilience, elasticity, and message-driven communication. This alignment ensures that the reverse proxy can effectively operate in dynamic, distributed environments.

# Round-robin implementation
To implement a simple round-robin algorithm it would be enough to use a circular queue. The strategy always takes the destination in the top, and then adds it again to the tail.

However, this naive approach doesn't work well when:
  - The latency and capacity of each downstream service is different (one service is slow and low capacity, but the other is fast is high capacity)
  - The requests sent are very different and the resources needed to process them are different (one type of request is very lightweight, other is computing or memory intensive)

In this context, it's better to have some mechanism to tweak the priorities of the round robin algorithm so it's able to assign more processing time to specific services.
Besides that, it's interesting to assign different weights to each requests, so a heavy requests consumes more resources in the service and the next requests will be redirected to different services. 

To implement this mechanism, I defined *burst time* as the number of units a downstream service (process) remains in the top of the queue.
Since we're not dealing with processes but individual requests to destinations, we can consider that a request
to a destination consumes a number of units (*quantums*) and we keep using the same destination until the "process" (destination)
is exhausted (remainingBurstTime = 0). At that point, we roll to the next destination with fresh burst time.

This architecture supports assigning different units depending on the scenario. For instance, a POST request
with body may consume more quantums (because it's more resource intensive) and a downstream service with less
resources may have less burst time (to stay less time as active). Also, all these settings may be configurable
in the `rules.json` file.

# Functional Coding style
As you can see in the code, the preferred programming style used was functional and reactive.

I used this to follow the patters encouraged by the WebFlux framework, but I understand that this style can mean a steep learning curve to some programmers and in the context of a team, I wouldn't take this decisions lightly.

However, this style fit very well with the facilities provided by the framework, and gave me some benefits.
On top of this, I used a *modern Java* approach, extensively using streams, optionals, map/flapmap, collectors and all the bells and whistles of the functional/reactive programming paradigm.

And to be honest, this was also a great opportunity to practice these moderns technologies without any constraint, so a took advantage ot that. 

# Future work
 The main pending task would be to stream downstream service responses to the client.
 
In the current architecture, each request returns a Reactor's `Mono` object that represent a single value. This means that the response returned to the client will be a single value (that single value is actually the full HTTP response).

In the other hand, Reactor project provides the type `Flux` with represent an unbounded stream of values. The server would return a `Flux` instead of a `Mono`, so it would be able to stream the responses in several chunks, without the need to receive and load in memory the downstream response before returning it to the upstream client.

This is specially important when the responses returned by downstream services are very large (hundreds of Mb), because in such cases, the memory pressure in the server will be very high (limiting the scalability)  