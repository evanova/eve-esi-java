package org.devfleet.esi.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.devfleet.esi.KillMail;
import org.devfleet.esi.Mail;
import org.devfleet.esi.Mailbox;
import org.devfleet.esi.api.KillmailsApi;
import org.devfleet.esi.api.MailApi;
import org.devfleet.esi.model.GetCharactersCharacterIdKillmailsRecent200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdMail200Ok;
import org.devfleet.esi.model.GetCharactersCharacterIdMailLabelsOk;
import org.devfleet.esi.model.GetCharactersCharacterIdMailLabelsOkLabels;
import org.devfleet.esi.model.GetCharactersCharacterIdMailMailIdOk;
import org.devfleet.esi.model.GetKillmailsKillmailIdKillmailHashOk;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class MailRetrofit {

    private final MailApi mailApi;
    private final KillmailsApi killMailApi;
    private final String datasource;

    public MailRetrofit(final Retrofit rf, final String datasource) {

        this.mailApi = rf.create(MailApi.class);
        this.killMailApi = rf.create(KillmailsApi.class);
        this.datasource = datasource;
    }

    public boolean deleteMail(Long charID, Long mailID) throws IOException {
        return this.mailApi
            .deleteCharactersCharacterIdMailMailId(
                charID.intValue(),
                mailID.intValue(),
                this.datasource)
            .execute()
            .isSuccessful();
    }

    public List<Mail> getMails(Long charID, Long afterMailID, String... labels) throws IOException {
        org.devfleet.esi.client.CollectionFormats.CSVParams params = null;
        if (ArrayUtils.isNotEmpty(labels)) {
            params = new org.devfleet.esi.client.CollectionFormats.CSVParams();
            params.setParams(Arrays.asList(labels));
        }

        final Response<List<GetCharactersCharacterIdMail200Ok>> r =
                this.mailApi.getCharactersCharacterIdMail(
                        charID.intValue(),
                        params,
                        afterMailID.intValue(),
                        this.datasource)
                        .execute();
        if (!r.isSuccessful()) {
            return null;
        }

        final List<Mail> mails = new ArrayList<>();
        for (GetCharactersCharacterIdMail200Ok object: r.body()) {
            mails.add(MailTransformer.transform(object));
        }
        return mails;
    }

    public List<Mailbox> getMailboxes(Long charID) throws IOException {
        final Response<GetCharactersCharacterIdMailLabelsOk> r =
                mailApi.getCharactersCharacterIdMailLabels(charID.intValue(), this.datasource)
                        .execute();
        if (!r.isSuccessful()) {
            return null;
        }

        final List<Mailbox> mailboxes = new ArrayList<>();
        /*for (GetCharactersCharacterIdMailLists200OkObject object:
                this.mailApi
                .getCharactersCharacterIdMailLists(charID.intValue(), this.datasource)
                .execute()
                .body()) {
            mailboxes.add(ESITransformer.transform(object));*/
            for (GetCharactersCharacterIdMailLabelsOkLabels object: r.body().getLabels()) {
            mailboxes.add(MailTransformer.transform(object));
        }
        return mailboxes;
    }

    public Mail getMailContent(Long charID, Long mailID) throws IOException {
        final Response<GetCharactersCharacterIdMailMailIdOk> r =
                mailApi.getCharactersCharacterIdMailMailId(
                        charID.intValue(),
                        mailID.intValue(),
                        this.datasource)
                        .execute();
        if (!r.isSuccessful()) {
            return null;
        }
        return MailTransformer.transform(r.body(), mailID);
    }

    public Integer postMail(Long charID, Mail mail) throws IOException {
        final Response<Integer> r =
                mailApi.postCharactersCharacterIdMail(
                        charID.intValue(),
                        MailTransformer.transform(mail),
                        this.datasource)
                        .execute();
        if (!r.isSuccessful()) {
            return null;
        }
        return r.body();
    }

    public boolean updateMail(Long charID, Mail mail) throws IOException {
        return mailApi.putCharactersCharacterIdMailMailId(
                charID.intValue(),
                mail.getId().intValue(),
                MailTransformer.transform2(mail),
                this.datasource)
                .execute()
                .isSuccessful();
    }

    public boolean createMailbox(Long charID, Mailbox mailbox) throws IOException {
        //TODO
        return false;
    }

    public boolean updateMailbox(Long charID, Mailbox mailbox) throws IOException {
        //TODO
        return false;
    }

    public List<KillMail> getKillMails(Long charID, Integer maxCount, Long maxKillID, boolean withContent) throws IOException {
        final Response<List<GetCharactersCharacterIdKillmailsRecent200Ok>> r =
                killMailApi
                    .getCharactersCharacterIdKillmailsRecent(charID.intValue(), maxCount, maxKillID.intValue(), this.datasource)
                    .execute();
        if (!r.isSuccessful()) {
            return null;
        }

        final List<KillMail> kills = new ArrayList<>();
        for (GetCharactersCharacterIdKillmailsRecent200Ok m: r.body()) {
            KillMail km = MailTransformer.transform(m);
            if (withContent) {
                km = getKillMail(km);
            }
            kills.add(km);
        }
        return kills;
    }

    public KillMail getKillMail(KillMail killMail) throws IOException {
        final Response<GetKillmailsKillmailIdKillmailHashOk> r =
                killMailApi
                .getKillmailsKillmailIdKillmailHash(killMail.getId().intValue(), killMail.getHash(), this.datasource)
                .execute();
        if (!r.isSuccessful()) {
            return null;
        }

        //TODO - fill mail
        return MailTransformer.transform(r.body());
    }
}
