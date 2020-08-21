package club.chillman.rpccore.transport.netty.common;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.transport.netty.consumer.NettyConsumer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author NIU
 * @createTime 2020/7/20 23:21
 */
@Slf4j
public class ChannelPool {
    private static Map<String, Channel> channelMap;
    private static NettyConsumer nettyConsumer;

    static {
        nettyConsumer = SingletonFactory.getInstance(NettyConsumer.class);
        channelMap = new ConcurrentHashMap<>();
    }

    private ChannelPool() {
    }

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 判断是否有对应地址的连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // 如果有的话，判断连接是否可用，可用的话就直接获取
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        // 否则，重新连接获取 Channel
        Channel channel = nettyConsumer.doConnect(inetSocketAddress);
        channelMap.put(key, channel);
        return channel;
    }

    public static void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        Channel channel = channelMap.remove(key);
        log.info("Channel池的大小为 :[{}]", channelMap.size());
    }
    public static Channel put(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        Channel oldChannel = channelMap.put(key, channel);
        log.info("Channel池的大小为 :[{}]", channelMap.size());
        return oldChannel;
    }
    public static boolean isActive(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)) {
            return channelMap.get(key).isActive();
        }
        return false;
    }
}
