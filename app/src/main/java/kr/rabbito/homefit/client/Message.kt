package kr.rabbito.homefit.client

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kr.rabbito.homefit.utils.calc.Converter
import java.lang.reflect.Type

fun makeMessage(messageNumber: Int, data: Any): ByteArray? {
    val start = "[".toByteArray()
    val number = messageNumber.toByte()
    val end = "]".toByteArray()

    var message: ByteArray? = null

    when (messageNumber) {
        1 -> {
            // 사용자명을 String 으로 입력 받음
            if (data is String) {
                val size = (data.length + 4).toByte()
                val userName = data.toByteArray()

                message = start + number + size + userName + end
            }
        }
        2 -> {
            // 이미지를 Bitmap 으로 입력 받음
            if (data is ByteArray) {
                val msgSize = (11).toByte()
                val fileSize = Converter.convertToBigEndian(data.size)
                Log.d("connection", "file size: ${data.size}")
                val ext = "png".toByteArray()

                message = start + number + msgSize + ext + fileSize + end
            }
        }
        3 -> {
            if (data is String) {
                val size = (data.length + 4).toByte()
                val info = data.toByteArray()

                message = start + number + size + info + end
            }
        }
    }


    return message
}

fun checkMessage(data: ByteArray): Int {
    if (data[0] == "[".toByteArray()[0]) {
        Log.d("connection", "correct start")
    } else {
        Log.d("connection", "wrong start")
        return -1
    }

    val size = data[2].toInt() - 1
    try {
        if (data[size] == "]".toByteArray()[0]) {
            Log.d("connection", "correct end")
        } else {
            Log.d("connection", "wrong end")
            return -1
        }
    } catch (e: java.lang.IndexOutOfBoundsException) {
        Log.d("connection", "wrong length")
        return -1
    }

    if (data[1].toInt() == 32) {
        return data[1].toInt()
    } else if (data[1].toInt() == 33) {
        return data[1].toInt()
    } else {
        Log.d("connection", "wrong message number ${data[1].toInt()}")
        return -1
    }
}

fun parseMessage(data: ByteArray): Map<String, Any> {
    // 데이터 부분 크기 = 전체 크기 - 데이터 부분 제외 크기
    Log.d("connection", "${data[3]} ${data[4]}")
    return mapOf("result" to listOf<Any>(String(data, 5, data[2].toInt() - 6), data[3].toUInt() * 128u + data[4].toUInt()))   // 음식 이름, 양
}

fun parseJSONString(jsonString: String?): Map<String, Any> {
    val gson = Gson()
    val type: Type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(jsonString, type)
}