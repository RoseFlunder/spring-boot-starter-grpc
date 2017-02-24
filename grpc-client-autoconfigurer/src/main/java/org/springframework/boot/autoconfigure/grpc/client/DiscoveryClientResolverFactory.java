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

import java.net.URI;

import javax.annotation.Nullable;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.Attributes;
import io.grpc.NameResolver;

/**
 * Created by rayt on 5/17/16.
 */
//Stephan Maevers: Added dispatcher for heartbeat events
public class DiscoveryClientResolverFactory extends NameResolver.Factory {
	private final DiscoveryClient client;
	private final DiscoveryClientHeartBeatEventDispatcher dispatcher;

	public DiscoveryClientResolverFactory(DiscoveryClient client, DiscoveryClientHeartBeatEventDispatcher dispatcher) {
		this.client = client;
		this.dispatcher = dispatcher;
	}

	@Nullable
	@Override
	public NameResolver newNameResolver(URI targetUri, Attributes params) {
		return new DiscoveryClientNameResolver(targetUri.toString(), client, params, dispatcher);
	}

	@Override
	public String getDefaultScheme() {
		return "spring";
	}
}
