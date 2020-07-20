package club.chillman.rpccore.transport.netty.consumer;

import club.chillman.rpccore.transport.dto.RemoteResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author NIU
 * @createTime 2020/7/20 21:48
 */
public class UnprocessedRequests {
    private static Map<String, CompletableFuture<RemoteResponse>> unprocessedFutureMap = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RemoteResponse> future) {
        unprocessedFutureMap.put(requestId, future);
    }

    public void complete(RemoteResponse remoteResponse) {
        CompletableFuture<RemoteResponse> future = unprocessedFutureMap.remove(remoteResponse.getResponseId());
        if (null != future) {
            future.complete(remoteResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
