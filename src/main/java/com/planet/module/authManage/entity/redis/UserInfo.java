package com.planet.module.authManage.entity.redis;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo implements Serializable {

    private Long id;

    private String name;

    private String code;

    private String realName;

    private String nickname;
}
