package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.remote.supabase.SupabaseClient
import com.example.data.remote.supabase.model.SupabaseHealthCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SyncResult(
    val success: Boolean,
    val itemsSynced: Int,
    val bytesTransferred: Long,
    val durationMs: Long,
    val message: String
)

class SupabaseSyncService(private val context: Context) {
    val supabaseClient: SupabaseClient = SupabaseClient.getInstance(context)

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun syncPayload(
        supabaseUrl: String,
        supabaseAnonKey: String,
        table: String,
        payloadJson: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        // Ensure supabase client is synced with current company profile config
        supabaseClient.updateConfig(supabaseUrl, supabaseAnonKey)

        // Decompress if the payload is compressed with GZIP: prefix
        val finalPayload = if (payloadJson.startsWith("GZIP:")) {
            try {
                com.example.util.CompressionUtils.decompress(payloadJson.substring(5))
            } catch (e: Exception) {
                Log.e("SupabaseSync", "Decompression failed for table $table, falling back to raw payload")
                payloadJson
            }
        } else {
            payloadJson
        }

        val formatArrayPayload = if (!finalPayload.trim().startsWith("[")) {
            "[$finalPayload]"
        } else {
            finalPayload
        }

        val result = supabaseClient.db.upsertBatch(table, formatArrayPayload)
        if (result.isSuccess) {
            result
        } else {
            val err = result.exceptionOrNull()
            Log.e("SupabaseSync", "Remote sync to $table failed: ${err?.message}")
            Result.failure(err ?: Exception("Unknown Supabase sync error"))
        }
    }

    suspend fun testConnection(): SupabaseHealthCheck {
        return supabaseClient.checkHealth()
    }
}
