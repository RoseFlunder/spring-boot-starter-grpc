/*
 * Copyright 2016-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.grpc.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.util.RoundRobinLoadBalancerFactory;

/**
 * Autoconfiguration for gRPC clients.
 * 
 * @author Ray Tsang
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({ Channel.class })
public class GrpcClientAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public GrpcChannelsProperties defaultGrpcChannelsProperties() {
		Map<String, GrpcChannelProperties> defaultChannels = new HashMap<String, GrpcChannelProperties>();
		GrpcChannelsProperties properties = new GrpcChannelsProperties();

		GrpcChannelProperties defaultChannel = new GrpcChannelProperties();
		defaultChannels.put("default", defaultChannel);

		properties.setChannels(defaultChannels);

		return properties;
	}

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "false")
	@Bean
	public GrpcChannelFactory defaultGrpcChannelFactory(GrpcChannelsProperties channels) {
		return new AddressChannelFactory(channels);
	}

	@ConditionalOnMissingBean
	@Bean
	public LoadBalancer.Factory defaultGrpcLoadBalancerFactory() {
		return RoundRobinLoadBalancerFactory.getInstance();
	}

	//Stephan Maevers: Use dispatcher when Discovery Client is used
	@ConditionalOnMissingBean
	@ConditionalOnBean(DiscoveryClient.class)
	@Bean
	public DiscoveryClientHeartBeatEventDispatcher discoveryClientHeartBeatEventDispatcher() {
		return new DiscoveryClientHeartBeatEventDispatcher();
	}

	@ConditionalOnMissingBean
	@ConditionalOnBean(DiscoveryClient.class)
	@Bean
	public DiscoveryClientChannelFactory discoveryClientChannelFactory(GrpcChannelsProperties channels,
			DiscoveryClient client, DiscoveryClientHeartBeatEventDispatcher dispatcher) {
		return new DiscoveryClientChannelFactory(channels, client, dispatcher);
	}
}
