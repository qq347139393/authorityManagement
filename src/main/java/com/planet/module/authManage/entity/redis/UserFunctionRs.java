package com.planet.module.authManage.entity.redis;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserFunctionRs implements Serializable {

    private Long userId;

    private List<String> functionPermits;

}
