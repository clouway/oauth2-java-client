package com.clouway.oauth2.client.adapter.http.google

import com.clouway.oauth2.client.adapter.LocalCredentialsFileStorage
import com.clouway.oauth2.client.core.Credentials
import com.google.gson.JsonParser
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.*

/**
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class LocalCredentialsFileStorageTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @After
    fun tearDown() {
        tempFolder.delete()
    }

    @Test
    fun saveCredentialsSuccessful() {
        val instantTime = Date()
        val testFile = tempFolder.newFile("test")
        val credentialsStorage = LocalCredentialsFileStorage(testFile)

        credentialsStorage.saveCredentials(Credentials("::access_token::", "::refresh_token::", instantTime))

        val fileContent = testFile.inputStream().bufferedReader().use { it.readText() }
        val json = JsonParser().parse(fileContent).asJsonObject

        assertThat(json.get("access_token").asString, `is`("::access_token::"))
        assertThat(json.get("refresh_token").asString, `is`("::refresh_token::"))
        assertThat(json.get("expiration_date").asLong, `is`(instantTime.time))
    }

    @Test
    fun getSavedCredentials() {
        val instantTime = Date()
        val credentialsStorage = LocalCredentialsFileStorage(tempFolder.newFile("test"))

        credentialsStorage.saveCredentials(Credentials("::access_token::", "::refresh_token::", instantTime))

        val credentials = credentialsStorage.getCredentials()

        assertThat(credentials.isPresent, `is`(true))
        assertThat(credentials.get().accessToken, `is`("::access_token::"))
        assertThat(credentials.get().refreshToken, `is`("::refresh_token::"))
    }

    @Test
    fun credentialsAreNotFound() {
        val credentialsStorage = LocalCredentialsFileStorage(tempFolder.newFile("test"))
        val credentials = credentialsStorage.getCredentials()

        assertThat(credentials.isPresent, `is`(false))
    }
}