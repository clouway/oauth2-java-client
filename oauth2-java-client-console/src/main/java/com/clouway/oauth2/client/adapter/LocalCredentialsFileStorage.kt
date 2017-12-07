package com.clouway.oauth2.client.adapter

import com.clouway.oauth2.client.core.Credentials
import com.clouway.oauth2.client.core.CredentialsStorage
import com.google.common.base.Optional
import com.google.common.io.Files
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * Store credentials in local file on your machine
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
class LocalCredentialsFileStorage(private val file: File) : CredentialsStorage {
    override fun getCredentials(): Optional<Credentials> {
        try {
            if (file.exists() && !file.isDirectory) {
                val json = JsonParser().parse(readFile(file)).asJsonObject
                val accessToken = json.get("access_token").asString
                val refreshToken = json.get("refresh_token").asString
                val expirationDate = json.get("expiration_date").asLong

                return Optional.of(Credentials(accessToken, refreshToken, Date(expirationDate)))
            }
        } catch (e: IllegalStateException) {
            return Optional.absent()
        }

        return Optional.absent()
    }

    override fun saveCredentials(credentials: Credentials) {
        val json = JsonObject()
        json.addProperty("access_token", credentials.accessToken)
        json.addProperty("refresh_token", credentials.refreshToken)
        json.addProperty("expiration_date", credentials.expirationDate.time)

        Files.write(json.toString().toByteArray(), file)
    }

    private fun readFile(file: File): String {
        val inputStream: InputStream = file.inputStream()

        return inputStream.bufferedReader().use { it.readText() }
    }
}