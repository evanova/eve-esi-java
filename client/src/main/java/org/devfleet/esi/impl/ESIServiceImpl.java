package org.devfleet.esi.impl;

import org.devfleet.esi.Calendar;
import org.devfleet.esi.Character;
import org.devfleet.esi.Corporation;
import org.devfleet.esi.ESIService;
import org.devfleet.esi.KillMail;
import org.devfleet.esi.Mail;
import org.devfleet.esi.Mailbox;
import org.devfleet.esi.client.ApiClient;

import java.util.List;

public class ESIServiceImpl implements ESIService {

    private final CharacterAPIImpl characterImpl;
    private final CorporationAPIImpl corporationImpl;
    private final MailAPIImpl mailImpl;

    public ESIServiceImpl(final ApiClient client) {
        this(client, "tranquility");
    }

    public ESIServiceImpl(final ApiClient client, final String datasource) {
        this.characterImpl = new CharacterAPIImpl(client, datasource);
        this.corporationImpl = new CorporationAPIImpl(client, datasource);
        this.mailImpl = new MailAPIImpl(client, datasource);
    }

    @Override
    public Character getCharacter(Long charID) {
        return this.characterImpl.getCharacter(charID);
    }

    @Override
    public Calendar getCalendar(Long charID, Long afterEventID) {
        return this.characterImpl.getCalendar(charID, afterEventID);
    }

    @Override
    public boolean postCalendarEvent(Long charID, Long eventID, Calendar.Event.Response response) {
        return this.characterImpl.postCalendarEvent(charID, eventID, response);
    }

    @Override
    public Corporation getCorporation(Long corpID) {
        return this.corporationImpl.getCorporation(corpID);
    }

    @Override
    public List<Corporation.Member> getMembers(Long corpID) {
        return this.corporationImpl.getMembers(corpID);
    }

    @Override
    public boolean deleteMail(Long charID, Long mailID) {
        return this.mailImpl.deleteMail(charID, mailID);
    }

    @Override
    public List<Mail> getMails(Long charID, Long afterMailID, String... labels) {
        return this.mailImpl.getMails(charID, afterMailID, labels);
    }

    @Override
    public List<Mailbox> getMailboxes(Long charID) {
        return this.mailImpl.getMailboxes(charID);
    }

    @Override
    public Mail getMailContent(Long charID, Long mailID) {
        return this.mailImpl.getMailContent(charID, mailID);
    }

    @Override
    public Integer postMail(Long charID, Mail mail) {
        return this.mailImpl.postMail(charID, mail);
    }

    @Override
    public boolean updateMail(Long charID, Mail mail) {
        return this.mailImpl.updateMail(charID, mail);
    }

    @Override
    public boolean createMailbox(Long charID, Mailbox mailbox) {
        return (null == mailbox.getId())  ?
                this.mailImpl.createMailbox(charID, mailbox) : this.mailImpl.updateMailbox(charID, mailbox);
    }

    @Override
    public List<KillMail> getKillMails(Long charID, Integer maxCount, Long maxKillID, boolean withContent) {
        return this.mailImpl.getKillMails(charID, maxCount, maxKillID, withContent);
    }

    @Override
    public KillMail getKillMail(KillMail km) {
        return this.mailImpl.getKillMail(km);
    }
}
