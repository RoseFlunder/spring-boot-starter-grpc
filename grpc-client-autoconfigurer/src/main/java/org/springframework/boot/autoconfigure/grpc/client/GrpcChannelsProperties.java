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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

/**
 * Created by rayt on 5/17/16.
 */
@ConfigurationProperties("grpc.client")
public class GrpcChannelsProperties {
	@NestedConfigurationProperty
	private Map<String, GrpcChannelProperties> channels;

	public Map<String, GrpcChannelProperties> getChannels() {
		return channels;
	}
	
	//Stephan Maevers: Added this method to allow settings for channels with a placeholder at the end
	//Example: HeatMapService-Station-%
	public GrpcChannelProperties getChannelProperties(String name) {
		if (channels.containsKey(name))
			return channels.get(name);
		
		for (String key : channels.keySet()) {
			if (key.endsWith("%")) {
				String tmp = key.replace("%", "");
				if (name.startsWith(tmp)) {
					return channels.get(key);
				}
			}
		}
		
		return null;
	}

	public void setChannels(Map<String, GrpcChannelProperties> channels) {
		this.channels = channels;
	}

	@Override
	public String toString() {
		return "GrpcChannelsProperties{" + "channels=" + channels + '}';
	}
}
