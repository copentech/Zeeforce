package com.example.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.Base64

object CompressionUtils {
    fun compress(data: String): String {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(data.toByteArray()) }
        return Base64.getEncoder().encodeToString(bos.toByteArray())
    }
    fun decompress(data: String): String {
        val bytes = Base64.getDecoder().decode(data)
        val bis = ByteArrayInputStream(bytes)
        return GZIPInputStream(bis).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
