package com.example.voicerecordchat

import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log
import android.util.Patterns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.io.IOException


class AudioSteamHelper(private val scope: CoroutineScope, private val listener : AudioSteamHelperListener) {

    private val LOG_TAG = "AudioSteamHelper"

    /* Voice Player*/
    private var player: MediaPlayer? = null

    private val steamDurationTimer = CountdownTimer()
    private val timerCountDownInterval = 100L

    private var audioData : VoiceChatModel? = null

    init {
        createTimer()
    }

    private fun createTimer() {
        scope.launch {
            steamDurationTimer.countdownChannel.consumeEach { currentDuation ->
                Log.e("steamDurationTimer", "countdown in service : " + currentDuation)
                audioData?.let { audio ->
                    player?.let {
                        Log.e(LOG_TAG, "$audio : " + it.currentPosition)
                        listener.onPlaying(audio,it.currentPosition)
                    }
                }
            }
        }
    }

    private fun startTimer(duration: Long) {
        steamDurationTimer.start(
            countDownInterval = timerCountDownInterval,
            scope = scope,
            coroutineContext = Dispatchers.Main,
            millisInFuture = duration
        )
    }

    private fun stopTimer() {
        steamDurationTimer.stop()
    }

    private fun getAudioStreamDuaration(sourceUrl: String): Long {
        return MediaMetadataRetriever().apply {
            setDataSource(sourceUrl, HashMap<String, String>())
        }.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
    }


    interface AudioSteamHelperListener{
        fun onPlaying(voiceChatModel: VoiceChatModel,duration:Int)
    }


    /* voice  player*/

    fun isAudioPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    fun isPlaySameAudio(audioData:VoiceChatModel): Boolean {
        this.audioData?.let {
            return audioData.id == it.id
        }
        return false
    }

    fun startAudioPlayer(audioData:VoiceChatModel) {
        this.audioData = audioData
        stopAudioPlayer()
        player = MediaPlayer().apply {
            try {
                if (Patterns.WEB_URL.matcher(audioData.url).matches()){
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                }
                setDataSource(audioData.url)
                setOnCompletionListener {
                    stopAudioPlayer()
                }
                prepare()
                startTimer(
                    getAudioStreamDuaration(audioData.url)
                )
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    fun stopAudioPlayer() {
        player?.release()
        player = null
        stopTimer()
    }

}