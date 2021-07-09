package me.qping.upgrade.common.message.impl;

import lombok.Data;

/**
 * 文件描述信息
 * 
 * @author admin
 *
 */
@Data
public class FileDescInfo {
	private String	fileUrl;	// 文件路径
	private String	fileName;	// 文件名称
	private String	fileType;	// 文件类型（业务日志、存储日志、升级文件）
	private Long	fileSize;	// 文件大小
}
