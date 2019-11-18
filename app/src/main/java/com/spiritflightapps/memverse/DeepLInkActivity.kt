package com.spiritflightapps.memverse

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class DeepLInkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link)

        val action: String? = intent?.action
        val data: Uri? = intent?.data

        Log.d("NJW", "intent action = $action")
        Log.d("NJW", "intent uri = $data")
        // todo: make dyn link
        // also fix share version by sending first char analytics only if ther eare...

        // adb shell am start
        //        -W -a android.intent.action.VIEW
        //        -d "example://gizmos" com.example.android

        // handle deeplink sfa://memverse/add?book=Colossians&chapter=1&verse=17&translation=NIV
    }
}
