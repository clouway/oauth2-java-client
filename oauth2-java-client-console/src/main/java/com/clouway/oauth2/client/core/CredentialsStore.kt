package com.clouway.oauth2.client.core

import com.google.common.base.Optional

/**
 * Store user credentials
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
interface CredentialsStore {

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

data class Credentials(val accessToken: String, val refreshToken: String)