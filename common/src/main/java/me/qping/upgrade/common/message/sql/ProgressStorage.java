package me.qping.upgrade.common.message.sql;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Assert;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileProgress;
import me.qping.utils.database.DataBaseUtilBuilder;
import me.qping.utils.database.util.MetaDataUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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


    MetaDataUtil database;

    public void init(String url, String username, String password, boolean force) throws ClassNotFoundException, SQLException {
        database = DataBaseUtilBuilder.init(url, username, password).build();

        if(database.getTableInfo("FILE_PROGRESS") == null || force){

            String content = null;
            BufferedReader reader = null;
            try {
                ClassPathResource resource = new ClassPathResource("init.sql");
                reader = new BufferedReader(new InputStreamReader(resource.getStream()));
                content = reader.lines().collect(Collectors.joining("\n"));
                reader.close();

                database.update(content);
            } catch (Exception e) {
                System.err.println("初始化数据库异常：" + e.getMessage());
            } finally {
                try {
                    if (null != reader) {
                        reader.close();
                    }
                } catch (IOException e) {}
            }

            System.out.println(content);
        }

    }


    public FileProgress findByNodeIdAndFilePathAndFileName(long nodeId, String filePath, String fileName) throws Exception {
        // 1 中间 2 结束 3 错误
        List<FileProgressBean> list = database.queryList(FileProgressBean.class, "select * from FILE_PROGRESS where node_id = ? and source_path = ? and file_name = ? order by begin_date desc limit 1", nodeId, filePath, fileName);
        if(list != null && list.size() > 0){
            FileProgress progress = new FileProgress();
            BeanUtil.copyProperties(list.get(0), progress);
            return progress;
        }
        return null;
    }


    public int insert(FileProgress progress, Long toNodeId) throws SQLException {

        Assert.notNull(progress);
        Assert.notBlank(progress.getFileName());
        Assert.notBlank(progress.getSourcePath());
        Assert.notBlank(progress.getTargetPath());
        Assert.isTrue(progress.getFileName().length() < 800);
        Assert.isTrue(progress.getSourcePath().length() < 800);
        Assert.isTrue(progress.getTargetPath().length() < 800);

        FileProgressBean progressBean = new FileProgressBean();
        BeanUtil.copyProperties(progress, progressBean);

        progressBean.setBeginDate(new Date());
        progressBean.setToNodeId(toNodeId);

        int key = database.insertReturnPrimaryKey(progressBean, "id");

        progress.setId(key);

        return key;
    }

    public void tagEnd(int progressId, long position) throws SQLException {
        database.update("update FILE_PROGRESS set read_position = ?, status = ?, end_date = ?  where id = ?", position, FileStatus.END, new Date(), progressId);
    }

    public void tagProgress(int progressId, long totalSize, long position) throws SQLException {
        database.update("update FILE_PROGRESS set read_position = ?, status = ?  where id = ?", position, FileStatus.CENTER, progressId);
    }

    public void tagError(int progressId, String errorMsg) throws SQLException {
        if(errorMsg.length() > 800){
            errorMsg = errorMsg.substring(0, 800);
        }
        database.update("update FILE_PROGRESS set err_msg = ?, status = ? where id = ? ", errorMsg, FileStatus.ERROR, progressId);
    }

    public void tagStop(int progressId) throws SQLException {
        database.update("update FILE_PROGRESS set status = ?, end_date = ?  where id = ?", FileStatus.STOP, new Date(), progressId);
    }


    public List<FileProgressBean> findAll() throws IllegalAccessException, SQLException, InstantiationException {
        return database.queryList(FileProgressBean.class, "select * from FILE_PROGRESS");
    }

    public FileProgressBean findById(Integer id) throws IllegalAccessException, SQLException, InstantiationException {
        if(id == null){
            return null;
        }
        return database.queryOne(FileProgressBean.class, "select * from FILE_PROGRESS where id = ?", id);
    }


}
