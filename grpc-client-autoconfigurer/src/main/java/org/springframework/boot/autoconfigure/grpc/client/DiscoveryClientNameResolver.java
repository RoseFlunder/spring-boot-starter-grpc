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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;

/**
 * Created by rayt on 5/17/16.
 * @author Stephan Maevers
 */
public class DiscoveryClientNameResolver extends NameResolver {
	private final String name;
	private final DiscoveryClient client;
	@SuppressWarnings("unused")
	private final Attributes attributes;
	private final DiscoveryClientHeartBeatEventDispatcher dispatcher;

	private Listener listener;

	public DiscoveryClientNameResolver(String name, DiscoveryClient client, Attributes attributes,
			DiscoveryClientHeartBeatEventDispatcher dispatcher) {
		this.name = name;
		this.client = client;
		this.attributes = attributes;
		this.dispatcher = dispatcher;
	}

	@Override
	public String getServiceAuthority() {
		return name;
	}

	@Override
	public void start(Listener listener) {
		this.listener = listener;
		//Stephan Maevers: register this name resolver at the dispatcher to get refreshed at eureka heartbeat events
		dispatcher.addListener(this);
		refresh();
	}

	//Stephan Maevers: Modfied this method to be compatible with gRPC 1.0.3
	@Override
	public void refresh() {
		List<List<ResolvedServerInfo>> servers = new ArrayList<>();
		for (ServiceInstance serviceInstance : client.getInstances(name)) {
			servers.add(Collections.singletonList(new ResolvedServerInfo(
					InetSocketAddress.createUnresolved(serviceInstance.getHost(), serviceInstance.getPort()),
					Attributes.EMPTY)));
		}

		this.listener.onUpdate(servers, Attributes.EMPTY);
	}

	//Stephan Maevers: deregister from the dispatcher
	@Override
	public void shutdown() {
		dispatcher.removeListener(this);
	}
}
