package com.servicelens.platform.api.dto;

import com.servicelens.platform.domain.SignalStatus;

import jakarta.validation.constraints.NotNull;

public record PatchSignalRequest(@NotNull SignalStatus status) {
}
