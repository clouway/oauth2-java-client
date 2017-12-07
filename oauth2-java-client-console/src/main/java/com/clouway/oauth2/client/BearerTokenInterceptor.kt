package com.clouway.oauth2.client

import com.clouway.oauth2.client.adapter.http.google.GoogleOAuthClient
import com.clouway.oauth2.client.core.OAuthClientConfig
import com.clouway.oauth2.client.core.TokenSource
import com.google.api.client.http.HttpExecuteInterceptor
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class BearerTokenInterceptor(private val tokenSource: TokenSource) : HttpRequestInitializer, HttpExecuteInterceptor {

    companion object {
        fun newGoogleInterceptor(config: OAuthClientConfig): BearerTokenInterceptor {
            return BearerTokenInterceptor(ConsoleAppTokenSource(config, GoogleOAuthClient(config.clientId, config.secret, NetHttpTransport())))
        }
    }

    override fun initialize(request: HttpRequest) {
        request.interceptor = this
    }

    override fun intercept(request: HttpRequest) {
        val accessToken = tokenSource.getToken()
        request.headers.set("Authorization", listOf("Bearer " + accessToken))
    }
}