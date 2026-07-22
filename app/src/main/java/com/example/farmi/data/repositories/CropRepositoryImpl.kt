package com.example.farmi.data.repositories

import android.util.Log
import com.example.farmi.domain.entities.Crop
import com.example.farmi.domain.entities.CropStatus
import com.example.farmi.domain.interfaces.CropRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class CropRepositoryImpl(private val deviceUuid: String) : CropRepository {

    companion object {
        private const val TAG = "CropRepository"
        private const val BASE_URL = "http://10.0.2.2:8080/api"
    }

    override suspend fun getCrops(): List<Crop> = withContext(Dispatchers.IO) {
        val cropsUrl = "$BASE_URL/crops/$deviceUuid"
        var connection: HttpURLConnection? = null
        val cropsList = mutableListOf<Crop>()
        try {
            val url = URL(cropsUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")

            Log.d(TAG, "Fetching crops from $cropsUrl...")

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
                val cropsArray = jsonResponse.optJSONArray("crops")
                if (cropsArray != null) {
                    for (i in 0 until cropsArray.length()) {
                        val cropJson = cropsArray.getJSONObject(i)
                        
                        val idStr = cropJson.optString("id")
                        val id = try { UUID.fromString(idStr) } catch (e: Exception) { UUID.randomUUID() }
                        
                        val name = cropJson.optString("name", "Unknown Crop")
                        val variety = cropJson.optString("variety", "Standard")
                        
                        val plantedDateStr = cropJson.optString("planted_date")
                        val plantedDate = parseIsoDate(plantedDateStr)
                        
                        val estHarvestStr = cropJson.optString("estimated_harvest_date")
                        val estHarvestDate = parseIsoDate(estHarvestStr)
                        
                        val statusStr = cropJson.optString("status", "Planted")
                        val status = CropStatus.entries.firstOrNull { it.value.equals(statusStr, ignoreCase = true) } ?: CropStatus.PLANTED
                        
                        cropsList.add(
                            Crop(
                                id = id,
                                name = name,
                                variety = variety,
                                plantedDate = plantedDate,
                                estimatedHarvestDate = estHarvestDate,
                                status = status
                            )
                        )
                    }
                }
                Log.d(TAG, "Successfully loaded ${cropsList.size} crops from backend.")
            } else {
                Log.e(TAG, "Failed to load crops. Status: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching crops: ${e.message}", e)
        } finally {
            connection?.disconnect()
        }
        cropsList
    }

    override suspend fun addCrop(crop: Crop): Unit = withContext(Dispatchers.IO) {
        val addUrl = "$BASE_URL/crops"
        var connection: HttpURLConnection? = null
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            
            val payload = JSONObject().apply {
                put("deviceUuid", deviceUuid)
                put("name", crop.name)
                put("variety", crop.variety)
                put("acres", 1.0) // default value for backend schema
                put("plantedDate", sdf.format(crop.plantedDate))
                put("estimatedHarvestDate", sdf.format(crop.estimatedHarvestDate))
                put("status", crop.status.value)
            }

            val url = URL(addUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            Log.d(TAG, "Adding crop via $addUrl... Payload: $payload")

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                Log.d(TAG, "Crop successfully added to backend. Response code: $responseCode")
            } else {
                Log.e(TAG, "Add crop request failed with status: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error adding crop: ${e.message}", e)
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun updateCrop(crop: Crop) {
        // Mock method required by interface, not exposed/called by current UI
        Log.d(TAG, "updateCrop not supported by backend REST API.")
    }

    override suspend fun deleteCrop(id: UUID) {
        // Mock method required by interface, not exposed/called by current UI
        Log.d(TAG, "deleteCrop not supported by backend REST API.")
    }

    private fun parseIsoDate(dateStr: String): Date {
        if (dateStr.isEmpty()) return Date()
        return try {
            val odt = OffsetDateTime.parse(dateStr)
            Date.from(odt.toInstant())
        } catch (e: Exception) {
            try {
                val instant = Instant.parse(dateStr)
                Date.from(instant)
            } catch (e2: Exception) {
                try {
                    val ldt = java.time.LocalDateTime.parse(dateStr)
                    Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant())
                } catch (e3: Exception) {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        sdf.parse(dateStr) ?: Date()
                    } catch (e4: Exception) {
                        Date()
                    }
                }
            }
        }
    }
}
