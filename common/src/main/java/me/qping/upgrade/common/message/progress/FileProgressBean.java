package me.qping.upgrade.common.message.progress;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.message.Msg;
import me.qping.utils.database.bean.DataBaseColumn;
import me.qping.utils.database.bean.DataBaseTable;

import java.util.Date;

/**
 * 文件分片指令
 *
 * @author admin
 */
@Data
@NoArgsConstructor
@DataBaseTable("FILE_PROGRESS")
public class FileProgressBean extends Msg {

    @DataBaseColumn("id")
    protected long id = -1;

    @DataBaseColumn("message_id")
    protected long messageId;

    @DataBaseColumn("node_id")
    protected long nodeId;          // 文件来源节点id️

    @DataBaseColumn("to_node_id")
    protected long toNodeId;        // 目标节点

    @DataBaseColumn("file_name")
    protected String fileName;        // 文件名称

    @DataBaseColumn("source_path")
    protected String sourcePath;       // 文件URL

    @DataBaseColumn("target_path")
    protected String targetPath;       // 文件URL

    @DataBaseColumn("total_size")
    protected long totalSize;       // 文件总大小

    @DataBaseColumn("read_position")
    protected long readPosition;    // 读取位置

    @DataBaseColumn("status")
    protected int status;           // FileStatus 1 中间、2 结尾、4 错误

    @DataBaseColumn("err_msg")
    protected String errMsg;      // 错误信息记录

    @DataBaseColumn("begin_date")
    protected Date beginDate;      // 文件传输开始日期

    @DataBaseColumn("end_date")
    protected Date endDate;         // 文件传输结束日期
}
