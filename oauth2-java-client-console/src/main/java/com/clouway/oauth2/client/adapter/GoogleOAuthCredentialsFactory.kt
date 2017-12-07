package com.clouway.oauth2.client.adapter

import com.clouway.oauth2.client.ConsoleAppCredentialsSource
import com.clouway.oauth2.client.adapter.http.google.GoogleOAuthHttpClient
import com.clouway.oauth2.client.core.Credentials
import com.clouway.oauth2.client.core.OAuthClientConfig
import com.clouway.oauth2.client.core.OAuthCredentialsFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.common.base.Optional
import java.util.*

/**
 * Generate credentials for given OAuth client configuration
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class GoogleOAuthCredentialsFactory(private val config: OAuthClientConfig) : OAuthCredentialsFactory {

    override fun create(): Optional<Credentials> {
        val httpClient = GoogleOAuthHttpClient(config.clientId, config.secret, NetHttpTransport())
        val credentialsSource = ConsoleAppCredentialsSource(config, httpClient)

        return Optional.of(credentialsSource.getCredentials(Date()))
    }
}