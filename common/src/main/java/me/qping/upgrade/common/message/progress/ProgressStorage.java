package me.qping.upgrade.common.message.progress;

import me.qping.upgrade.common.message.impl.FileBurstInstruct;
import me.qping.utils.database.DataBaseUtilBuilder;
import me.qping.utils.database.util.CrudUtil;

/**
 * @ClassName DataBase
 * @Description 进度
 * @Author qping
 * @Date 2021/7/12 14:35
 * @Version 1.0
 **/
public class ProgressStorage {


    public static ProgressStorage getInstance(){
        return Holder.instance;
    }

    private static class Holder {
        private static ProgressStorage instance = new ProgressStorage();
    }


    CrudUtil crudUtil;

    public void init(String url, String username, String password) throws ClassNotFoundException {
        crudUtil = DataBaseUtilBuilder.init(url, username, password).buildCrudUtil();
    }


    public FileBurstInstruct findByNodeIdAndFileUrl(long nodeId, String fileUrl) {
        return null;
    }


    public void save(FileBurstInstruct file) {

    }

    public void updateEndDate(int fileId) {

    }



}
