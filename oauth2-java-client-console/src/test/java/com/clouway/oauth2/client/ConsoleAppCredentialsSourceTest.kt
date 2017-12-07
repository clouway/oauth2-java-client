package com.clouway.oauth2.client

import com.clouway.oauth2.client.core.*
import com.google.common.base.Optional
import org.hamcrest.Matchers.`is`
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.*


/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class ConsoleAppCredentialsSourceTest {

    @JvmField
    val context = JUnitRuleMockery()

    val credentialsStore = context.mock(CredentialsStorage::class.java)
    val oauthClient = context.mock(OAuthHttpClient::class.java)

    val config = OAuthClientConfig
            .setClient("::client::")
            .setSecret("::secret::")
            .setScopes(mutableSetOf("scope1", "scope2"))
            .setCredentialsStore(credentialsStore)
            .build()

    private var credentialsSource = ConsoleAppCredentialsSource(config, oauthClient)

    @Test
    fun getTokenWhenUserHaveCredentialsInStore() {
        val instantTime = today(5, 20, 30)
        val tokenExpirationDate = today(5, 20, 40)
        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.of(Credentials("access_token", "refresh_token", tokenExpirationDate))))
        }
        })

        val credentials = credentialsSource.getCredentials(instantTime)

        assertThat(credentials, `is`(Credentials("access_token", "refresh_token", tokenExpirationDate)))
    }

    @Test
    fun refreshTheTokenFromCredentialsStore() {
        val instantTime = today(5, 21, 30)
        val tokenExpirationDate = today(5, 20, 30)
        val newExpirationDate = today(6, 20, 30)

        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.of(Credentials("access_token", "refresh_token", tokenExpirationDate))))

            oneOf(oauthClient).refreshToken("refresh_token")
            will(returnValue(TokenResponse("new_access_token", "refresh_token", newExpirationDate)))

            oneOf(credentialsStore).saveCredentials(Credentials("new_access_token", "refresh_token", newExpirationDate))
        }
        })

        val credentials = credentialsSource.getCredentials(instantTime)

        assertThat(credentials.accessToken, `is`("new_access_token"))
    }

    @Test(expected = RefreshTokenIsInvalidException::class)
    fun cannotRefreshTheToken() {
        val instantTime = today(5, 20, 30)
        val tokenExpirationDate = today(5, 20, 20)

        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.of(Credentials("access_token", "refresh_token", tokenExpirationDate))))

            oneOf(oauthClient).refreshToken("refresh_token")
            will(throwException(RefreshTokenIsInvalidException("invalid refresh")))
        }
        })

        credentialsSource.getCredentials(instantTime)
    }

    @Test
    fun triggerUserAuthorization() {
        val instantTime = Date()

        val server = JettyServer(8089)
        val inputStream = ByteArrayInputStream("::authCode::".toByteArray())
        System.setIn(inputStream)

        context.checking(object : Expectations() { init {
            oneOf(credentialsStore).getCredentials()
            will(returnValue(Optional.absent<Credentials>()))

            oneOf(oauthClient).authorizeUser(server.callbackUri, mutableSetOf("scope1", "scope2"))

            oneOf(oauthClient).newTokenRequest("::authCode::", server.callbackUri)
            will(returnValue(TokenResponse("::access_token::", "::refresh_token::", instantTime)))

            oneOf(credentialsStore).saveCredentials(Credentials("::access_token::", "::refresh_token::", instantTime))
        }
        })

        val credentials = credentialsSource.getCredentials(Date())

        System.setIn(System.`in`)

        assertThat(credentials, `is`(Credentials("::access_token::", "::refresh_token::", instantTime)))
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

        credentialsSource.getCredentials(Date())

        System.setIn(System.`in`)
    }


    private fun today(hour: Int, minutes: Int, seconds: Int): Date {
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.set(currentDate.year, currentDate.month, currentDate.day, hour, minutes, seconds)
        return calendar.time
    }
}