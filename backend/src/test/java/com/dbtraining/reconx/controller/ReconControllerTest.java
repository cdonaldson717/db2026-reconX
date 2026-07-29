package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.api.GlobalExceptionHandler;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ReconControllerTest {

    private final Map<Long, ReconBreak> storedBreaks = new HashMap<>();
    private ReconBreak savedBreak;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        storedBreaks.clear();
        savedBreak = null;
        ReconBreakRepository breaks = (ReconBreakRepository) Proxy.newProxyInstance(
                ReconBreakRepository.class.getClassLoader(),
                new Class<?>[]{ReconBreakRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(storedBreaks.get((Long) args[0]));
                    case "save" -> savedBreak = (ReconBreak) args[0];
                    case "toString" -> "InMemoryReconBreakRepository";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        ReconController controller = new ReconController(breaks, null);
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resolveUpdatesAndSavesExistingBreak() throws Exception {
        ReconBreak reconBreak = new ReconBreak();
        reconBreak.setTradeId(42L);
        reconBreak.setDiscrepancyType("PRICE_MISMATCH");
        storedBreaks.put(7L, reconBreak);

        mockMvc.perform(put("/v1/recon/results/7/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Confirmed by counterparty\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Confirmed by counterparty"))
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

        assertThat(savedBreak).isSameAs(reconBreak);
    }

    @Test
    void resolveReturns404ForUnknownBreak() throws Exception {
        mockMvc.perform(put("/v1/recon/results/99/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Checked\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolveRejectsMissingOrBlankNote() throws Exception {
        mockMvc.perform(put("/v1/recon/results/7/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/v1/recon/results/7/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolveRejectsNoteLongerThan500Characters() throws Exception {
        String body = "{\"note\":\"" + "x".repeat(501) + "\"}";

        mockMvc.perform(put("/v1/recon/results/7/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
