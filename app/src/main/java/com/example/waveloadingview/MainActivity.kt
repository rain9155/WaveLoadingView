package com.example.waveloadingview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
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
                tvAmplitudeProcess.text = "$pro"
            }
            R.id.sbVelocity -> {
                val pro = progress * 1f / seekBar.max
                tvVelocityProcess.text = "$pro"
            }
            R.id.sbProcess -> {
                tvProcesses.text = "$progress"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sbAmplitude.setOnSeekBarChangeListener(this)
        sbVelocity.setOnSeekBarChangeListener(this)
        sbProcess.setOnSeekBarChangeListener(this)

    }
}
