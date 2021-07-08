package me.qping.upgrade.common.message;

import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName ResponseFuture
 * @Description 管理请求和响应的关系
 * 主要是通过唯一的请求标识id
 * @Author qping
 * @Date 2021/7/2 09:10
 * @Version 1.0
 **/
@Data
public class MsgStorage {

    public static final  Map<Long, MsgStorage> msgStorageMap = new ConcurrentHashMap<Long, MsgStorage>();
    public static final int TIMEOUT = 300 * 1000;
    public static final int SLEEP_INTERVAL = 30 * 000;

    long start;
    Msg msg;

    public MsgStorage(Msg msg) {
        this.msg = msg;
        this.start = new Date().getTime();
    }

    /**
     * 根据请求id获取响应结果
     * @param messageId
     * @return
     */
    public static Msg get(long messageId) {
        MsgStorage msg = msgStorageMap.get(messageId);
        msgStorageMap.remove(messageId);
        return msg.getMsg();
    }

    /**
     * 存储服务器端的响应
     * @param responseMsg
     */
    public static void recive(Msg responseMsg) {
        msgStorageMap.put(responseMsg.getMessageId(), new MsgStorage(responseMsg));
    }

    /**
     * 处理请求超时的线程
     */
    static class FutureTimeOutThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException e) {}

                if(!msgStorageMap.isEmpty()){
                    int clearCount = 0;
                    for (long futureId : msgStorageMap.keySet()) {
                        MsgStorage f = msgStorageMap.get(futureId);
                        if (f == null || (System.currentTimeMillis() - f.getStart()) > TIMEOUT) {
                            msgStorageMap.remove(futureId);
                            clearCount++;
                        }
                    }
                    System.out.println("清理消息: " + clearCount + " 条");
                }

            }
        }
    }

    /**
     * 设置为后台线程
     */
    static {
        FutureTimeOutThread timeOutThread = new FutureTimeOutThread();
        timeOutThread.setDaemon(true);
        timeOutThread.start();
    }


}
