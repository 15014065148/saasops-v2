package com.eveb.saasops.listener;

import com.eveb.saasops.modules.system.msgtemple.service.MsgModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by William on 2018/3/7.
 */
@Slf4j
//@Configuration
@Component
public class MessageSenderListener implements ApplicationListener<BizEvent> {

    @Autowired
    private MsgModelService msgModelService;

    @Override
    public void onApplicationEvent(BizEvent event) {
        log.info("消息正在发送");
        msgModelService.sendMsg(event);
        //msgModelService.sendMsg(event.getSiteCode(), event.getUserId(), String.valueOf(event.getEventType().getEventCode()));
    }
}



