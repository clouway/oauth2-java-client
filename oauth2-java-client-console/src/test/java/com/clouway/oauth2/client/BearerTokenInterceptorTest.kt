package com.clouway.oauth2.client


import com.clouway.oauth2.client.core.TokenSource
import com.github.restdriver.clientdriver.ClientDriverRequest
import com.github.restdriver.clientdriver.ClientDriverRule
import com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse
import com.github.restdriver.clientdriver.RestClientDriver.onRequestTo
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.hamcrest.Matchers.`is`
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class BearerTokenInterceptorTest {
    @JvmField
    val context = JUnitRuleMockery()

    val tokenSource = context.mock(TokenSource::class.java)
    val clientDriver = ClientDriverRule()

    @Test
    fun setAuthorizationHeader() {
        val interceptor = BearerTokenInterceptor(tokenSource)

        context.checking(object : Expectations() {init {
            oneOf(tokenSource).getToken()
            will(returnValue("::access_token::"))
        }
        })

        clientDriver.addExpectation(
                onRequestTo("/v1/customers").withHeader("Authorization", "Bearer ::access_token::").withMethod(ClientDriverRequest.Method.GET),
                giveEmptyResponse()
        )

        val response = NetHttpTransport().createRequestFactory(interceptor).buildGetRequest(GenericUrl(clientDriver.baseUrl + "/v1/customers")).execute()

        assertThat(response.isSuccessStatusCode, `is`(true))
    }
}