package com.clouway.oauth2.client.adapter.http.google

import com.clouway.oauth2.client.adapter.http.google.GoogleOAuthClient
import com.clouway.oauth2.client.core.AuthorizationCodeIsInvalidException
import com.clouway.oauth2.client.core.RefreshTokenIsInvalidException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class GoogleOAuthClientTest {

    private var oauthClient = GoogleOAuthClient("::client::", "::secret::", NetHttpTransport())

    @Before
    fun setUp() {
        oauthClient = GoogleOAuthClient("::client::", "::secret::", NetHttpTransport())
    }

    @Test(expected = AuthorizationCodeIsInvalidException::class)
    fun sendNewTokenRequest() {
        oauthClient.newTokenRequest("::authCode::", "::url::")
    }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun sendRefreshTokenRequest() {
        oauthClient.refreshToken("::access_token::")
    }

    @Test
    fun sendTokenInfoRequest() {
        val transport = MockHttpTransport.Builder()
                .setLowLevelHttpResponse(
                        MockLowLevelHttpResponse()
                                .setStatusCode(200)
                                .setContentType("application/json")
                                .setContent("{\"issued_to\":\"::clientId::\",\"expires_in\":\"1785\",\"scope\":\"scope1 scope2\"}"))
                .build()

        oauthClient = GoogleOAuthClient("::client::", "::secret::", transport)

        val tokenInfo = oauthClient.getTokenInfo("::access_token::")

        assertThat(tokenInfo.isPresent, `is`(true))
        assertThat(tokenInfo.get().issuedTo, `is`("::clientId::"))
        assertThat(tokenInfo.get().expireIn, `is`(1785))
        assertThat(tokenInfo.get().scopes, `is`(setOf("scope1", "scope2")))
    }

    @Test
    fun tokenInfoRequestFailed() {
        val transport = MockHttpTransport.Builder()
                .setLowLevelHttpResponse(
                        MockLowLevelHttpResponse()
                                .setStatusCode(404)).build()

        oauthClient = GoogleOAuthClient("::client::", "::secret::", transport)

        val tokenInfo = oauthClient.getTokenInfo("::access_token::")

        assertThat(tokenInfo.isPresent, `is`(false))
    }
}