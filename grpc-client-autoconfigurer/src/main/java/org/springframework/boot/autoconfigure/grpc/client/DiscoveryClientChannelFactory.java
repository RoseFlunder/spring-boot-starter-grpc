/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.boot.autoconfigure.grpc.client;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;

/**
 * Created by rayt on 5/17/16.
 */
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {
	private final GrpcChannelsProperties channels;
	private final DiscoveryClient client;
	private final DiscoveryClientHeartBeatEventDispatcher dispatcher;

	public DiscoveryClientChannelFactory(GrpcChannelsProperties channels, DiscoveryClient client,
			DiscoveryClientHeartBeatEventDispatcher dispatcher) {
		this.channels = channels;
		this.client = client;
		this.dispatcher = dispatcher;
	}

	@Override
	public Channel createChannel(String name) {
		// Dont know why this line of code fixed the whole thing..
		// it trigger instantiation of the eureka client works here
		client.getServices();

		return ManagedChannelBuilder.forTarget(name)
				.nameResolverFactory(new DiscoveryClientResolverFactory(client, dispatcher))
				.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
				.usePlaintext(channels.getChannels().get(name).isPlaintext()).build();
	}
}
