package com.wepon.graphview

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 一些属性的设置
//        gvView.mEndGradientColor = Color.YELLOW
//        gvView.mScoreLineColor = Color.RED

        btSetScore.setOnClickListener {
            val random = Random()
            val size = random.nextInt(4) + 4
            val intArray = IntArray(size)
            for (i in 0 until size) {
                intArray[i] = random.nextInt(100)
            }
            gvView.mScoreData = intArray
        }
    }
}
