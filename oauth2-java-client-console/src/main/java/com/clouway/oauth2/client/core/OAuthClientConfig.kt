package com.clouway.oauth2.client.core

import com.google.common.base.Optional

/**
 * OAuthHttpClient config contains configuration for the credentials that will be created
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class OAuthClientConfig(val clientId: String, val secret: String, val scopes: Set<String>, val credentialsStorage: CredentialsStorage) {

    companion object Builder {
        private var clientId = ""
        private var secret = ""
        private var scopes = setOf<String>()
        private var credentialsStorage: CredentialsStorage = DefaultCredentialsStorage()

        fun setClient(clientId: String): Builder {
            Builder.clientId = clientId
            return this
        }

        fun setSecret(secret: String): Builder {
            Builder.secret = secret
            return this
        }

        fun setScopes(scopes: Set<String>): Builder {
            Builder.scopes = scopes
            return this
        }

        fun setCredentialsStore(storage: CredentialsStorage): Builder {
            credentialsStorage = storage
            return this
        }

        fun build(): OAuthClientConfig = OAuthClientConfig(clientId, secret, scopes, credentialsStorage)
    }
}

internal class DefaultCredentialsStorage : CredentialsStorage {
    override fun saveCredentials(credentials: Credentials) {}

    override fun getCredentials(): Optional<Credentials> = Optional.absent()
}