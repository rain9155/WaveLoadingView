package com.example.waveloadingview

import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.example.library.WaveLoadingView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener{

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        //...
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        //...
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when(seekBar?.id){
            R.id.sbAmplitude -> {
                val pro = progress  * 1f / seekBar.max
                tvAmplitudeProcess.text = String.format("%.2f", pro)
                wl.waveAmplitude = pro
            }
            R.id.sbVelocity -> {
                val pro = progress * 1f / seekBar.max
                tvVelocityProcess.text = String.format("%.2f", pro)
                wl.waveVelocity = pro
            }
            R.id.sbProcess -> {
                tvProcesses.text = "$progress"
                wl.process = progress
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sbAmplitude.setOnSeekBarChangeListener(this)
        sbVelocity.setOnSeekBarChangeListener(this)
        sbProcess.setOnSeekBarChangeListener(this)

        sbAmplitude.progress = (wl.waveAmplitude * 100).toInt()
        sbVelocity.progress = (wl.waveVelocity * 100).toInt()
        sbProcess.progress = wl.process
        btnPauseAnim.setOnClickListener {
            wl.pauseLoading()
        }
        btnResumeAnim.setOnClickListener {
            wl.resumeLoading()
        }
        btnCancelAnim.setOnClickListener {
            wl.cancelLoading()
        }
        btnStartAnim.setOnClickListener {
            wl.startLoading()
        }

    

    }
}
