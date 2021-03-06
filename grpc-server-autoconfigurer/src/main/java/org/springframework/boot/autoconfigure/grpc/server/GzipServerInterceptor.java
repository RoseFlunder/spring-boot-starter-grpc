package org.springframework.boot.autoconfigure.grpc.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * Server interceptor which enables gzip compression
 * @author Stephan.Maevers
 */
public class GzipServerInterceptor implements ServerInterceptor {

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		call.setCompression("gzip");
		return next.startCall(call, headers);
	}

}
