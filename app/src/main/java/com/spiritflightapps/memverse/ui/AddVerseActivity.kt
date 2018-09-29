package com.spiritflightapps.memverse.ui

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.spiritflightapps.memverse.CurrentUser
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.ui.ui.addverse.AddVerseFragment
import org.jetbrains.anko.startActivity

class AddVerseActivity : AppCompatActivity() {
    // TODO: do this feature carefully with singleactivity app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_verse_activity)
        if (CurrentUser.isLoggedIn.not()) {
            startActivity<LoginActivity>()
            return
        }

        val action: String? = intent?.action
        val data: Uri? = intent?.data

        Log.d("NJW", "intent action = $action")
        Log.d("NJW", "intent uri = $data")

        // adb shell am start
        //        -W -a android.intent.action.VIEW
        //        -d "example://gizmos" com.example.android

        // handle deeplink sfa://memverse/add?book=Colossians&chapter=1&verse=17&translation=NIV
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AddVerseFragment.newInstance())
                    .commitNow()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        //dosoething
        Log.d("NJW", "dostf")
    }


}