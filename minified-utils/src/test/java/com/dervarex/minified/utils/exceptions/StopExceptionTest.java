package com.dervarex.minified.utils.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopExceptionTest {

    @Test
    void factoriesSetTheRightSeverity() {
        StopException cancel = StopException.userCancel("closed the window");
        StopException fatal = StopException.fatal("disk is on fire");

        assertEquals(StopException.Severity.INFO, cancel.getSeverity());
        assertEquals("USER_CANCEL", cancel.getCode());
        assertTrue(cancel.isRecoverable());

        assertEquals(StopException.Severity.FATAL, fatal.getSeverity());
        assertEquals("FATAL_STOP", fatal.getCode());
        assertFalse(fatal.isRecoverable());
    }

    @Test
    void userFriendlyMessageListsTheMetadata() {
        String message = new StopException.Builder()
                .reason("no space left")
                .meta("path", "/home/someone/.minecraft")
                .build()
                .toUserFriendlyMessage();

        assertTrue(message.startsWith("Operation stopped: no space left"));
        assertTrue(message.contains("  - path: /home/someone/.minecraft"));
    }
}
