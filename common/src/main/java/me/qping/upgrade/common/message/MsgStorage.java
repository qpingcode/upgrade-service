package me.qping.upgrade.common.message;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import lombok.Data;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static me.qping.upgrade.common.constant.ServerConstant.MSG_STORAGE_CLEAN_SLEEP_INTERVAL;
import static me.qping.upgrade.common.constant.ServerConstant.MSG_STORAGE_MSG_TIMEOUT;

/**
 * @ClassName ResponseFuture
 * @Description 管理请求和响应的关系
 * 主要是通过唯一的请求标识id
 * @Author qping
 * @Date 2021/7/2 09:10
 * @Version 1.0
 **/
@Data
public class MsgStorage{


    static TimedCache<Long, LinkedBlockingQueue<Msg>> timedCache = CacheUtil.newTimedCache(MSG_STORAGE_MSG_TIMEOUT);
    static{
        timedCache.schedulePrune(MSG_STORAGE_CLEAN_SLEEP_INTERVAL);
    }


    long start;
    Msg msg;

    public MsgStorage(Msg msg) {
        this.msg = msg;
        this.start = new Date().getTime();
    }

    public static void init(long messageId) {
        timedCache.put(messageId, new LinkedBlockingQueue<Msg>(1));
    }

    /**
     * 根据请求id获取响应结果
     * @param messageId
     * @return
     */
    public static <T extends Msg> T get(long messageId, long timeoutMillis) {

        try {

            if(timeoutMillis > MSG_STORAGE_MSG_TIMEOUT){
                timeoutMillis = MSG_STORAGE_MSG_TIMEOUT;
            }

            if(timedCache.get(messageId) == null){
                return null;
            }

            Msg msg = timedCache.get(messageId).poll(timeoutMillis, TimeUnit.MILLISECONDS);
            timedCache.remove(messageId);

            if(msg != null){
                return (T) msg;
            }
        } catch (InterruptedException e) {}

        return null;
    }

    /**
     * 存储服务器端的响应
     * @param responseMsg
     */
    public static void recive(Msg responseMsg) {

        if(responseMsg == null || timedCache.get(responseMsg.getMessageId()) == null){
            return;
        }
        timedCache.get(responseMsg.getMessageId()).add(responseMsg);
    }


}
