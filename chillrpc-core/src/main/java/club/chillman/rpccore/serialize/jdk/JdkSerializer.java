package club.chillman.rpccore.serialize.jdk;

import club.chillman.rpccommon.enumeration.RemoteErrorMessageEnum;
import club.chillman.rpccommon.exception.RemoteException;
import club.chillman.rpccommon.exception.SerializeException;
import club.chillman.rpccore.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author NIU
 * @createTime 2020/7/22 17:01
 */
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            byte[] bytes = baos.toByteArray();
            baos.close();
            oos.close();
            return bytes;
        } catch (Exception e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object o = ois.readObject();
            return clazz.cast(o);
        } catch (Throwable e) {
            throw new SerializeException("反序列化失败");
        }
    }
}
