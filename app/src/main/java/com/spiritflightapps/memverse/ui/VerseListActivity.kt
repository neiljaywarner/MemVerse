package com.spiritflightapps.memverse.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spiritflightapps.memverse.CurrentUser
import com.spiritflightapps.memverse.R
import org.jetbrains.anko.startActivity

class VerseListActivity : AppCompatActivity() {
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
                    .replace(R.id.container, VerseListFragment.newInstance())
                    .commitNow()
        }
    }

}