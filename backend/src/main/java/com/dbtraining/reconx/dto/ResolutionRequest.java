package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for atomically resolving a reconciliation break. */
public record ResolutionRequest(
        @NotBlank @Size(max = 500) String note
) {
}
