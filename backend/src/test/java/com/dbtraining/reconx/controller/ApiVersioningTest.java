package com.dbtraining.reconx.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ApiVersioningTest {

    @Test
    void retiredV0TradesReturns410AndStandardDeprecationHeaders() throws Exception {
        MockMvc mockMvc = standaloneSetup(new DeprecatedTradeController()).build();

        String sunset = mockMvc.perform(get("/v0/trades"))
                .andExpect(status().isGone())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Sunset", DeprecatedTradeController.SUNSET_DATE))
                .andExpect(header().string("Link", DeprecatedTradeController.SUCCESSOR_LINK))
                .andReturn()
                .getResponse()
                .getHeader("Sunset");

        assertThat(ZonedDateTime.parse(sunset, DateTimeFormatter.RFC_1123_DATE_TIME))
                .isNotNull();
    }

    @Test
    void retiredEndpointCarriesSourceLevelRemovalMetadata() throws Exception {
        Method method = DeprecatedTradeController.class.getMethod("retiredTrades");
        Deprecated annotation = method.getAnnotation(Deprecated.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.since()).isEqualTo("v1.4.0");
        assertThat(annotation.forRemoval()).isTrue();
    }

    @Test
    void currentResourceControllersUseV1Prefix() {
        assertVersioned(TradeController.class);
        assertVersioned(ReconController.class);
        assertVersioned(AuditController.class);
    }

    private static void assertVersioned(Class<?> controllerType) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertThat(mapping)
                .as("%s must declare a class-level RequestMapping", controllerType.getSimpleName())
                .isNotNull();
        assertThat(mapping.value())
                .allMatch(path -> path.startsWith("/v1/"));
    }
}
