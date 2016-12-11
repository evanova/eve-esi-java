package org.devfleet.esi.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.devfleet.esi.KillMail;
import org.devfleet.esi.Mail;
import org.devfleet.esi.Mailbox;
import org.devfleet.esi.api.KillmailsApi;
import org.devfleet.esi.api.MailApi;
import org.devfleet.esi.client.ApiClient;
import org.devfleet.esi.model.GetCharactersCharacterIdKillmailsRecent200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdMail200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdMailLabelsOkLabels;
import org.devfleet.esi.model.GetKillmailsKillmailIdKillmailHashOk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class MailAPIImpl {

    private static Logger LOG = LoggerFactory.getLogger(MailAPIImpl.class);

    private final MailApi mailApi;
    private final KillmailsApi killMailApi;
    private final String datasource;

    public MailAPIImpl(final ApiClient client, final String datasource) {

        this.mailApi = client.createService(MailApi.class);
        this.killMailApi = client.createService(KillmailsApi.class);
        this.datasource = datasource;
    }

    public boolean deleteMail(Long charID, Long mailID) {
        try {
            return this.mailApi
                .deleteCharactersCharacterIdMailMailId(
                    charID.intValue(),
                    mailID.intValue(),
                    this.datasource)
                .execute()
                .isSuccessful();
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    public List<Mail> getMails(Long charID, Long afterMailID, String... labels) {
        try {
            org.devfleet.esi.client.CollectionFormats.CSVParams params = null;
            if (ArrayUtils.isNotEmpty(labels)) {
                params = new org.devfleet.esi.client.CollectionFormats.CSVParams();
                params.setParams(Arrays.asList(labels));
            }
            final List<Mail> mails = new ArrayList<>();
            for (GetCharactersCharacterIdMail200Ok object:
                this.mailApi.getCharactersCharacterIdMail(
                    charID.intValue(),
                    params,
                    afterMailID.intValue(),
                    this.datasource)
                    .execute().body()) {
                mails.add(EsiTransformer.transform(object));
            }
            return mails;
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Mailbox> getMailboxes(Long charID) {
        try {
            final List<Mailbox> mailboxes = new ArrayList<>();
            /*for (GetCharactersCharacterIdMailLists200OkObject object:
                    this.mailApi
                    .getCharactersCharacterIdMailLists(charID.intValue(), this.datasource)
                    .execute()
                    .body()) {
                mailboxes.add(EsiTransformer.transform(object));*/
                for (GetCharactersCharacterIdMailLabelsOkLabels object:
                        mailApi.getCharactersCharacterIdMailLabels(charID.intValue(), this.datasource)
                        .execute()
                        .body()
                        .getLabels()) {
                mailboxes.add(EsiTransformer.transform(object));
            }
            return mailboxes;
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    public Mail getMailContent(Long charID, Long mailID) {
        try {
            return EsiTransformer.transform(
                    mailApi.getCharactersCharacterIdMailMailId(
                    charID.intValue(),
                    mailID.intValue(),
                    this.datasource)
                    .execute()
                    .body(), mailID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public Integer postMail(Long charID, Mail mail) {
        try {
            return mailApi.postCharactersCharacterIdMail(
                    charID.intValue(),
                    EsiTransformer.transform(mail),
                    this.datasource)
                    .execute()
                    .body();
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean updateMail(Long charID, Mail mail) {
        try {
            return mailApi.putCharactersCharacterIdMailMailId(
                    charID.intValue(),
                    mail.getId().intValue(),
                    EsiTransformer.transform2(mail),
                    this.datasource)
                    .execute()
                    .isSuccessful();
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    public boolean createMailbox(Long charID, Mailbox mailbox) {
        //TODO
        return false;
    }

    public boolean updateMailbox(Long charID, Mailbox mailbox) {
        //TODO
        return false;
    }

    public List<KillMail> getKillMails(Long charID, Integer maxCount, Long maxKillID, boolean withContent) {
        try {
            final List<KillMail> kills = new ArrayList<>();
            for (GetCharactersCharacterIdKillmailsRecent200Ok m:
                    killMailApi.getCharactersCharacterIdKillmailsRecent(charID.intValue(), maxCount, maxKillID.intValue(), this.datasource)
                    .execute()
                    .body()) {
                KillMail km = EsiTransformer.transform(m);
                if (withContent) {
                    km = getKillMail(km);
                }
                kills.add(km);
            }
            return kills;
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    public KillMail getKillMail(KillMail killMail) {
        try {
            GetKillmailsKillmailIdKillmailHashOk ok =
                killMailApi.getKillmailsKillmailIdKillmailHash(killMail.getId().intValue(), killMail.getHash(), this.datasource)
                    .execute()
                    .body();

            //TODO - fill mail
            return killMail;
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }
}
