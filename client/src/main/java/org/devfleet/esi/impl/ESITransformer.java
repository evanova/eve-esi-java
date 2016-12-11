package org.devfleet.esi.impl;

import org.devfleet.esi.Calendar;
import org.devfleet.esi.Character;
import org.devfleet.esi.Corporation;
import org.devfleet.esi.KillMail;
import org.devfleet.esi.Mail;
import org.devfleet.esi.Mailbox;
import org.devfleet.esi.model.CharacterscharacterIdmailRecipients;
import org.devfleet.esi.model.CharacterscharacterIdmailRecipients1;
import org.devfleet.esi.model.GetCharactersCharacterIdCalendar200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdCalendarEventIdOk;
import org.devfleet.esi.model.GetCharactersCharacterIdCorporationhistory200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdKillmailsRecent200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdMail200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdMailLabelsOkLabels;
import org.devfleet.esi.model.GetCharactersCharacterIdMailMailIdOk;
import org.devfleet.esi.model.GetCharactersCharacterIdMailMailIdOkRecipients;
import org.devfleet.esi.model.GetCharactersCharacterIdOk;
import org.devfleet.esi.model.GetCorporationsCorporationIdAlliancehistory200Ok;
import org.devfleet.esi.model.GetCorporationsCorporationIdMembers200Ok;
import org.devfleet.esi.model.GetCorporationsCorporationIdOk;
import org.devfleet.esi.model.PostCharactersCharacterIdMailMail;
import org.devfleet.esi.model.PutCharactersCharacterIdMailMailIdContents;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

class ESITransformer {

    private ESITransformer() {}

    public static Character transform(final long charID, final GetCharactersCharacterIdOk c) {
        final Character character = new Character(charID);

        return character;
    }

    public static Corporation transform(final long corpID, final GetCorporationsCorporationIdOk c) {
        final Corporation corporation = new Corporation(corpID);

        return corporation;
    }

    public static Character.History transform(GetCharactersCharacterIdCorporationhistory200Ok h) {
        final Character.History history = new Character.History();
        return history;
    }

    public static Corporation.History transform(GetCorporationsCorporationIdAlliancehistory200Ok h) {
        final Corporation.History history = new Corporation.History();
        return history;
    }

    public static Corporation.Member transform(GetCorporationsCorporationIdMembers200Ok m) {
        final Corporation.Member member = new Corporation.Member();

        return member;
    }

    public static Calendar.Event transform(
            GetCharactersCharacterIdCalendar200Ok meta,
            GetCharactersCharacterIdCalendarEventIdOk details) {
        final Calendar.Event event = new Calendar.Event(Long.valueOf(meta.getEventId()));

        return event;
    }

    public static Mail transform(GetCharactersCharacterIdMailMailIdOk object, final Long mailId) {
        Mail mail = new Mail();
        mail.setId(mailId);
        mail.setFrom(object.getFrom().longValue());
        mail.setRead(object.getRead());
        mail.setSubject(object.getSubject());
        mail.setTimestamp(object.getTimestamp().getMillis());

        for (GetCharactersCharacterIdMailMailIdOkRecipients r: object.getRecipients()) {
            mail.addRecipient(r.getRecipientId().longValue(), r.getRecipientType().toString());
        }
        return mail;
    }

    public static Mail transform(GetCharactersCharacterIdMail200Ok object) {
        Mail mail = new Mail();
        mail.setFrom(object.getFrom().longValue());
        mail.setId(object.getMailId());
        mail.setRead(object.getIsRead());
        mail.setSubject(object.getSubject());
        mail.setTimestamp(object.getTimestamp().getMillis());

        for (CharacterscharacterIdmailRecipients r: object.getRecipients()) {
            mail.addRecipient(r.getRecipientId().longValue(), r.getRecipientType().toString());
        }
        return mail;
    }

    public static PostCharactersCharacterIdMailMail transform(final Mail mail) {
        PostCharactersCharacterIdMailMail object = new PostCharactersCharacterIdMailMail();
        object.setSubject(mail.getSubject());
        object.setApprovedCost(mail.getCost());
        object.setBody(mail.getBody());

        List<CharacterscharacterIdmailRecipients1> recipients = new ArrayList<>();
        for (Mail.Recipient r: mail.getRecipients()) {
            final CharacterscharacterIdmailRecipients1 r1 = new CharacterscharacterIdmailRecipients1();
            r1.setRecipientId(r.getId().intValue());
            r1.setRecipientType(recipientType(r.getType()));
            recipients.add(r1);
        }
        object.setRecipients(recipients);
        return object;
    }

    public static PutCharactersCharacterIdMailMailIdContents transform2(final Mail mail) {
        PutCharactersCharacterIdMailMailIdContents contents = new PutCharactersCharacterIdMailMailIdContents();
        contents.setLabels(new ArrayList<>(mail.getLabels().keySet()));
        contents.setRead(mail.getRead());
        return contents;
    }

    public static KillMail transform(GetCharactersCharacterIdKillmailsRecent200Ok object) {
        KillMail km = new KillMail();
        km.setHash(object.getKillmailHash());
        km.setId(object.getKillmailId().longValue());
        return km;
    }

    public static Mailbox transform(GetCharactersCharacterIdMailLabelsOkLabels object) {
        final Mailbox m = new Mailbox();
        m.setId(object.getLabelId().longValue());
        //m.setColor(object.getColor().val);
        m.setLabel(object.getName());
        m.setUnread(object.getUnreadCount());
        return m;
    }

    private static CharacterscharacterIdmailRecipients1.RecipientTypeEnum recipientType(final String type) {
        for (CharacterscharacterIdmailRecipients1.RecipientTypeEnum e: EnumSet.allOf(CharacterscharacterIdmailRecipients1.RecipientTypeEnum.class)) {
            if (e.name().equalsIgnoreCase(type)) {
                return e;
            }
        }
        return null;
    }
}
