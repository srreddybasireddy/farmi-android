package com.example.farmi.data.repositories

import android.util.Log
import com.example.farmi.domain.interfaces.ChatRepository
import com.example.farmi.domain.interfaces.ChatAdvisoryResponse
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
    ): ChatAdvisoryResponse = withContext(Dispatchers.IO) {
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
                val qaId = jsonResponse.optString("qaId").takeIf { it.isNotEmpty() }
                Log.d(TAG, "Chat response received successfully.")
                ChatAdvisoryResponse(answer, qaId)
            } else {
                Log.e(TAG, "Chat request failed with status: $responseCode")
                ChatAdvisoryResponse("Error: Backend returned HTTP status $responseCode", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during chat request: ${e.message}", e)
            ChatAdvisoryResponse("Error: Network connection failed. Please check if backend is running.", null)
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun submitRating(deviceUuid: String, qaId: String, rating: Int): Unit = withContext(Dispatchers.IO) {
        val rateUrl = "$BASE_URL/chat/rate"
        var connection: HttpURLConnection? = null
        try {
            val payload = JSONObject().apply {
                put("deviceUuid", deviceUuid)
                put("qaId", qaId)
                put("rating", rating)
            }

            val url = URL(rateUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.doInput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            Log.d(TAG, "Sending rating payload to $rateUrl: $payload")

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                Log.d(TAG, "Rating submitted successfully.")
            } else {
                Log.e(TAG, "Rating submission failed with status: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during rating submission: ${e.message}", e)
        } finally {
            connection?.disconnect()
        }
    }
}
