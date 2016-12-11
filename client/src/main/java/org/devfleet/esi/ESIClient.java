package org.devfleet.esi;

import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.devfleet.esi.client.ApiClient;
import org.devfleet.esi.client.auth.OAuth;
import org.devfleet.esi.impl.ESIServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Note: this class will most likely be changed in the future.
public class ESIClient {

    private static final Logger LOG = LoggerFactory.getLogger(ESIClient.class);

    private final ApiClient apiClient;
    private final ESIService service;

    public ESIClient(final String clientID,
                     final String clientSecret,
                     final String redirectUri) {

        this.apiClient = new ApiClient("evesso");
        this.apiClient.createDefaultAdapter();
        this.apiClient.configureAuthorizationFlow(clientID, clientSecret, redirectUri);
        this.apiClient.registerAccessTokenListener(new OAuth.AccessTokenListener() {
            public void notify(BasicOAuthToken token) {
                onAccessTokenChanged(token.getAccessToken(), token.getRefreshToken());
            }
        });
        this.service = new ESIServiceImpl(apiClient);
    }

    public final ESIService getService() {
        return this.service;
    }

    public void setAccessToken(final String token) {
        this.apiClient.setAccessToken(token);
    }

    public void onAccessTokenChanged(final String accessToken, final String refreshToken) {

    }

}
