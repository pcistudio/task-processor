package com.pcistudio.task.procesor.register;

import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.util.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HandlerManagerImplTest {

    @Mock
    private TaskStorageSetup taskTableSetup;

    @Test
    @DisplayName("Test register handler with same table name")
    void testRegisterHandlerWithSame() {
        final HandlerManager manager = HandlerManagerImpl.builder()
                .taskTableSetup(taskTableSetup)
                .register(
                        HandlerProperties.builder()
                                .handlerName("email")
                                .tableName("notification")
                                .encrypt(false)
                                .taskHandler(payload -> {})
                                .taskHandlerType(Object.class)
                                .build()
                )
                .register(
                        HandlerProperties.builder()
                                .handlerName("sms")
                                .tableName("notification")
                                .encrypt(false)
                                .taskHandler(payload -> {})
                                .taskHandlerType(Object.class)
                                .build()
                )
                .build();
        Assert.notNull(manager, "manager is required");
        HandlerPropertiesWrapper email = manager.getProperties("email");
        assertEquals("task_info_notification", email.getTableName());

        HandlerPropertiesWrapper sms = manager.getProperties("sms");
        assertEquals("task_info_notification", sms.getTableName());
    }

    @Test
    void testDuplicatedHandler() {
        HandlerManagerImpl.Builder handlerManagerBuilder = HandlerManagerImpl.builder()
                .taskTableSetup(taskTableSetup)
                .register(
                        HandlerProperties.builder()
                                .handlerName("email")
                                .tableName("email")
                                .encrypt(false)
                                .taskHandler(payload -> {
                                })
                                .taskHandlerType(Object.class)
                                .build()
                )
                .register(
                        HandlerProperties.builder()
                                .handlerName("email")
                                .tableName("email")
                                .encrypt(false)
                                .taskHandler(payload -> {
                                })
                                .taskHandlerType(Object.class)
                                .build()
                );
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, handlerManagerBuilder::build);
        assertEquals("Handler already registered: email", illegalStateException.getMessage());
    }

}