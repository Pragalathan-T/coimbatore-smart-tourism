package com.yourorg.tourism.guide.entity;

public enum VerificationLevel {
    BASIC,
    ID_VERIFIED,
    ADDRESS_VERIFIED,
    FULLY_VERIFIED;

    public boolean isAtLeast(VerificationLevel minimumLevel) {
        return this.ordinal() >= minimumLevel.ordinal();
    }
}
