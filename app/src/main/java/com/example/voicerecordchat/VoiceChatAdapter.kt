package com.example.voicerecordchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

class VoiceChatAdapter(private val chatItem:MutableList<VoiceChatModel>):RecyclerView.Adapter<VoiceChatViewHolder>(){

    private var onClickListener : ((VoiceChatModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return VoiceChatViewHolder(inflater.inflate(R.layout.voice_chat_item, parent, false))
    }

    override fun onBindViewHolder(holder: VoiceChatViewHolder, position: Int) {

        holder.bind(chatItem[position])
        holder.itemView.findViewById<AppCompatImageView>(R.id.ivPlayer).setOnClickListener {
            onClickListener?.invoke(chatItem[position])
        }

    }

    override fun getItemId(position: Int): Long {
        return chatItem[position].id.toLong()
    }

    override fun getItemCount(): Int = chatItem.size

    fun setOnClickListener(callback :  ((VoiceChatModel) -> Unit)){
        onClickListener = callback
    }

    fun addItem(item :VoiceChatModel){
        chatItem.add(item)
        notifyDataSetChanged()
    }


}

class VoiceChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val TAG_PLAY_VIEW = "TAG_PLAY_VIEW"
    private val TAG_STOP_VIEW = "TAG_STOP_VIEW"

    private var tvDuration : AppCompatTextView? = null

    private var voiceChatModel : VoiceChatModel? = null

    fun bind(voiceChatModel: VoiceChatModel) {

        this.voiceChatModel = voiceChatModel

        tvDuration = itemView.findViewById<AppCompatTextView>(R.id.tvDuration)

        tvDuration?.text = voiceChatModel.duration.toFormattedDuration(itemView.context)

        itemView.findViewById<AppCompatImageView>(R.id.ivPlayer)?.let {
                it.setImageResource(android.R.drawable.ic_media_play)
        }

        itemView.tag = TAG_PLAY_VIEW

    }

    fun updatePlayingView(duration:Long){
        itemView.findViewById<AppCompatImageView>(R.id.ivPlayer)?.let {
            if (itemView.tag == TAG_PLAY_VIEW){
                it.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
        tvDuration?.text = duration.toFormattedDuration(itemView.context)
    }


}

