package club.chillman.rpccore.serialize.json;

import club.chillman.rpccore.serialize.Serializer;
import com.alibaba.fastjson.JSONObject;

/**
 * @author NIU
 * @createTime 2020/7/22 17:14
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return JSONObject.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSONObject.parseObject(bytes, clazz);
    }
}
