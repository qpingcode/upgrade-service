package me.qping.upgrade.common.constant;

public interface FileStatus {
	int	BEGIN		= 0;	// 开始		请求
	int	CENTER		= 1;	// 中间      回应
	int	END			= 2;	// 结尾		 回应
	int	COMPLETE	= 3;	// 完成
}