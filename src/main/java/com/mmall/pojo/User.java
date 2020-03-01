package com.mmall.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id; // 用户id

    private String username;

    private String password;

    private String email;

    private String phone;

    private String question; // 找回密码问题

    private String answer; // 找回密码答案

    private Integer role; // 用户角色

    private Date createTime;

    private Date updateTime;
}