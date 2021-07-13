package me.qping.upgrade.common.message.codec;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import me.qping.upgrade.common.constant.MsgTypeEnum;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具
 *
 * @author admin
 */
@SuppressWarnings("unchecked")
public class Serialization {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Map<Byte, Class<?>> msgTypeClassMap = new ConcurrentHashMap<>();
    private static Map<Class<?>, Byte> classMsgTypeMap = new ConcurrentHashMap<>();



    private static Objenesis objenesis = new ObjenesisStd();

    private Serialization() {

    }

    /**
     * 序列化(对象 -> 字节数组)
     *
     * @param obj 对象
     * @return 字节数组
     */

    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化(字节数组 -> 对象)
     *
     * @param data
     * @param cls
     * @param <T>
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }

    public static void init(){
        System.out.println("开始加载协议对象，共：" + MsgTypeEnum.values().length);
        for (MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()) {
            System.out.println(String.format("消息type: %s  消息Bean: %s", msgTypeEnum.val(), msgTypeEnum.protocolStruct().getName()));
            setClass(msgTypeEnum.val(), msgTypeEnum.protocolStruct());
        }
    }

    public static <T> void setClass(byte messageType, Class<T> cls) {
        msgTypeClassMap.put(messageType, cls);
        classMsgTypeMap.put(cls, messageType);
        getSchema(cls);
    }

    public static Class<?> getClass(byte messageType) {
        if(!msgTypeClassMap.containsKey(messageType)){
            throw new RuntimeException("消息类型不存在：" + messageType);
        }
        return msgTypeClassMap.get(messageType);
    }


    public static int getMsgType(Class<?> cls) {
        if(!classMsgTypeMap.containsKey(cls)){
            throw new RuntimeException("消息class不存在：" + cls.getName());
        }
        return classMsgTypeMap.get(cls);
    }
}
