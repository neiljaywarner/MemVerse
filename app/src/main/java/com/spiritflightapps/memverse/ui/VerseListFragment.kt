package com.spiritflightapps.memverse.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spiritflightapps.memverse.CurrentUser
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.ui.dummy.DummyContent
import kotlinx.android.synthetic.main.fragment_verse_list.*


class VerseListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_verse_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.adapter = MyVerseRecylerAdapter(CurrentUser.memverses)
    }

    // TODO: maybe a verse list type? refs, list type pending, etc, etc.
    //bundle, etc
    companion object {
        @JvmStatic
        fun newInstance() = VerseListFragment()
    }



}
