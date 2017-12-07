package com.clouway.oauth2.client


import com.clouway.oauth2.client.core.Credentials
import com.clouway.oauth2.client.core.OAuthCredentialsFactory
import com.github.restdriver.clientdriver.ClientDriverRequest
import com.github.restdriver.clientdriver.ClientDriverResponse
import com.github.restdriver.clientdriver.ClientDriverRule
import com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse
import com.github.restdriver.clientdriver.RestClientDriver.onRequestTo
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.common.base.Optional
import org.hamcrest.Matchers.`is`
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class BearerTokenInterceptorTest {
    @JvmField
    val context = JUnitRuleMockery()

    val oauthCredentialsFactory = context.mock(OAuthCredentialsFactory::class.java)
    val clientDriver = ClientDriverRule()

    @Test
    fun setAuthorizationHeader() {
        val interceptor = BearerTokenInterceptor(oauthCredentialsFactory)

        context.checking(object : Expectations() {init {
            oneOf(oauthCredentialsFactory).create()
            will(returnValue(Optional.of(Credentials("::access_token::", "::refresh_token::", Date()))))
        }
        })

        clientDriver.addExpectation(
                onRequestTo("/v1/customers").withHeader("Authorization", "Bearer ::access_token::").withMethod(ClientDriverRequest.Method.GET),
                giveEmptyResponse()
        )

        val response = NetHttpTransport().createRequestFactory(interceptor).buildGetRequest(GenericUrl(clientDriver.baseUrl + "/v1/customers")).execute()

        assertThat(response.isSuccessStatusCode, `is`(true))
    }

    @Test
    fun credentialsAreNotPresent() {
        val interceptor = BearerTokenInterceptor(oauthCredentialsFactory)

        context.checking(object : Expectations() {init {
            oneOf(oauthCredentialsFactory).create()
            will(returnValue(Optional.absent<Credentials>()))
        }
        })

        clientDriver.addExpectation(
                onRequestTo("/v1/customers").withMethod(ClientDriverRequest.Method.GET),
                ClientDriverResponse()
        )

        val response = NetHttpTransport().createRequestFactory(interceptor).buildGetRequest(GenericUrl(clientDriver.baseUrl + "/v1/customers")).execute()

        assertThat(response.isSuccessStatusCode, `is`(true))
    }
}