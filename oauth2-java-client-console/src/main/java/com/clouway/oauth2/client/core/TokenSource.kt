package com.clouway.oauth2.client.core

/**
 * TokenSource is the source of tokens where token can be provided
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
interface TokenSource {

    /**
     * Return available token bi given configuration
     *
     * @return the available token access token
     */
    fun getToken(): String
}