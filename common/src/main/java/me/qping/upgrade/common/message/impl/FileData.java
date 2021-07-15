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
public class FileData{
    private String err;         // 错误信息
	private byte[]	bytes;		// 文件字节；再实际应用中可以使用非对称加密，以保证传输信息安全
}
