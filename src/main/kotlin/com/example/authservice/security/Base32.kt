package com.example.authservice.security

import java.util.Locale

object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun encode(bytes: ByteArray): String {
        var i = 0
        val out = StringBuilder()
        var buffer = 0
        var bitsLeft = 0

        while (i < bytes.size) {
            buffer = buffer shl 8 or (bytes[i].toInt() and 0xff)
            bitsLeft += 8
            i++
            while (bitsLeft >= 5) {
                val index = (buffer shr (bitsLeft - 5)) and 0x1f
                bitsLeft -= 5
                out.append(ALPHABET[index])
            }
        }
        if (bitsLeft > 0) {
            val index = (buffer shl (5 - bitsLeft)) and 0x1f
            out.append(ALPHABET[index])
        }
        return out.toString()
    }

    fun decode(str: String): ByteArray {
        val clean = str.trim().replace("=", "").uppercase(Locale.US)
        var buffer = 0
        var bitsLeft = 0
        val out = ArrayList<Byte>()

        for (c in clean) {
            val index = ALPHABET.indexOf(c)
            if (index < 0) continue
            buffer = (buffer shl 5) or index
            bitsLeft += 5
            if (bitsLeft >= 8) {
                val b = (buffer shr (bitsLeft - 8)) and 0xff
                bitsLeft -= 8
                out.add(b.toByte())
            }
        }
        return out.toByteArray()
    }
}
