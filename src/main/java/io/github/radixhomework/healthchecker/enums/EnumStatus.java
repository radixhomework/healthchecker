package io.github.radixhomework.healthchecker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumStatus {
    UNKNOWN("Unknown"),
    SUCCESS("Success"),
    FAILURE("Failure");

    private final String code;
}
