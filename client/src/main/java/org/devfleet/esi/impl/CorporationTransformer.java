package org.devfleet.esi.impl;

import org.devfleet.esi.Corporation;
import org.devfleet.esi.model.GetCorporationsCorporationIdAlliancehistory200Ok;
import org.devfleet.esi.model.GetCorporationsCorporationIdMembers200Ok;
import org.devfleet.esi.model.GetCorporationsCorporationIdOk;

final class CorporationTransformer {

    private CorporationTransformer() {}

    public static Corporation transform(final long corpID, final GetCorporationsCorporationIdOk c) {
        final Corporation corporation = new Corporation(corpID);

        return corporation;
    }

    public static Corporation.History transform(GetCorporationsCorporationIdAlliancehistory200Ok h) {
        final Corporation.History history = new Corporation.History();
        return history;
    }

    public static Corporation.Member transform(GetCorporationsCorporationIdMembers200Ok m) {
        final Corporation.Member member = new Corporation.Member();

        return member;
    }

}
