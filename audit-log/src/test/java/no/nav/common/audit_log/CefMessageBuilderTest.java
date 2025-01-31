package no.nav.common.audit_log;

import no.nav.common.audit_log.cef.CefMessage;
import no.nav.common.audit_log.cef.CefMessageEvent;
import no.nav.common.audit_log.cef.CefMessageSeverity;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static no.nav.common.audit_log.cef.CefMessageExtensionFields.FIELD_SOURCE_USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CefMessageBuilderTest {

    @Test
    public void should_build_cef_message() {
        Map<String, String> extensions = new HashMap<>();
        extensions.put(FIELD_SOURCE_USER_ID, "Z12345");
        extensions.put("flexString1Label", "hello");
        extensions.put("flexString1", "world");
        extensions.put("cs1Label", "testLabel");
        extensions.put("cs1", "testValue");

        CefMessage expectedMessage = new CefMessage(
                0,
                "my-application",
                "AuditLogger",
                "1.0",
                CefMessageEvent.ACCESS.type,
                "NAV-ansatt har gjort oppslag på bruker",
                CefMessageSeverity.INFO.name(),
                extensions
        );

        CefMessage newMessage = CefMessage.builder()
                .applicationName("my-application")
                .event(CefMessageEvent.ACCESS)
                .name("NAV-ansatt har gjort oppslag på bruker")
                .severity(CefMessageSeverity.INFO)
                .flexString(1, "hello", "world")
                .customString(1, "testLabel", "testValue")
                .extensions(extensions)
                .build();

        assertEquals(expectedMessage.getVersion(), newMessage.getVersion());
        assertEquals(expectedMessage.getDeviceProduct(), newMessage.getDeviceProduct());
        assertEquals(expectedMessage.getDeviceVendor(), newMessage.getDeviceVendor());
        assertEquals(expectedMessage.getDeviceVersion(), newMessage.getDeviceVersion());
        assertEquals(expectedMessage.getName(), newMessage.getName());
        assertEquals(expectedMessage.getSeverity(), newMessage.getSeverity());
        assertEquals(expectedMessage.getSignatureId(), newMessage.getSignatureId());

        Map<String, String> newMessageExt = newMessage.getExtension();

        assertEquals(5, newMessageExt.size());
        assertEquals(extensions.get(FIELD_SOURCE_USER_ID), newMessageExt.get(FIELD_SOURCE_USER_ID));
        assertEquals(extensions.get("flexString1Label"), newMessageExt.get("flexString1Label"));
        assertEquals(extensions.get("flexString1"), newMessageExt.get("flexString1"));
        assertEquals(extensions.get("cs1Label"), newMessageExt.get("cs1Label"));
        assertEquals(extensions.get("cs1"), newMessageExt.get("cs1"));
    }

    @Test
    public void should_throw_if_missing_data() {
        assertThrows(NullPointerException.class, () -> {
            CefMessage.builder()
                    .applicationName("my-application")
                    .build();
        });
    }

}
