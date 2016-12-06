package org.springframework.boot.autoconfigure.grpc.client;

import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.event.EventListener;

import io.grpc.NameResolver;

public class DiscoveryClientHeartBeatEventDispatcher {

	private final CopyOnWriteArraySet<NameResolver> listeners = new CopyOnWriteArraySet<>();
	
	public void addListener(NameResolver nameResolver) {
		listeners.add(nameResolver);
	}
	
	public void removeListener(NameResolver nameResolver) {
		listeners.remove(nameResolver);
	}
	
	@EventListener
	public void onEvent(HeartbeatEvent e) {
		System.out.println("Received heart beat event");
		for (NameResolver nameResolver : listeners) {
			nameResolver.refresh();
		}
	}
}
