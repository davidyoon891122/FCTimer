package com.example.chapter6

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.example.chapter6.databinding.ActivityMainBinding
import com.example.chapter6.databinding.DialogCountdownSettingBinding
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var countDownSecond = 4
    private var currentCountDownDeciSecond = countDownSecond * 10
    private var currentDeciSecond = 0
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.countDownTextView.setOnClickListener {
            showCountDownSettingDialog()
        }

        binding.startButton.setOnClickListener {
            start()
            binding.startButton.isVisible = false
            binding.stopButton.isVisible = false
            binding.pauseButton.isVisible = true
            binding.lapButton.isVisible = true
        }

        binding.stopButton.setOnClickListener {
            showAlertDialog()
        }

        binding.pauseButton.setOnClickListener {
            pause()
            binding.startButton.isVisible = true
            binding.stopButton.isVisible = true
            binding.pauseButton.isVisible = false
            binding.lapButton.isVisible = false
        }

        binding.lapButton.setOnClickListener {
            lap()
        }

        initCountdownViews()
    }

    private fun initCountdownViews() {
        binding.countDownTextView.text = String.format("%02d", countDownSecond)
        binding.countDownProgressBar.progress = 100
    }

    private fun start() {
        timer = timer(initialDelay = 0, period = 100) {
            if (currentCountDownDeciSecond == 0) {
                currentDeciSecond += 1
                val minutes = currentDeciSecond.div(10) / 60
                val seconds = currentDeciSecond.div(10) % 60
                val deciSeconds = currentDeciSecond % 10

                runOnUiThread {
                    binding.timeTextView.text = String.format("%02d:%02d", minutes, seconds)
                    binding.tickTextView.text = deciSeconds.toString()


                    binding.countDownGroup.isVisible = false
                }
            } else {
                currentCountDownDeciSecond -= 1
                val seconds = currentCountDownDeciSecond / 10
                val progress = (currentCountDownDeciSecond * 100 / (countDownSecond * 10))
                Log.d("progress", progress.toString())
                binding.root.post {
                    binding.countDownTextView.text = String.format("%02d", seconds)
                    binding.countDownProgressBar.progress = progress.toInt()
                }
            }

            if (currentDeciSecond == 0 && currentCountDownDeciSecond < 31 && currentCountDownDeciSecond % 10 == 0) {
                val toneType = if(currentCountDownDeciSecond == 0) ToneGenerator.TONE_CDMA_HIGH_L else ToneGenerator.TONE_CDMA_ANSWER
                ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
                    .startTone(toneType, 100)
            }

        }
    }

    private fun stop() {
        binding.startButton.isVisible = true
        binding.stopButton.isVisible = true
        binding.pauseButton.isVisible = false
        binding.lapButton.isVisible = false

        currentDeciSecond = 0
        countDownSecond = 4
        binding.timeTextView.text = "00:00"
        binding.tickTextView.text = "0"

        binding.countDownGroup.isVisible = true
        initCountdownViews()

        binding.lapContainerLinearLayout.removeAllViews()
    }

    private fun pause() {
        timer?.cancel()
        timer = null

    }

    private fun lap() {

        if (currentDeciSecond == 0) return
        val container = binding.lapContainerLinearLayout
        TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            val minutes = currentDeciSecond / 60
            val seconds = currentDeciSecond.div(10) % 60
            val deciSecond = currentDeciSecond % 10
            text = "${container.childCount.inc()}. " + String.format("%02d:%02d %01d", minutes, seconds, deciSecond)
            // 1. 01.03 0
            setPadding(30)
        }.let { lapTextView ->
            container.addView(lapTextView, 0 )
        }

    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("종료하시겠습니까?")
            setPositiveButton("네") { dialog, id ->
                stop()
            }
            setNegativeButton("아니요", null)
        }.show()
    }

    private fun showCountDownSettingDialog() {
        AlertDialog.Builder(this).apply {
            val dialogBinding = DialogCountdownSettingBinding.inflate(layoutInflater)
            with(dialogBinding.countDownSecondPicker) {
                maxValue = 20
                minValue = 0
                value = countDownSecond
            }
            setView(dialogBinding.root)
            setTitle("카운트다운 설정")
            setPositiveButton("확인") { _, _ ->
                countDownSecond = dialogBinding.countDownSecondPicker.value
                currentCountDownDeciSecond = countDownSecond * 10
                binding.countDownTextView.text = String.format("%02d", countDownSecond)
            }
            setNegativeButton("취소", null)
        }.show()
    }

}