package com.example.authservice.security

import org.springframework.stereotype.Service
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TotpService {

    private val random = SecureRandom()

    fun generateSecret(): String {
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        return Base32.encode(bytes)
    }

    fun verifyCode(secret: String, code: String, window: Int = 1): Boolean {
        val cleanCode = code.trim()
        val secretBytes = Base32.decode(secret)
        val timeStep = System.currentTimeMillis() / 1000L / 30L
        for (i in -window..window) {
            val expected = generateCode(secretBytes, timeStep + i)
            if (expected == cleanCode) return true
        }
        return false
    }

    fun otpauthUri(account: String, issuer: String, secret: String): String {
        val encodedAccount = account.replace(" ", "%20")
        val encodedIssuer = issuer.replace(" ", "%20")
        return "otpauth://totp/$encodedIssuer:$encodedAccount?secret=$secret&issuer=$encodedIssuer&digits=6&period=30"
    }

    private fun generateCode(secret: ByteArray, timeStep: Long): String {
        val data = ByteArray(8)
        var value = timeStep
        for (i in 7 downTo 0) {
            data[i] = (value and 0xff).toByte()
            value = value shr 8
        }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(secret, "HmacSHA1"))
        val hash = mac.doFinal(data)
        val offset = hash[hash.size - 1].toInt() and 0x0f
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            (hash[offset + 3].toInt() and 0xff)
        val otp = binary % 1_000_000
        return otp.toString().padStart(6, '0')
    }
}
