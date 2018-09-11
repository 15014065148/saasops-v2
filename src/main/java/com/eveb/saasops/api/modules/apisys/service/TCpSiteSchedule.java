package com.eveb.saasops.api.modules.apisys.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TCpSiteSchedule {

    @Autowired
    TCpSiteService tCpSiteService;

    //@Scheduled(cron="0/15 * * * * ? ") //每分钟执行一次
    public void flushSchemaName(){
       tCpSiteService.initSchemaName();
    }
}
