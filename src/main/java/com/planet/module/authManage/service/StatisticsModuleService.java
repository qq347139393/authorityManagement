package com.planet.module.authManage.service;


import com.planet.common.util.RspResult;

import java.util.List;

public interface StatisticsModuleService {

    RspResult usersOnlineDurationForData(String usersJson);

    void usersOnlineDurationForFile(String usersJson);

    RspResult activeUsersForData(String usersJson);

    void activeUsersForFile(String usersJson);
}
