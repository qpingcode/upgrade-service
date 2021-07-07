package me.qping.upgrade.common.message.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
/**
 * MessagePack工具类
 * 1、实现对象转byte数组
 * 2、实现byte数组转对象
 * 3、实现byte数组转List
 */
public class MessagePackUtil {
 
    //静态对象
    private  static ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
 
    /**
     * 对象转byte数组，
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] toBytes(T obj){
 
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * byte数组转List
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(byte[] bytes, Class<T> clazz){
        List<T> list = null;
 
        try {
            list = mapper.readValue(bytes, MessagePackUtil.<T>List(clazz));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
 
    /**
     * byte数组转对象
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toObject(byte[] bytes, Class<T> clazz) {
        try {
            return mapper.readValue(bytes, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 私有静态方法，获取List的实例化类型
     * @param clazz //List元素对象类型
     * @param <T>
     * @return
     */
    private static <T> JavaType List(Class<?> clazz){
        return mapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
    }
}