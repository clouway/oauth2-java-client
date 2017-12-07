package com.clouway.oauth2.client

import com.clouway.oauth2.client.core.Credentials
import com.clouway.oauth2.client.core.OAuthClient
import com.clouway.oauth2.client.core.OAuthClientConfig
import com.clouway.oauth2.client.core.TokenSource
import java.util.Scanner

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class ConsoleAppTokenSource(private val config: OAuthClientConfig, private val oauthClient: OAuthClient) : TokenSource {

    override fun getToken(): String {
        val possibleCredentials = config.credentialsStore.getCredentials()
        if (!possibleCredentials.isPresent) {
            val server = JettyServer(8089)
            try {
                server.startServer()
                oauthClient.authorizeUser(server.callbackUri, config.scopes)

                //wait for user to input the auth code
                println("Enter authorization code below:")
                val scanner = Scanner(System.`in`)
                val authCode = scanner.nextLine()

                val response = oauthClient.newTokenRequest(authCode, server.callbackUri)

                config.credentialsStore.saveCredentials(Credentials(response.accessToken, response.refreshToken))
                return response.accessToken

            } finally {
                server.stopServer()
            }
        }

        val credentials = possibleCredentials.get()

        val possibleTokenInfo = oauthClient.getTokenInfo(credentials.accessToken)
        if (!possibleTokenInfo.isPresent) {
            val refreshedToken = oauthClient.refreshToken(credentials.refreshToken)

            //update credentials in the store
            config.credentialsStore.saveCredentials(Credentials(refreshedToken, credentials.refreshToken))
            return refreshedToken
        }

        return credentials.accessToken
    }
}