package com.clouway.oauth2.client

import com.clouway.oauth2.client.adapter.GoogleOAuthCredentialsFactory
import com.clouway.oauth2.client.core.OAuthClientConfig
import com.clouway.oauth2.client.core.OAuthCredentialsFactory
import com.google.api.client.http.HttpExecuteInterceptor
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class BearerTokenInterceptor(private val oauthCredentialsFactory: OAuthCredentialsFactory) : HttpRequestInitializer, HttpExecuteInterceptor {

    companion object {
        fun newGoogleInterceptor(config: OAuthClientConfig): BearerTokenInterceptor {
            return BearerTokenInterceptor(GoogleOAuthCredentialsFactory(config))
        }
    }

    override fun initialize(request: HttpRequest) {
        request.interceptor = this
    }

    override fun intercept(request: HttpRequest) {
        val possibleCredentials = oauthCredentialsFactory.create()
        if (!possibleCredentials.isPresent) {
            return
        }
        request.headers.set("Authorization", listOf("Bearer " + possibleCredentials.get().accessToken))
    }
}