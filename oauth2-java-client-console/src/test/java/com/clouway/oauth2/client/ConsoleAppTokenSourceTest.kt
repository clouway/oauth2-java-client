package com.clouway.oauth2.client

import com.clouway.oauth2.client.core.*
import com.google.common.base.Optional
import org.hamcrest.Matchers.`is`
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream


/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class ConsoleAppTokenSourceTest {

    @JvmField
    val context = JUnitRuleMockery()

    val credentialsStore = context.mock(CredentialsStore::class.java)
    val oauthClient = context.mock(OAuthClient::class.java)

    val config = OAuthClientConfig
            .setClient("::client::")
            .setSecret("::secret::")
            .setScopes(mutableSetOf("scope1", "scope2"))
            .setCredentialsStore(credentialsStore)
            .build()

    private var tokenSource = ConsoleAppTokenSource(config, oauthClient)

    @Test
    fun getTokenWhenUserHaveCredentialsInStore() {
        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.of(Credentials("access_token", "refresh_token"))))

            oneOf(oauthClient).getTokenInfo("access_token")
            will(returnValue(Optional.of(TokenInfo("issued_to", setOf("scope1", "scope2"), 646))))
        }
        })

        val token = tokenSource.getToken()

        assertThat(token, `is`("access_token"))
    }

    @Test
    fun refreshTheTokenFromCredentialsStore() {
        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.of(Credentials("access_token", "refresh_token"))))

            oneOf(oauthClient).getTokenInfo("access_token")
            will(returnValue(Optional.absent<TokenInfo>()))

            oneOf(oauthClient).refreshToken("refresh_token")
            will(returnValue("new_access_token"))

            oneOf(credentialsStore).saveCredentials(Credentials("new_access_token", "refresh_token"))
        }
        })

        val token = tokenSource.getToken()

        assertThat(token, `is`("new_access_token"))
    }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun cannotRefreshTheToken() {
        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.of(Credentials("access_token", "refresh_token"))))

            oneOf(oauthClient).getTokenInfo("access_token")
            will(returnValue(Optional.absent<TokenInfo>()))

            oneOf(oauthClient).refreshToken("refresh_token")
            will(throwException(RefreshTokenIsInvalidException("invalid refresh")))
        }
        })

        tokenSource.getToken()
    }

    @Test
    fun triggerUserAuthorization() {
        val server = JettyServer(8089)
        val inputStream = ByteArrayInputStream("::authCode::".toByteArray())
        System.setIn(inputStream)

        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.absent<Credentials>()))

            oneOf(oauthClient).authorizeUser(server.callbackUri, mutableSetOf("scope1", "scope2"))

            oneOf(oauthClient).newTokenRequest("::authCode::", server.callbackUri)
            will(returnValue(TokenResponse("::access_token::", "::refresh_token::")))

            oneOf(credentialsStore).saveCredentials(Credentials("::access_token::", "::refresh_token::"))
        }
        })

        val token = tokenSource.getToken()

        System.setIn(System.`in`)

        assertThat(token, `is`("::access_token::"))
    }

    @Test(expected = AuthorizationCodeIsInvalidException::class)
    fun onAuthorizationAuthCodeIsInvalid() {
        val server = JettyServer(8089)
        val inputStream = ByteArrayInputStream("::authCode::".toByteArray())
        System.setIn(inputStream)

        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.absent<Credentials>()))

            oneOf(oauthClient).authorizeUser(server.callbackUri, mutableSetOf("scope1", "scope2"))

            oneOf(oauthClient).newTokenRequest("::authCode::", server.callbackUri)
            will(throwException(AuthorizationCodeIsInvalidException("failed")))
        }
        })

        tokenSource.getToken()

        System.setIn(System.`in`)
    }
}