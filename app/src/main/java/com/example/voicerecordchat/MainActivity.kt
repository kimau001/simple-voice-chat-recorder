package com.example.voicerecordchat

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.util.TimeUtils
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.example.voicerecordchat.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : DataBindingActivity<ActivityMainBinding>(), CoroutineScope,
    AudioSteamHelper.AudioSteamHelperListener {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val mAdapter = VoiceChatAdapter(mutableListOf())

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val viewModel: VoiceRecordViewModel = VoiceRecordViewModel()

    private val audioSteamHelper by lazy { AudioSteamHelper(this, this) }

    override fun layoutId(): Int = R.layout.activity_main

    override fun startPage() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        initView()
        initViewModel()
    }

    private fun initViewModel() {

        viewModel.apply {

            getExternalFilesDir(null)?.let {
                setDirectory(it.absolutePath)
            }

            onBackPressEvent.observe(this@MainActivity, Observer { isStartRecord ->
                if (isStartRecord) {
                    stopRecord()
                } else {
                    super.onBackPressed()
                }
            })

            recordingTime.observe(this@MainActivity, Observer {
                updateTimerText(it)
            })

            errorToastEvent.observe(this@MainActivity, Observer {
                Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
            })

            voiceRecordedEvent.observe(this@MainActivity, Observer {

                mAdapter.addItem(it)

            })

        }

    }

    private fun initView() {

        viewBinding.apply {

            groupVoiceRecord.isVisible = false

            voiceEnableButton.setOnClickListener {
                toggleVoiceEnableButton()
            }

            voiceRecordButton.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startRecord()
                    }
                    MotionEvent.ACTION_UP -> {
                        stopRecord()
                    }
                }
                true
            }
            edtText.setOnClickListener {
                hideVoiceRecordView()
            }

            edtText.setOnFocusChangeListener { view, b ->
                hideVoiceRecordView()
            }

            mAdapter.setHasStableIds(true)
            chatView.adapter = mAdapter
            mAdapter.setOnClickListener {
                viewModel.apply {
                    audioSteamHelper.apply {

                        mAdapter.notifyDataSetChanged()

                        if (isAudioPlaying()){
                            if (isPlaySameAudio(it)){
                                stopAudioPlayer()
                                Toast.makeText(this@MainActivity, "Stop", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }
                        }

                        Toast.makeText(this@MainActivity, "Play", Toast.LENGTH_SHORT).show()
                        startAudioPlayer(it)

                    }
                }
            }
            for (i in 1..50) {
                mAdapter.addItem(VoiceChatModel(i + 99, Mock.link1, 60000))
            }

        }

    }

    private fun updateItemRecyclerView(voiceChatModel: VoiceChatModel, currentDuaration: Long) {
        viewBinding.apply {
            chatView.findViewHolderForItemId(voiceChatModel.id.toLong())?.let {
                (it as VoiceChatViewHolder).updatePlayingView(currentDuaration)
            }
        }
    }

    private fun startRecord() {
        viewModel.isStartRecord = true
        updateRecordButton(true)
        showTimerText(true)
        viewModel.startRecording()
    }


    private fun stopRecord() {
        viewModel.isStartRecord = false
        updateRecordButton(false)
        showTimerText(false)
        viewModel.stopRecording()
    }

    private fun showTimerText(isShow: Boolean) {
        viewBinding.tvRecordTime.isVisible = isShow
    }

    private fun updateTimerText(time: Long) {

        val minutes = time / 1000 / 60
        val seconds = time / 1000 % 60

        viewBinding.tvRecordTime.text =
            String.format(getString(R.string.text_record_time), minutes, seconds)
    }

    private fun toggleVoiceEnableButton() {
        viewBinding.apply {
            KeyboardUtils.hideKeyboard(edtText)
            voiceEnableButton.isActivated = voiceEnableButton.isActivated.not()
            groupVoiceRecord.isVisible = voiceEnableButton.isActivated
            updateVoiceEnableButton(voiceEnableButton.isActivated)
        }
    }

    private fun hideVoiceRecordView() {
        viewBinding.apply {
            voiceEnableButton.isActivated = false
            groupVoiceRecord.isVisible = false
            updateVoiceEnableButton(false)
        }
    }

    private fun updateVoiceEnableButton(isActivated: Boolean) {
        viewBinding.apply {
            when (isActivated) {
                true -> {
                    voiceEnableButton.setImageResource(R.drawable.mic_green)
                }
                false -> {
                    voiceEnableButton.setImageResource(R.drawable.mic_white)
                }
            }
        }
    }

    private fun updateRecordButton(isActivated: Boolean) {
        viewBinding.apply {
            when (isActivated) {
                true -> {
                    voiceRecordButton.setImageResource(R.drawable.mic_red)
                    voiceRecordButton.setBackgroundResource(R.drawable.circle_border_red)
                }
                false -> {
                    voiceRecordButton.setImageResource(R.drawable.mic_black)
                    voiceRecordButton.setBackgroundResource(R.drawable.circle_border_black)
                }
            }
        }
    }

    /* override method */

    override fun onBackPressed() {
        viewModel.triggerOnBackPress()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }


    override fun onStop() {
        super.onStop()
        viewModel.clearRecorderAndPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioSteamHelper.stopAudioPlayer()
        job.cancel()
    }

    override fun onPlaying(voiceChatModel: VoiceChatModel, duration: Int) {
        updateItemRecyclerView(voiceChatModel, duration.toLong())
    }

}