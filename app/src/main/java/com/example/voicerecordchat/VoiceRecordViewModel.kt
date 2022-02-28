package com.example.voicerecordchat

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.media.AudioAttributes




class VoiceRecordViewModel : ViewModel() {

    /* const */
    var isStartRecord: Boolean = false
    /* Timer */
    private val recordTimer = CountdownTimer()
    private var internalRecordTime = 0L

    /* Voice Recorder*/
    private var audioDir: String = ""
    private val LOG_TAG = "Voice Recorder"
//    private var fileName: String = ""
    private var fileName: String = ""
    /* Voice Recorder*/
    private var recorder: MediaRecorder? = null
    /* Live Data */

    private val _onBackPressEvent = MutableLiveData<Boolean>()
    val onBackPressEvent: LiveData<Boolean> = _onBackPressEvent

    private val _recordingTime = MutableLiveData<Long>()
    val recordingTime: LiveData<Long> = _recordingTime

    private val _voiceRecordedEvent= MutableLiveData<VoiceChatModel>()
    val voiceRecordedEvent: LiveData<VoiceChatModel> = _voiceRecordedEvent

    private val _errorToastEvent = MutableLiveData<String>()
    val errorToastEvent: LiveData<String> = _errorToastEvent

    init {

        viewModelScope.launch {
            recordTimer.countdownChannel.consumeEach {
                Log.e("recordTimer", "countdown in service : " + it)
                internalRecordTime = TimeUnit.DAYS.toMillis(1) - it
                updateRecordTime(internalRecordTime)
            }
        }

    }

    fun triggerOnBackPress() {
        _onBackPressEvent.value = isStartRecord
    }

    /* Timer */

    private fun startTimer() {
        internalRecordTime = 0L
        recordTimer.start(
            countDownInterval = 1000,
            scope = viewModelScope,
            coroutineContext = Dispatchers.IO,
            millisInFuture = TimeUnit.DAYS.toMillis(1)
        )
    }


    private fun stopTimer() {
        recordTimer.stop()
        internalRecordTime = 0L
        _recordingTime.value = 0L
    }

    private fun updateRecordTime(time: Long) {
        _recordingTime.value = time
    }

    /* voice recorder*/

    fun startRecording() {
        startTimer()
        startVoiceRecorder()
    }

    fun stopRecording() {
        if (TimeUnit.MILLISECONDS.toSeconds(internalRecordTime) <= 1) {
            //todo do not updload voice audio
            _errorToastEvent.value = "ไม่สามารถส่งได้ เสียงสั่นเกินไป"
        }
        stopVoiceRecorder()
        stopTimer()
    }

    fun setDirectory(dir: String) {
        audioDir = dir
    }

    fun clearRecorderAndPlayer() {
        recorder?.release()
        recorder = null
    }

    private fun startVoiceRecorder() {
        recorder = MediaRecorder().apply {

            fileName = audioDir + "/AudioRecording.3gp"

            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed : {${e.message}}")
            }

            this.start()
        }
    }

    private fun stopVoiceRecorder() {
        recorder?.apply {
            this.stop()
            release()
            _voiceRecordedEvent.value = VoiceChatModel(1,fileName,40000)
        }
        recorder = null
    }

}