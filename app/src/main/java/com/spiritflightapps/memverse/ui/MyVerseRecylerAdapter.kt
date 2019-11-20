package com.spiritflightapps.memverse.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Memverse



import kotlinx.android.synthetic.main.item_verse.view.*

class MyVerseRecylerAdapter(private val verses: List<Memverse> )
    : RecyclerView.Adapter<MyVerseRecylerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_verse, parent, false)
        return ViewHolder(view)
    }

    // https://antonioleiva.com/recyclerview-adapter-kotlin/
    // todo: Change to databinding
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(verses[position])

    override fun getItemCount(): Int = verses.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(verse: Memverse) = with(itemView) {
            text_verse_ref.text = verse.ref
            text_next_test_date.text = verse.next_test
            text_status.text = verse.status

        }
    }
}
