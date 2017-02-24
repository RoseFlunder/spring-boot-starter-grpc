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

package org.springframework.boot.autoconfigure.grpc.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

/**
 * Discovers gRPC service implementations by the {@link GrpcService} annotation.
 * 
 * @author Ray Tsang
 * @author Stephan Maevers
 */
public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware, GrpcServiceDiscoverer {
	private static final Log logger = LogFactory.getLog(AnnotationGrpcServiceDiscoverer.class);

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public Collection<String> findGrpcServiceBeanNames() {
		String[] beanNames = this.applicationContext.getBeanNamesForAnnotation(GrpcService.class);
		return Collections.unmodifiableList(Arrays.asList(beanNames));
	}

//	@Override
//	public Collection<GrpcServiceDefinition> findGrpcServices() {
//		Collection<String> beanNames = findGrpcServiceBeanNames();
//		List<GrpcServiceDefinition> definitions = new ArrayList<GrpcServiceDefinition>(beanNames.size());
//		for (String beanName : beanNames) {
//			Object bean = this.applicationContext.getBean(beanName);
//			Class<?> beanClazz = bean.getClass();
//			GrpcService grpcServiceAnnotation = AnnotationUtils.findAnnotation(beanClazz, GrpcService.class);
//			Class<?> grpcClazz = grpcServiceAnnotation.value();
//			Method[] methods = grpcClazz.getDeclaredMethods();
//			boolean bindServiceFound = false;
//			for (Method method : methods) {
//				if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("bindService")) {
//					bindServiceFound = true;
//					try {
//						Class<?> grpcInterfaceClazz = method.getParameterTypes()[0];
//						if (!grpcInterfaceClazz.isAssignableFrom(beanClazz)) {
//							throw new IllegalStateException("gRPC service class: " + bean.getClass().getName()
//									+ " does not implement " + grpcInterfaceClazz.getName());
//						}
//						ServerServiceDefinition definition = (ServerServiceDefinition) method.invoke(null, bean);
//						definitions.add(new GrpcServiceDefinition(beanName, beanClazz, definition));
//						logger.debug("Found gRPC service: " + definition.getName() + ", bean: " + beanName + ", class: "
//								+ bean.getClass().getName());
//					} catch (IllegalAccessException e) {
//						throw new IllegalStateException(e);
//					} catch (IllegalArgumentException e) {
//						throw new IllegalStateException(e);
//					} catch (InvocationTargetException e) {
//						throw new IllegalStateException(e);
//					}
//				}
//			}
//			if (!bindServiceFound) {
//				throw new IllegalStateException(grpcClazz.getName()
//						+ " does not have a static bindService method, are you sure this is a gRPC generated class?");
//			}
//		}
//		return definitions;
//	}

	// Stephan Mävers: rewrote this method because of gRPC api changes and interceptor support
	// No interfaces and wrapper classes for services anymore, instead the
	// method to create service definitions is part of the parent class, which
	// leads to easier code here
	@Override
	public Collection<GrpcServiceDefinition> findGrpcServices() {
		Collection<String> beanNames = findGrpcServiceBeanNames();
		List<GrpcServiceDefinition> definitions = new ArrayList<GrpcServiceDefinition>(beanNames.size());
		for (String beanName : beanNames) {
			Object bean = this.applicationContext.getBean(beanName);
			Class<?> beanClazz = bean.getClass();
			try {
				Method method = beanClazz.getMethod("bindService");
				ServerServiceDefinition definition = (ServerServiceDefinition) method.invoke(bean);
				//add interceptors
				List<ServerInterceptor> interceptors = new ArrayList<>();
				GrpcService annotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
				if (annotation.interceptors() != null) {
					for (Class<? extends ServerInterceptor> inteceptorClass : annotation.interceptors()) {
						interceptors.add(inteceptorClass.newInstance());
					}
				}
				definition = ServerInterceptors.intercept(definition, interceptors);
				definitions.add(new GrpcServiceDefinition(beanName, beanClazz, definition));
				logger.debug("Found gRPC service: " + definition.getServiceDescriptor().getName() + ", bean: "
						+ beanName + ", class: " + bean.getClass().getName());
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e1) {
				throw new IllegalStateException(beanClazz.getName()
						+ " does not have a bindService method, are you sure this is a gRPC generated class?");
			} catch (InstantiationException e) {
				logger.error("Cannot create an instance of an interceptor", e);
			}
		}
		return definitions;
	}

}
