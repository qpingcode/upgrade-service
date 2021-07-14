package me.qping.upgrade.common.message.progress;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.message.Msg;
import me.qping.utils.database.bean.DataBaseColumn;
import me.qping.utils.database.bean.DataBaseTable;

import java.util.Date;

/**
 * 文件分片指令
 * @author admin
 *
 */
@Data
@NoArgsConstructor
@DataBaseTable("file_progress")
public class FileProgress extends Msg {

    @DataBaseColumn("id")
    private int id;

    @DataBaseColumn("node_id")
    private long nodeId;

    @DataBaseColumn("status")
    private int status;       // FileStatus ｛0开始、1中间、2结尾、3完成｝

    @DataBaseColumn("file_url")
    private String fileUrl; // 文件URL

    @DataBaseColumn("total_size")
    private long totalSize;       // 文件总大小

    @DataBaseColumn("read_position")
    private long readPosition;    // 读取位置

    @DataBaseColumn("begin_date")
    private Date beginDate;      // 文件传输开始日期

    @DataBaseColumn("create_date")
    private Date endDate;         // 文件传输结束日期

    public FileProgress(int status) {
        this.status = status;
    }
}
