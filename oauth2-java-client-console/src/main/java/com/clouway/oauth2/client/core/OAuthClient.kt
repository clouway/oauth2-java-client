package com.clouway.oauth2.client.core

import com.google.common.base.Optional

/**
 * Authorization flow that provide OAuth authorization sequence
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
interface OAuthClient {

    /**
     * Authorize user and redirect to given redirect url.
     * Note: this method is adapted to be used with UI
     *
     * @param redirectUrl the redirect url where the response will be received
     * @param scopes the scopes that will be granted for user
     */
    fun authorizeUser(redirectUrl: String, scopes: Set<String>)

    /**
     * Create new access token and refresh token
     *
     * @param authCode authorization code
     * @param redirectUrl the redirect url where the response will be received
     * @return  TokenResponse with access token and refresh token
     */
    @Throws(AuthorizationCodeIsInvalidException::class)
    fun newTokenRequest(authCode: String, redirectUrl: String): TokenResponse

    /**
     * Refresh the access token by given refresh token
     *
     * @param refreshToken refresh token that will be used to refresh the access token
     * @return the new  access token
     */
    @Throws(RefreshTokenIsInvalidException::class)
    fun refreshToken(refreshToken: String): String

    /**
     * Find token info by given access token
     *
     * @param accessToken the access token for which token info will be returned
     * @return optional of TokenInfo
     */
    fun getTokenInfo(accessToken: String): Optional<TokenInfo>
}

data class TokenInfo(val issuedTo: String, val scopes: Set<String>, val expireIn: Int)

data class TokenResponse(val accessToken: String, val refreshToken: String)