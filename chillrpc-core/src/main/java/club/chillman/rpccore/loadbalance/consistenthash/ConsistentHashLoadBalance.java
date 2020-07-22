package club.chillman.rpccore.loadbalance.consistenthash;

import club.chillman.rpccore.loadbalance.AbstractLoadBalance;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 负载均衡器，使用一致性哈希算法
 * @author NIU
 * @createTime 2020/7/22 17:18
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    // 哈希环
    private TreeMap<Long, String> hashCircle = new TreeMap<>();
    private List<String> cachedServiceAddresses;
    // 虚拟节点数+1
    private static final int VIRTUAL_NODE_NUMBER = 160;

    @Override
    protected String doSelect(List<String> serviceAddresses, RemoteRequest remoteRequest) {
        if (cachedServiceAddresses == null || serviceAddresses.hashCode() != cachedServiceAddresses.hashCode()) {
            buildHashCircle(serviceAddresses);
        }
        if (hashCircle.size() == 0) {
            return null;
        }
        byte[] digest = md5(remoteRequest.getRequestId());
        long hash = hash(digest, 0);
        if (!hashCircle.containsKey(hash)) {
            // 获取一个子集。其所有对象的 key 的值大于等于 fromKey
            SortedMap<Long, String> tailMap = hashCircle.tailMap(hash);
            // tailMap是值大于hash的节点集合
            // 如果是空，那么说明环上没有大于当前哈希值的节点，顺时针到达头部
            // 如果非空，那么取大于hash的最近的一个节点
            hash = tailMap.isEmpty() ? hashCircle.firstKey() : tailMap.firstKey();
        }
        return hashCircle.get(hash);
    }



    private void buildHashCircle(List<String> serviceAddresses) {
        if (cachedServiceAddresses == null || cachedServiceAddresses.size() == 0) {
            cachedServiceAddresses = serviceAddresses;
            for (String serviceAddress : serviceAddresses) {
                circleAdd(serviceAddress);
            }
        } else {

            log.info("旧地址列表为:{}", cachedServiceAddresses);
            log.info("新地址列表为:{}", serviceAddresses);
            Set<String> set = new HashSet<>(serviceAddresses);
            // 求交集 并存入set
            set.retainAll(cachedServiceAddresses);
            // 移除哈希环上过时的节点
            for (String cachedServiceAddress : cachedServiceAddresses) {
                if (!set.contains(cachedServiceAddress)) {
                    circleRemove(cachedServiceAddress);
                }
            }
            // 添加新节点
            for (String serviceAddress : serviceAddresses) {
                if (!set.contains(serviceAddress)) {
                    circleAdd(serviceAddress);
                }
            }
            this.cachedServiceAddresses = serviceAddresses;
        }
        log.info("更新后地址列表为:{}", hashCircle.values());
    }


    private void circleAdd(String serviceAddress) {
        for (int i = 0; i < VIRTUAL_NODE_NUMBER / 4; i++) {
            // 根据md5算法为每4个结点生成一个消息摘要，摘要长为16字节128位。
            byte[] digest = md5(serviceAddress + "-" + i);
            // 随后将MD5截取成四段，0-31,32-63,64-95,95-128，并生成4个32位数，存于long64中，long的高32位都为0
            // 并作为虚拟结点的key。
            for (int h = 0; h < 4; h++) {
                long m = hash(digest, h);
                hashCircle.put(m, serviceAddress);
            }
        }
    }

    private void circleRemove(String serviceAddress) {
        for (int i = 0; i < VIRTUAL_NODE_NUMBER / 4; i++) {
            // 根据md5算法为每4个结点生成一个消息摘要，摘要长为16字节128位。
            byte[] digest = md5(serviceAddress + "-" +  i);
            // 随后将128位分为4部分，0-31,32-63,64-95,95-128，并生成4个32位数，存于long中，long的高32位都为0
            // 并作为虚拟结点的key。
            for (int h = 0; h < 4; h++) {
                long m = hash(digest, h);
                hashCircle.remove(m);
            }
        }
    }



    private byte[] md5(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        md5.reset();
        byte[] bytes = null;
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        md5.update(bytes);
        return md5.digest();
    }

    /**
     * 摘要是128位，共16个byte
     * number为0就是digest 低32位，3是digest 高32位
     * 结果的long的高32位都为0
     * <p>
     * 因为生成的结果是一个32位数，若用int保存可能会产生负数。而一致性hash生成的逻辑环其hashCode的范围是在 0 - MAX_VALUE之间。因此为正整数，所以这里要强制转换为long类型，避免出现负数。
     *
     * @param digest
     * @param number
     * @return
     */
    private long hash(byte[] digest, int number) {
        return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                | (digest[0 + number * 4] & 0xFF))
                & 0xFFFFFFFFL;
    }
}
