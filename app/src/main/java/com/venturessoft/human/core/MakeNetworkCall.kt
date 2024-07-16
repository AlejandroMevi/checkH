package com.venturessoft.human.core

import com.venturessoft.human.core.utils.Constants.Companion.ERRORHTTP
import com.venturessoft.human.core.utils.Constants.Companion.ET000
import com.venturessoft.human.core.utils.Constants.Companion.FACE_FAIL
import com.venturessoft.human.core.utils.Constants.Companion.FACE_NO_DETECTED
import com.venturessoft.human.core.utils.Constants.Companion.SMALL_FACE
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> makeNetworkCall(call: suspend () -> T): ApiResponceStatus<T> =
    withContext(Dispatchers.IO) {
        try {
            ApiResponceStatus.Success(call())
        } catch (exception: Exception) {
            var messageException = exception.message.toString()
            messageException.lowercase().apply {
                if (this.contains("timedout") ||
                    this.contains("error_timeout")||
                    this.contains("timed out") ||
                    this.contains("timed_out") ||
                    this.contains("timeout") ||
                    this.contains("time out") ||
                    this.contains("time_out") ||
                    this.contains("connection reset")||
                    this.contains("connection aborted") ||
                    this.contains("connection abort") ||
                    this.contains("failed to connect to") ||
                    this.contains("waiting for")
                ) {
                    messageException = TIMEOUT
                }
                if (this.contains("unable to resolve host") ||
                    this.contains("validation failed") ||
                    this.contains("http 400") ||
                    this.contains("http 401") ||
                    this.contains("http 402") ||
                    this.contains("http 403") ||
                    this.contains("http 404") ||
                    this.contains("http 405") ||
                    this.contains("http 406") ||
                    this.contains("http 407") ||
                    this.contains("http 408") ||
                    this.contains("http 409") ||
                    this.contains("http 410") ||
                    this.contains("http 411") ||
                    this.contains("http 412") ||
                    this.contains("http 413") ||
                    this.contains("http 414") ||
                    this.contains("http 415") ||
                    this.contains("http 416") ||
                    this.contains("http 417") ||
                    this.contains("http 418") ||
                    this.contains("http 421") ||
                    this.contains("http 422") ||
                    this.contains("http 423") ||
                    this.contains("http 424") ||
                    this.contains("http 425") ||
                    this.contains("http 426") ||
                    this.contains("http 428") ||
                    this.contains("http 429") ||
                    this.contains("http 431") ||
                    this.contains("http 451") ||
                    this.contains("http 500") ||
                    this.contains("http 501") ||
                    this.contains("http 502") ||
                    this.contains("http 503") ||
                    this.contains("http 504") ||
                    this.contains("http 505") ||
                    this.contains("http 506") ||
                    this.contains("http 507") ||
                    this.contains("http 508") ||
                    this.contains("http 510") ||
                    this.contains("http 511") ||
                    this.contains("http 600") ||
                    this.contains("http 601") ||
                    this.contains("http 605") ||
                    this.contains("http 620") ||
                    this.contains("http 630") ||
                    this.contains("http 631") ||
                    this.contains("http 632") ||
                    this.contains("http 633") ||
                    this.contains("http 634") ||
                    this.contains("http 635") ||
                    this.contains("http 636") ||
                    this.contains("http 637") ||
                    this.contains("http 638") ||
                    this.contains("http 639") ||
                    this.contains("http 640") ||
                    this.contains("http 641") ||
                    this.contains("http 660") ||
                    this.contains("http 661") ||
                    this.contains("http 662") ||
                    this.contains("http 663") ||
                    this.contains("http 699") ||
                    this.contains("db003") ||
                    this.contains("db210") ||
                    this.contains("db211") ||
                    this.contains("db212") ||
                    this.contains("db213") ||
                    this.contains("db214") ||
                    this.contains("db215") ||
                    this.contains("db216") ||
                    this.contains("db217") ||
                    this.contains("db218") ||
                    this.contains("db219") ||
                    this.contains("db220") ||
                    this.contains("db221") ||
                    this.contains("db222") ||
                    this.contains("db223") ||
                    this.contains("db224") ||
                    this.contains("db225") ||
                    this.contains("db226") ||
                    this.contains("db227") ||
                    this.contains("db229") ||
                    this.contains("db230") ||
                    this.contains("db231") ||
                    this.contains("db232") ||
                    this.contains("db233") ||
                    this.contains("db250") ||
                    this.contains("db400") ||
                    this.contains("db401") ||
                    this.contains("db402") ||
                    this.contains("db403") ||
                    this.contains("db404") ||
                    this.contains("db405") ||
                    this.contains("db407") ||
                    this.contains("db408") ||
                    this.contains("db409") ||
                    this.contains("db410") ||
                    this.contains("db411") ||
                    this.contains("db412") ||
                    this.contains("db413") ||
                    this.contains("db414") ||
                    this.contains("db415") ||
                    this.contains("db416") ||
                    this.contains("db417") ||
                    this.contains("db418") ||
                    this.contains("db419") ||
                    this.contains("db420") ||
                    this.contains("db421") ||
                    this.contains("error_http")
                ) {
                    messageException = ERRORHTTP
                }
            }
            ApiResponceStatus.Error(messageException)
        }
    }

fun evaluateResponce(codigo: String, errorMessage: String? = null) {
    if (codigo != ET000 && codigo.uppercase() != "OK") {
        if (codigo.isNotEmpty()) {
            throw Exception(codigo)
        } else {
            if (!errorMessage.isNullOrEmpty()) {
                throw Exception(errorMessage)
            } else {
                throw Exception("")
            }
        }
    }
}

fun evaluateFace(face: String) {
    when (face.lowercase()) {
        "genuine" -> {}
        "small face" -> throw Exception(SMALL_FACE)
        "face not detected", "face is covered" -> throw Exception(FACE_NO_DETECTED)
        else -> throw Exception(FACE_FAIL)
    }
}