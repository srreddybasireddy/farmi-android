package com.example.farmi.data.repositories

import android.util.Log
import com.example.farmi.domain.interfaces.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChatRepositoryImpl : ChatRepository {

    companion object {
        private const val TAG = "ChatRepository"
        private const val BASE_URL = "http://10.0.2.2:8080/api"
    }

    override suspend fun getAdvisoryResponse(
        deviceUuid: String,
        category: String,
        query: String
    ): String = withContext(Dispatchers.IO) {
        val chatUrl = "$BASE_URL/chat"
        var connection: HttpURLConnection? = null
        try {
            val payload = JSONObject().apply {
                put("deviceUuid", deviceUuid)
                put("category", category)
                put("query", query)
                put("summarize", false)
            }

            val url = URL(chatUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.doInput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            Log.d(TAG, "Sending chat query to $chatUrl... Payload: $payload")

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val answer = jsonResponse.optString("answer", "No answer received.")
                Log.d(TAG, "Chat response received successfully.")
                answer
            } else {
                Log.e(TAG, "Chat request failed with status: $responseCode")
                "Error: Backend returned HTTP status $responseCode"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during chat request: ${e.message}", e)
            "Error: Network connection failed. Please check if backend is running."
        } finally {
            connection?.disconnect()
        }
    }
}
