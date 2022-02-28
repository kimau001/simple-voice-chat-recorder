package com.example.voicerecordchat

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.coroutines.CoroutineContext

class CountdownTimer {
    private lateinit var scope: CoroutineScope
    private lateinit var coroutineContext: CoroutineContext
    private val _countdownChannel: Channel<Long> = Channel()
    private var _currentMillisInFuture: Long = 0
    private var countDownInterval: Long = 1000
    private var countdownJob: Job? = null
    private var timeOfCountdownPaused: Long = 0

    val currentMillisInFuture : Long get() = _currentMillisInFuture
    val countdownChannel: ReceiveChannel<Long> get() = _countdownChannel

    private fun createCountdownJob(scope: CoroutineScope, coroutineContext: CoroutineContext): Job {
        return scope.launch(coroutineContext) {
            while (_currentMillisInFuture > 0) {
                _countdownChannel.send(_currentMillisInFuture)
                val nextDelay = if (_currentMillisInFuture >= countDownInterval) countDownInterval else _currentMillisInFuture
                delay(nextDelay)
                _currentMillisInFuture -= nextDelay
            }
            _countdownChannel.send(0)
        }
    }

    fun start(
            millisInFuture: Long,
            countDownInterval: Long = 1000,
            scope: CoroutineScope,
            coroutineContext: CoroutineContext = Dispatchers.Default
    ) {
        this._currentMillisInFuture = millisInFuture
        this.countDownInterval = countDownInterval
        this.scope = scope
        this.coroutineContext = coroutineContext
        timeOfCountdownPaused = 0
        cancelCountdownJob()
        countdownJob = createCountdownJob(scope, coroutineContext)
    }

    fun resume() {
        if (timeOfCountdownPaused > 0 && _currentMillisInFuture > 0 && ::scope.isInitialized && ::coroutineContext.isInitialized) {
            countdownJob = createCountdownJob(scope, coroutineContext)
        }
    }

    fun pause() {
        timeOfCountdownPaused = System.currentTimeMillis()
        cancelCountdownJob()
    }

    fun stop() {
        cancelCountdownJob()
    }

    private fun cancelCountdownJob() {
        countdownJob?.cancel()
        countdownJob = null
    }
}