package com.clouway.oauth2.client.core

import java.util.*

/**
 * CredentialsSource is the source of credentials where credentials can be provided
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
interface CredentialsSource {

    /**
     * Return available token by given date
     *
     * @return the available user credentials
     */
    fun getCredentials(instantTime: Date): Credentials
}