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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;

/**
 * Created by rayt on 5/17/16.
 */
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {
	private static final Log logger = LogFactory
			.getLog(DiscoveryClientChannelFactory.class);
	
	private final GrpcChannelsProperties channels;
	private final DiscoveryClient client;
	// Stephan Maevers: added dispatcher for name resolver factory
	private final DiscoveryClientHeartBeatEventDispatcher dispatcher;

	public DiscoveryClientChannelFactory(GrpcChannelsProperties channels, DiscoveryClient client,
			DiscoveryClientHeartBeatEventDispatcher dispatcher) {
		this.channels = channels;
		this.client = client;
		this.dispatcher = dispatcher;
	}

	@Override
	public ManagedChannel createChannel(String name) {
		NettyChannelBuilder builder = NettyChannelBuilder.forTarget(name)
				.nameResolverFactory(new DiscoveryClientResolverFactory(client, dispatcher))
				.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
				.usePlaintext(channels.getChannelProperties(name).isPlaintext());

		if (channels.getChannelProperties(name).getMaxMessageSize() > 0) {
			// Stephan Maevers: Configurable max message size to allow large
			// messages
			logger.info("Setting max message to " + channels.getChannelProperties(name).getMaxMessageSize() + " MB");
			builder = builder.maxMessageSize(channels.getChannelProperties(name).getMaxMessageSize() * 1024 * 1024);
		}

		return builder.build();
	}
}
