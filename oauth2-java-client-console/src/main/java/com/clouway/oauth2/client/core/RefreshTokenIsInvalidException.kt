package com.clouway.oauth2.client.core

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class RefreshTokenIsInvalidException(override val message: String) : Exception()