package com.clouway.oauth2.client.core

import com.google.common.base.Optional

/**
 * A Factory that provides OAuth credentials
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
interface OAuthCredentialsFactory {

    /**
     * Create new oauth credentials
     *
     * @return optional of credentials
     */
    fun create(): Optional<Credentials>
}