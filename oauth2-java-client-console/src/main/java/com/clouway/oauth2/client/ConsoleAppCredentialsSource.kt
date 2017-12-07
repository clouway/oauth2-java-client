package com.clouway.oauth2.client

import com.clouway.oauth2.client.core.Credentials
import com.clouway.oauth2.client.core.OAuthClientConfig
import com.clouway.oauth2.client.core.OAuthHttpClient
import com.clouway.oauth2.client.core.CredentialsSource
import java.util.*

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
internal class ConsoleAppCredentialsSource(private val config: OAuthClientConfig, private val oauthHttpClient: OAuthHttpClient) : CredentialsSource {

    override fun getCredentials(instantTime: Date): Credentials {
        val possibleCredentials = config.credentialsStorage.getCredentials()
        if (!possibleCredentials.isPresent) {
            val server = JettyServer(8089)
            try {
                server.startServer()
                oauthHttpClient.authorizeUser(server.callbackUri, config.scopes)

                //wait for user to input the auth code
                println("Enter authorization code below:")
                val scanner = Scanner(System.`in`)
                val authCode = scanner.nextLine()

                val response = oauthHttpClient.newTokenRequest(authCode, server.callbackUri)

                config.credentialsStorage.saveCredentials(Credentials(response.accessToken, response.refreshToken, response.expirationDate))
                return Credentials(response.accessToken, response.refreshToken, response.expirationDate)

            } finally {
                server.stopServer()
            }
        }

        val credentials = possibleCredentials.get()

        if (credentials.areExpired(instantTime)) {
            val response = oauthHttpClient.refreshToken(credentials.refreshToken)

            //update credentials in the store
            config.credentialsStorage.saveCredentials(Credentials(response.accessToken, credentials.refreshToken, response.expirationDate))
            return Credentials(response.accessToken, response.accessToken, response.expirationDate)
        }

        return credentials
    }
}