package com.gpetuhov.android.hive.ui.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.model.Message
import com.gpetuhov.android.hive.domain.repository.Repo
import kotlinx.android.synthetic.main.item_message.view.*
import javax.inject.Inject

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    @Inject lateinit var context: Context
    @Inject lateinit var repo: Repo

    private var messageList = mutableListOf<Message>()

    init {
        HiveApp.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount() = messageList.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bindMessage(message.text, message.getMessageTime(), message.isFromUser(repo.currentUserUid()))
    }

    // === Public methods ===

    fun setMessages(messages: MutableList<Message>) {
        messageList.clear()
        messageList.addAll(messages)
        notifyDataSetChanged()
    }

    // === Inner classes ===

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var view = itemView

        fun bindMessage(text: String, time: String, isFromCurrentUser: Boolean) {
            view.apply {
                item_message_text.text = text
                item_message_time.text = time

                // Set message layout depending on message sender (current user or not)
                message_root_layout.gravity = if (isFromCurrentUser) Gravity.END else Gravity.START
                message_left_space.visibility = if (isFromCurrentUser) View.VISIBLE else View.GONE
                message_right_space.visibility = if (isFromCurrentUser) View.GONE else View.VISIBLE
                item_message_wrapper.setBackgroundResource(
                    if (isFromCurrentUser) R.drawable.message_background_current_user else R.drawable.message_background
                )
            }
        }
    }
}