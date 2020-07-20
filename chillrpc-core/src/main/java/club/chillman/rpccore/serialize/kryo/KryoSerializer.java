package club.chillman.rpccore.serialize.kryo;

import club.chillman.rpccore.serialize.Serializer;

/** TODO
 * @author NIU
 * @createTime 2020/7/20 23:41
 */
public class KryoSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
