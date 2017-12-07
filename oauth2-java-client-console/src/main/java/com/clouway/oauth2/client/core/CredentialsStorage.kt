package com.clouway.oauth2.client.core

import com.google.common.base.Optional
import java.util.*

/**
 * Store user credentials
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
interface CredentialsStorage {

    /**
     * Save user credentials
     *
     * @param credentials the credentials that will be stored
     */
    fun saveCredentials(credentials: Credentials)

    /**
     * Return credentials, depends on the configuration of the implementation
     *
     * @return optional of credentials
     */
    fun getCredentials(): Optional<Credentials>
}

data class Credentials(val accessToken: String, val refreshToken: String, val expirationDate: Date) {
    fun areExpired(date: Date): Boolean {
        return this.expirationDate.before(date)
    }
}