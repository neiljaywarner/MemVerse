package com.spiritflightapps.memverse.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AddVerseFragment.newInstance())
                    .commitNow()
        }
    }


}