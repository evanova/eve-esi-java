package org.devfleet.esi.impl;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.devfleet.esi.Calendar;
import org.devfleet.esi.Character;
import org.devfleet.esi.Corporation;
import org.devfleet.esi.ESIService;
import org.devfleet.esi.ESIStore;
import org.devfleet.esi.ESIToken;
import org.devfleet.esi.KillMail;
import org.devfleet.esi.Location;
import org.devfleet.esi.Mail;
import org.devfleet.esi.Mailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ESIRetrofit implements ESIService {

    private static final Logger LOG = LoggerFactory.getLogger(ESIRetrofit.class);

    private static final class ClientInterceptor implements Interceptor {
        private final ESIRetrofit cr;

        public ClientInterceptor(ESIRetrofit cr) {
            this.cr = cr;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder =
                    chain
                        .request()
                        .newBuilder();
            final ESIToken token = cr.store.get(cr.refresh);
            if (null != token) {
                builder.addHeader("Authorization", "Bearer " + token.getAccessToken());
            }
            return chain.proceed(builder.build());
        }
    }

    private static final class RetryInterceptor implements Interceptor {
        private final ESIRetrofit cr;

        public RetryInterceptor(ESIRetrofit cr) {
            this.cr = cr;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response r = chain.proceed(chain.request());
            if (r.isSuccessful()) {
                return r;
            }
            if (r.body().string().contains("invalid_token")) {
                cr.refreshToken();
                r = chain.proceed(chain.request());
            }
            return r;
        }
    }
    private static final class UserAgentInterceptor implements  Interceptor {
        private final String host;
        private final String agent;

        public UserAgentInterceptor(String host, String agent) {
            this.host = host;
            this.agent = agent;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder =
                    chain
                            .request()
                            .newBuilder()
                            .addHeader("Host", host)
                            .addHeader("User-Agent", agent);
            return chain.proceed(builder.build());
        }
    }

    private final CharacterRetrofit characterApi;
    private final CorporationRetrofit corporationApi;
    private final MailRetrofit mailAPIApi;

    private final String host;

    private final OAuth20Service oAuth;
    private final ESIStore store;
    private final String refresh;

    public ESIRetrofit(
            final String host,
            final String login,
            final OAuth20Service oAuth,
            final ESIStore store,
            final String agent,
            final long timeout,
            final String refresh) {

        Validate.isTrue(StringUtils.isNotBlank(host), "host parameter cannot be empty.");
        Validate.isTrue(StringUtils.isNotBlank(login), "login parameter cannot be empty.");
        Validate.isTrue(StringUtils.isNotBlank(agent), "agent parameter cannot be empty.");

        Validate.notNull(oAuth, "oAuth parameter cannot be null.");
        Validate.notNull(store, "store parameter cannot be null.");

        this.host = host;
        this.store = store;
        this.refresh = refresh;
        this.oAuth = oAuth;

        OkHttpClient.Builder retrofitClient =
                new OkHttpClient.Builder()
                        .readTimeout(timeout, TimeUnit.MILLISECONDS)
                        .addInterceptor(new UserAgentInterceptor(host, agent))
                        .addInterceptor(new ClientInterceptor(this))
                        .addInterceptor(new RetryInterceptor(this));
        if (LOG.isDebugEnabled()) {
            retrofitClient = retrofitClient
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        final Gson gson = new Gson();
        final Converter.Factory mapper = GsonConverterFactory.create(gson);

        Retrofit rf =
                new Retrofit.Builder()
                        .baseUrl("https://" + host + "/")
                        .addConverterFactory(mapper)
                        .client(retrofitClient.build())
                        .build();

        this.characterApi = new CharacterRetrofit(rf, "tranquility");
        this.corporationApi = new CorporationRetrofit(rf, "tranquility");
        this.mailAPIApi = new MailRetrofit(rf, "tranquility");
    }

    @Override
    public Character getCharacter(Long charID) {
        try {
            verify();
            return this.characterApi.getCharacter(charID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public Location getCharacterLocation(Long charID) {
        try {
            verify();
            return this.characterApi.getCharacterLocation(charID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public Calendar getCalendar(Long charID, Long afterEventID) {
        try {
            verify();
            return this.characterApi.getCalendar(charID, afterEventID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    @Override
    public boolean postCalendarEvent(Long charID, Long eventID, Calendar.Event.Response response) {
        try {
            verify();
            return this.characterApi.postCalendarEvent(charID, eventID, response);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public Corporation getCorporation(Long corpID) {
        try {
            verify();
            return this.corporationApi.getCorporation(corpID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public List<Corporation.Member> getMembers(Long corpID) {
        try {
            verify();
            return this.corporationApi.getMembers(corpID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean deleteMail(Long charID, Long mailID) {
        try {
            verify();
            return this.mailAPIApi.deleteMail(charID, mailID);

        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public List<Mail> getMails(Long charID, Long afterMailID, String... labels) {
        try {
            verify();
            return this.mailAPIApi.getMails(charID, afterMailID, labels);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Mailbox> getMailboxes(Long charID) {
        try {
            verify();
            return this.mailAPIApi.getMailboxes(charID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Mail getMailContent(Long charID, Long mailID) {
        try {
            verify();
            return this.mailAPIApi.getMailContent(charID, mailID);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public Integer postMail(Long charID, Mail mail) {
        try {
            verify();
            return this.mailAPIApi.postMail(charID, mail);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public boolean updateMail(Long charID, Mail mail) {
        try {
            verify();
            return this.mailAPIApi.updateMail(charID, mail);

        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean createMailbox(Long charID, Mailbox mailbox) {
        try {
            verify();
            return this.mailAPIApi.createMailbox(charID, mailbox);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public List<KillMail> getKillMails(Long charID, Integer maxCount, Long maxKillID, boolean withContent) {
        try {
            verify();
            return this.mailAPIApi.getKillMails(charID, maxCount, maxKillID, withContent);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public KillMail getKillMail(KillMail killMail) {
        try {
            verify();
            return this.mailAPIApi.getKillMail(killMail);
        }
        catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
    }

    private void verify() throws IOException {
        try {
            verifyImpl();
        }
        catch (OAuthException e) {
            throw new IOException(e);
        }
    }

    private void verifyImpl() throws IOException, OAuthException {
        if (StringUtils.isBlank(this.refresh)) {
            throw new IOException("No refresh token available");
        }

        ESIToken stored = this.store.get(this.refresh);
        if (null == stored) {
            stored = new ESIToken().setRefreshToken(this.refresh);
            this.store.save(stored);
        }

        if (StringUtils.isBlank(stored.getAccessToken())) {
            updateToken(stored, this.oAuth.refreshAccessToken(this.refresh));
            this.store.save(stored);
        }
    }

    private void refreshToken() throws IOException {
        ESIToken stored = this.store.get(this.refresh);
        updateToken(stored, this.oAuth.refreshAccessToken(stored.getRefreshToken()));
        this.store.save(stored);
    }

    private static void updateToken(final ESIToken token, OAuth2AccessToken with) {
        LOG.debug("updateToken {} -> {} ", token.getAccessToken(), with.getAccessToken());
        token.setAccessToken(with.getAccessToken());
        token.setExpiresIn(with.getExpiresIn());
        token.setScope(with.getScope());
        token.setTokenType(with.getTokenType());
    }
}
