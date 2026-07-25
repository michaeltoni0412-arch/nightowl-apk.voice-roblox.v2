package com.nightowl.voice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VoiceAdapter(
    private val presets: List<VoicePreset>,
    private val onTap: (VoicePreset) -> Unit
) : RecyclerView.Adapter<VoiceAdapter.VoiceViewHolder>() {

    private var selectedIndex = 0

    class VoiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.voiceName)
        val mark: TextView = view.findViewById(R.id.selectedMark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voice, parent, false)
        return VoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoiceViewHolder, position: Int) {
        val preset = presets[position]
        holder.name.text = preset.name
        holder.mark.visibility = if (position == selectedIndex) View.VISIBLE else View.INVISIBLE
        holder.itemView.setOnClickListener {
            val prev = selectedIndex
            selectedIndex = position
            notifyItemChanged(prev)
            notifyItemChanged(selectedIndex)
            onTap(preset)
        }
    }

    override fun getItemCount() = presets.size
}
