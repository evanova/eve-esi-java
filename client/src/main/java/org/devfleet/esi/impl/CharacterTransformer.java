package org.devfleet.esi.impl;

import org.devfleet.esi.Calendar;
import org.devfleet.esi.Character;
import org.devfleet.esi.Location;
import org.devfleet.esi.model.GetCharactersCharacterIdCalendar200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdCalendarEventIdOk;
import org.devfleet.esi.model.GetCharactersCharacterIdCorporationhistory200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdLocationOk;
import org.devfleet.esi.model.GetCharactersCharacterIdOk;

final class CharacterTransformer {

    private CharacterTransformer() {}

    public static Character transform(final long charID, final GetCharactersCharacterIdOk c) {
        final Character character = new Character(charID);

        return character;
    }

    public static Character.History transform(GetCharactersCharacterIdCorporationhistory200Ok h) {
        final Character.History history = new Character.History();
        return history;
    }

    public static Calendar.Event transform(
            GetCharactersCharacterIdCalendar200Ok meta,
            GetCharactersCharacterIdCalendarEventIdOk details) {
        final Calendar.Event event = new Calendar.Event(Long.valueOf(meta.getEventId()));

        return event;
    }

    public static Location transform(GetCharactersCharacterIdLocationOk object) {
        Location location = new Location();
        location.setSolarSystemId((null == object.getSolarSystemId()) ? null : object.getSolarSystemId().longValue());
        location.setStationId((null == object.getStationId()) ? null : object.getStationId().longValue());
        location.setStructureId(object.getStructureId());
        return location;
    }

}
