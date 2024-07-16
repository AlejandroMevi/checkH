package com.venturessoft.human.core

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import com.venturessoft.human.R
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.isActiveSpeach
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.isSpeach
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.textSpeach
class SpeachRecognition(private val activity: Activity) : RecognitionListener {

    private var speech: SpeechRecognizer? = null
    private var recognizerIntent = Intent()
    fun initSpeechRecognizer() {
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-US")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    fun startSpeech() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isActiveSpeach){
                textSpeach.value = null
                speech = SpeechRecognizer.createSpeechRecognizer(activity)
                speech?.setRecognitionListener(this)
                speech?.startListening(recognizerIntent)
            } } , 5000)
    }
    fun stopSpeech() {
        speech?.stopListening()
        speech?.destroy()
        speech = null
        isActiveSpeach = false
    }
    override fun onReadyForSpeech(p0: Bundle?) {
        Toast.makeText(activity, activity.getString(R.string.voice_message5), Toast.LENGTH_SHORT).show()
    }
    override fun onEndOfSpeech() {}
    override fun onError(p0: Int) {
        if (textSpeach.value.isNullOrEmpty()) {
            startSpeech()
        }
    }
    override fun onResults(p0: Bundle?) {
        val preResult = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (preResult != null) {
            Toast.makeText(activity, preResult[0], Toast.LENGTH_SHORT).show()
            val result = preResult[0]?.replace(" ", "")
            validateSpeechInColabMode(result)
        }
    }
    private fun validateSpeechInColabMode(result: String?) {
        if (result != null) {
            val resultText: String = result.lowercase()
            if (resultText.contains("entrada") || resultText.contains("salida") || resultText.isNotEmpty()) {
                if (resultText.contains("entrada") || resultText.contains("salida")) {
                    if (resultText.contains("entrada")) {
                        textSpeach.value = activity.getString(R.string.checkin_text)
                    }
                    if (resultText.contains("salida")) {
                        textSpeach.value = activity.getString(R.string.checkout_text)
                    }
                } else {
                    textSpeach.value = resultText
                }
            } else {
                textSpeach.value = null
                if (isActiveSpeach){
                    startSpeech()
                }else{
                    stopSpeech()
                }
            }
        } else {
            textSpeach.value = null
            if (isActiveSpeach){
                startSpeech()
            }else{
                stopSpeech()
            }
        }
    }
    override fun onPartialResults(p0: Bundle?) {}
    override fun onEvent(p0: Int, p1: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(p0: Float) {}
    override fun onBufferReceived(p0: ByteArray?) {}
}