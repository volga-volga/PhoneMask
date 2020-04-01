package ru.vvdev.phonemask.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_format.view.*
import ru.vvdev.phonemask.R
import ru.vvdev.phonemask.util.getDrawableForFormat
import ru.vvdev.phonemask.model.Format

class FormatAdapter(val context: Context?, val data: List<Format>, val listener: Listener) :
    RecyclerView.Adapter<FormatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_format, parent, false)
    )

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val format = data[position]
        holder?.apply {
            country.text = format.name

            context?.let {
                it.getDrawableForFormat(format)?.let {
                    flag.setImageDrawable(it)
                }
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val flag = itemView.ivFlag
        val country = itemView.tvCountry

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.formatClicked(data[adapterPosition])
        }
    }

    interface Listener {
        fun formatClicked(format: Format)
    }
}