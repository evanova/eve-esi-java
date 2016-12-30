package org.devfleet.esi.impl;

import org.devfleet.esi.Calendar;
import org.devfleet.esi.Character;
import org.devfleet.esi.ESIClient;
import org.devfleet.esi.api.CalendarApi;
import org.devfleet.esi.api.CharacterApi;
import org.devfleet.esi.client.ApiClient;
import org.devfleet.esi.model.GetCharactersCharacterIdCalendar200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdCalendarEventIdOk;
import org.devfleet.esi.model.GetCharactersCharacterIdCorporationhistory200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdPortraitOk;
import org.devfleet.esi.model.PutCharactersCharacterIdCalendarEventIdResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;

class CharacterAPIImpl {

    private static Logger LOG = LoggerFactory.getLogger(CharacterAPIImpl.class);

    private final CharacterApi characterApi;
    private final CalendarApi calendarApi;

    private final String datasource;

    public CharacterAPIImpl(final Retrofit rf, final String datasource){
        this.characterApi = rf.create(CharacterApi.class);
        this.calendarApi = rf.create(CalendarApi.class);
        this.datasource = datasource;
    }

    public Character getCharacter(Long charID) {
      try {
          Character character = ESITransformer.transform(
                  charID,
                  this.characterApi
                  .getCharactersCharacterId(charID.intValue(), this.datasource)
                  .execute()
                  .body());
          addPortraits(character);
          addHistory(character);
          return character;
      }
      catch (IOException e) {
          LOG.debug(e.getLocalizedMessage(), e);
          LOG.warn(e.getLocalizedMessage());
          return null;
      }
    }

    public Calendar getCalendar(Long charID, Long afterEventID) {
        try {
            List<GetCharactersCharacterIdCalendar200Ok> events =
                    this.calendarApi
                    .getCharactersCharacterIdCalendar(charID, (null == afterEventID) ? null : afterEventID.intValue(), this.datasource)
                    .execute()
                    .body();
            final Calendar calendar = new Calendar();
            for (GetCharactersCharacterIdCalendar200Ok e: events) {
                addEvent(charID, e, calendar);
            }

            return calendar;
        }
        catch (IOException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            LOG.warn(e.getLocalizedMessage());
            return null;
        }
    }

    private void addEvent(final Long charID, final GetCharactersCharacterIdCalendar200Ok object, final Calendar to) {
        try {
            GetCharactersCharacterIdCalendarEventIdOk event =
                    this.calendarApi
                            .getCharactersCharacterIdCalendarEventId(charID, object.getEventId(), this.datasource)
                            .execute()
                            .body();
            to.add(ESITransformer.transform(object, event));
        }
        catch (IOException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            LOG.warn(e.getLocalizedMessage());
        }
    }

    public boolean postCalendarEvent(Long charID, Long eventID, Calendar.Event.Response response) {
        try {
            PutCharactersCharacterIdCalendarEventIdResponse.ResponseEnum re;
            switch (response) {
                case ACCEPTED:
                    re = PutCharactersCharacterIdCalendarEventIdResponse.ResponseEnum.ACCEPTED;
                    break;
                case DECLINED:
                    re = PutCharactersCharacterIdCalendarEventIdResponse.ResponseEnum.DECLINED;
                    break;
                case TENTATIVE:
                    re = PutCharactersCharacterIdCalendarEventIdResponse.ResponseEnum.TENTATIVE;
                    break;
                default:
                    re = null;
                    break;
            }
            if (null == re) {
                return false;
            }

            PutCharactersCharacterIdCalendarEventIdResponse r =
                    new PutCharactersCharacterIdCalendarEventIdResponse().response(re);
            this.calendarApi
                    .putCharactersCharacterIdCalendarEventId(charID.intValue(), eventID.intValue(), r, this.datasource)
                    .execute();
            return true;
        }
        catch (IOException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            LOG.warn(e.getLocalizedMessage());
            return false;
        }
    }

    private void addPortraits(final Character to) {
        try {
            GetCharactersCharacterIdPortraitOk portraits =
                    this.characterApi
                            .getCharactersCharacterIdPortrait(to.getId().intValue(), this.datasource)
                            .execute()
                            .body();
            to.setPortrait64(portraits.getPx64x64());
            to.setPortrait128(portraits.getPx128x128());
            to.setPortrait256(portraits.getPx256x256());
            to.setPortrait512(portraits.getPx512x512());
        }
        catch (IOException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            LOG.warn(e.getLocalizedMessage());
        }
    }

    private void addHistory(final Character to) {
        try {
            List<GetCharactersCharacterIdCorporationhistory200Ok> history =
                    this.characterApi
                            .getCharactersCharacterIdCorporationhistory(to.getId().intValue(), this.datasource)
                            .execute()
                            .body();
            for (GetCharactersCharacterIdCorporationhistory200Ok h: history) {
                to.add(ESITransformer.transform(h));
            }
        }
        catch (IOException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            LOG.warn(e.getLocalizedMessage());
        }
    }
}
