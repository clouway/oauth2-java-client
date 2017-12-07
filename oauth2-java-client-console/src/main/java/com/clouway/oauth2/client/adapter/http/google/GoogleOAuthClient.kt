package com.clouway.oauth2.client.adapter.http.google

import com.clouway.oauth2.client.core.*
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.RefreshTokenRequest
import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.http.*
import com.google.api.client.json.gson.GsonFactory
import com.google.common.base.Optional
import com.google.gson.JsonParser
import java.awt.Desktop
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.URI

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class GoogleOAuthClient(private val client: String, private val secret: String, private val transport: HttpTransport) : OAuthClient {
    private val TOKEN_SERVICE_URL = "https://www.googleapis.com/oauth2/v4/token"
    private val AUTHORIZATION_CODE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
    private val TOKEN_INFO_SERVICE_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo"

    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    override fun authorizeUser(redirectUrl: String, scopes: Set<String>) {
        val authFlow = getAuthorizationFlow(client, secret)
        val authUrl = authFlow.newAuthorizationUrl()
                .set("access_type", "offline")
                .set("prompt", "consent")
                .setRedirectUri(redirectUrl)
                .setScopes(scopes).build()

        openInBrowser(authUrl)
    }

    override fun newTokenRequest(authCode: String, redirectUrl: String): TokenResponse {
        val flow = getAuthorizationFlow(client, secret)

        val response: com.google.api.client.auth.oauth2.TokenResponse
        try {
            response = flow.newTokenRequest(authCode).setRedirectUri(redirectUrl).execute()
        } catch (e: TokenResponseException) {
            throw AuthorizationCodeIsInvalidException("Cannot generate access token for auth code: " + authCode)
        }

        return TokenResponse(response.accessToken, response.refreshToken)
    }

    override fun refreshToken(refreshToken: String): String {
        val response: com.google.api.client.auth.oauth2.TokenResponse
        try {
            response = RefreshTokenRequest(transport, JSON_FACTORY, GenericUrl(TOKEN_SERVICE_URL), refreshToken)
                    .setClientAuthentication(BasicAuthentication(client, secret))
                    .execute()
        } catch (e: TokenResponseException) {
            throw RefreshTokenIsInvalidException("Cannot refresh the access token with: " + refreshToken)
        }

        return response.accessToken
    }

    override fun getTokenInfo(accessToken: String): Optional<TokenInfo> {
        val tokenInfo: HttpResponse
        try {
            tokenInfo = transport.createRequestFactory()
                    .buildGetRequest(GenericUrl(TOKEN_INFO_SERVICE_URL + "?access_token=" + accessToken)).execute()

        } catch (e: HttpResponseException) {
            return Optional.absent<TokenInfo>()
        }

        val json = JsonParser().parse(tokenInfo.parseAsString()).asJsonObject
        val issuedTo = json.get("issued_to").asString
        val expireIn = json.get("expires_in").asInt
        val scopes = json.get("scope").asString.split(" ").toSet()
        return Optional.of(TokenInfo(issuedTo, scopes, expireIn))
    }

    private fun getAuthorizationFlow(client: String, secret: String): AuthorizationCodeFlow {
        try {
            return AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    transport,
                    JSON_FACTORY,
                    GenericUrl(TOKEN_SERVICE_URL),
                    BasicAuthentication(client, secret),
                    client,
                    AUTHORIZATION_CODE_URL)
                    .build()
        } catch (e: IOException) {
            throw IllegalStateException("Authorization flow cannot be initialized.")
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot open $url in browser.")
        }
    }
}