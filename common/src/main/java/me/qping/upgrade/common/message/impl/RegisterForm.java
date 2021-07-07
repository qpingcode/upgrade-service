package me.qping.upgrade.common.message.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName RegisterMsg
 * @Description TODO
 * @Author qping
 * @Date 2021/7/6 11:50
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterForm {
    int code;
    String result;
}
