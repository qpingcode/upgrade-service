package me.qping.upgrade.common.message.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.message.Msg;

/**
 * 文件分片数据
 * 
 * @author admin
 *
 */
@Data
@NoArgsConstructor
public class FileBurstData extends Msg {
	private String	fileUrl;	// 客户端文件地址
	private String	fileName;	// 文件名称
	private Long	beginPos;	// 开始位置
	private Long	endPos;		// 结束位置
	private byte[]	bytes;		// 文件字节；再实际应用中可以使用非对称加密，以保证传输信息安全
	private Integer	status;		// Constants.FileStatus ｛0开始、1中间、2结尾、3完成｝

    public FileBurstData(int status) {
        this.status = status;
    }
}
