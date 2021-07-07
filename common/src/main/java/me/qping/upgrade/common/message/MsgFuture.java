package me.qping.upgrade.common.message;

import lombok.Data;
import me.qping.upgrade.common.message.impl.ResponseBase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName ResponseFuture
 * @Description 管理请求和响应的关系
 * 主要是通过唯一的请求标识id
 * @Author qping
 * @Date 2021/7/2 09:10
 * @Version 1.0
 **/
@Data
public class MsgFuture {

    //请求id
    private long id;

    //请求id对应的响应结果
    private volatile Msg responseMsg;

    //存储响应结果和自身绑定在一起
    public final static Map<Long, MsgFuture> FUTURES = new ConcurrentHashMap<Long, MsgFuture>();

    //超时时间
    private long timeout;

    private final long start = System.currentTimeMillis();

    //获取锁
    private volatile Lock lock = new ReentrantLock();

    //线程通知条件
    private volatile Condition condition = lock.newCondition();


    public MsgFuture(Msg msg) {
        id = msg.getMessageId();               // 获取对应的请求ID
        FUTURES.put(id, this);          // 存储当前的请求ID对应的上下文信息
    }

    private boolean hasDone() {
        return responseMsg != null ? true : false;
    }

    /**
     * 根据请求id获取响应结果
     * @param timeout
     * @return
     */
    public Msg get(long timeout) {
        long start = System.currentTimeMillis();
        lock.lock();//先锁
        while (!hasDone()) {
            try {
                condition.await(timeout, TimeUnit.SECONDS);
                if (System.currentTimeMillis() - start >= timeout) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();//释放锁
            }
        }
        return responseMsg;
    }

    /**
     * 存储服务器端的响应
     * @param responseMsg
     */
    public static void recive(Msg responseMsg) {
        //找到res相对应的DefaultFuture
        MsgFuture future = FUTURES.remove(responseMsg.getMessageId());
        if (future == null) {
            return;
        }
        Lock lock = future.getLock();
        lock.lock();
        try {
            //设置响应
            future.setResponseMsg(responseMsg);
            Condition condition = future.getCondition();
            if (condition != null) {
                //通知
                condition.signal();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 处理请求超时的线程
     */
    static class FutureTimeOutThread extends Thread {
        @Override
        public void run() {
            while (true) {
                for (long futureId : FUTURES.keySet()) {
                    MsgFuture f = FUTURES.get(futureId);
                    if (f == null) {
                        FUTURES.remove(futureId);                       // 为空的话 代表请求结果已经处理完毕了
                        continue;
                    }
                    if (f.getTimeout() > 0) {
                        if ((System.currentTimeMillis() - f.getStart()) > f.getTimeout()) {
                            Msg msg = Msg.response(f.getId(), ResponseBase.timeout());
                            MsgFuture.recive(msg);                 // 存储服务端的响应结果信息
                        }
                    }
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
