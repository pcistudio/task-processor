package com.contact.manager.util;

import com.contact.manager.notification.Notification;
import com.contact.manager.notification.NotificationTaskHandler;
import com.pcistudio.task.procesor.handler.TaskHandler;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericTypeUtilTest {
    @Test
    void test() {
        Class<?> genericType = GenericTypeUtil.getGenericTypeFromInterface(NotificationTaskHandler.class, TaskHandler.class);
        assertEquals(Notification.class, genericType);
    }
}
