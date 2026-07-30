package com.dbtraining.reconx.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Retirement endpoint for clients still calling the former v0 trade API. */
@RestController
@RequestMapping("/v0/trades")
@Tag(name = "deprecated-trades", description = "Retired trade API")
@SecurityRequirement(name = "bearerAuth")
public class DeprecatedTradeController {

    static final String SUNSET_DATE = "Wed, 01 Jul 2026 00:00:00 GMT";
    static final String SUCCESSOR_LINK =
            "</api/v1/trades>; rel=\"successor-version\"";

    @Deprecated(since = "v1.4.0", forRemoval = true)
    @GetMapping
    @Operation(summary = "Retired v0 trade endpoint", deprecated = true)
    public ResponseEntity<Void> retiredTrades() {
        return ResponseEntity.status(HttpStatus.GONE)
                .header("Deprecation", "true")
                .header("Sunset", SUNSET_DATE)
                .header(HttpHeaders.LINK, SUCCESSOR_LINK)
                .build();
    }
}
