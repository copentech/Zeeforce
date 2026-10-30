package com.example.data.remote.supabase

import android.util.Log
import com.example.data.remote.supabase.model.SupabaseConfig
import com.example.data.remote.supabase.model.SupabaseHealthCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

/**
 * Client handling remote data synchronization and table queries with Supabase PostgREST.
 * Implements low-bandwidth compression, merge-duplicates resolution, and connectivity telemetry.
 */
class SupabaseDatabaseClient(
    private val httpClient: OkHttpClient,
    private val configProvider: () -> SupabaseConfig
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Upserts an array of records (JSON format) into the specified Supabase table.
     * Uses `resolution=merge-duplicates` to ensure idempotence during offline outbox retries.
     */
    suspend fun upsertBatch(
        table: String,
        jsonArrayPayload: String,
        authToken: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        val config = configProvider()
        val originalBytes = jsonArrayPayload.toByteArray(Charsets.UTF_8)
        val originalSize = originalBytes.size.toLong()

        if (config.isSimulationMode || !config.isConfigured) {
            // High-fidelity low-bandwidth simulation mode (~200ms latency on 3G)
            delay(210)
            return@withContext Result.success(originalSize)
        }

        try {
            val endpoint = "${config.restEndpoint}/$table"
            val bearer = authToken ?: config.anonKey

            // Compress payload using GZIP to minimize bandwidth
            val compressedBytes = ByteArrayOutputStream().use { bos ->
                GZIPOutputStream(bos).use { gzos ->
                    gzos.write(originalBytes)
                }
                bos.toByteArray()
            }

            val request = Request.Builder()
                .url(endpoint)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer $bearer")
                .header("Content-Type", "application/json")
                .header("Content-Encoding", "gzip")
                .header("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(compressedBytes.toRequestBody(jsonMediaType))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(compressedBytes.size.toLong())
            } else {
                val err = response.body?.string() ?: "HTTP ${response.code}"
                Log.w("SupabaseDB", "Supabase upsert failure for table $table ($err)")
                Result.failure(Exception("Supabase sync failed (HTTP ${response.code}): $err"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseDB", "Network exception syncing $table", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches remote records from a Supabase table.
     */
    suspend fun fetchRecords(
        table: String,
        limit: Int = 100,
        select: String = "*",
        authToken: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = configProvider()

        if (config.isSimulationMode || !config.isConfigured) {
            delay(150)
            return@withContext Result.success("[]")
        }

        try {
            val endpoint = "${config.restEndpoint}/$table?select=$select&limit=$limit"
            val bearer = authToken ?: config.anonKey

            val request = Request.Builder()
                .url(endpoint)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer $bearer")
                .header("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "[]"
            if (response.isSuccessful) {
                Result.success(responseBody)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tests connectivity to the Supabase endpoint and measures round-trip latency.
     */
    suspend fun testConnection(): SupabaseHealthCheck = withContext(Dispatchers.IO) {
        val config = configProvider()
        val startTime = System.currentTimeMillis()

        if (config.isSimulationMode || !config.isConfigured) {
            delay(120)
            val elapsed = System.currentTimeMillis() - startTime
            return@withContext SupabaseHealthCheck(
                isConnected = true,
                isSimulation = true,
                latencyMs = elapsed,
                message = "Offline Simulation Mode (Ready for local persistence & instant outbox queueing)",
                endpointUrl = config.cleanUrl
            )
        }

        try {
            // Ping the REST root endpoint with the anon key
            val pingUrl = "${config.cleanUrl}/rest/v1/"
            val request = Request.Builder()
                .url(pingUrl)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val elapsed = System.currentTimeMillis() - startTime

            if (response.isSuccessful || response.code == 404 || response.code == 200 || response.code == 401) {
                // Any response from the server proves reachability
                val msg = if (response.isSuccessful) {
                    "Live Supabase Gateway Connected (${elapsed}ms latency)"
                } else {
                    "Supabase Gateway Reachable with HTTP ${response.code} (${elapsed}ms latency)"
                }
                SupabaseHealthCheck(
                    isConnected = true,
                    isSimulation = false,
                    latencyMs = elapsed,
                    message = msg,
                    endpointUrl = config.cleanUrl
                )
            } else {
                SupabaseHealthCheck(
                    isConnected = false,
                    isSimulation = false,
                    latencyMs = elapsed,
                    message = "Server returned HTTP ${response.code}",
                    endpointUrl = config.cleanUrl
                )
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            SupabaseHealthCheck(
                isConnected = false,
                isSimulation = false,
                latencyMs = elapsed,
                message = "Connection failed: ${e.localizedMessage ?: "Timeout / Host Unreachable"}",
                endpointUrl = config.cleanUrl
            )
        }
    }
}
