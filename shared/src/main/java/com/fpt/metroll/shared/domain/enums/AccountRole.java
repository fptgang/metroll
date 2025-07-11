package com.fpt.metroll.shared.domain.enums;

public enum AccountRole {
    ADMIN,
    STAFF,
    CUSTOMER;

    public boolean hasHigherRankThan(AccountRole role) {
        return ordinal() < role.ordinal();
    }

    public boolean hasHigherOrEqualRank(AccountRole role) {
        return ordinal() <= role.ordinal();
    }
}
