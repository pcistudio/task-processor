package com.contact.manager.notification;

import com.pcistudio.task.procesor.handler.TaskHandler;
import com.pcistudio.task.procesor.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationTaskHandler implements TaskHandler<Notification> {
    @Override
    public void process(Notification payload) {
//        log.info("{}", JsonUtil.toPrettyJson(payload));

        JsonUtil.print("PROCESSED NOTIFICATION\n", payload);
    }
}
