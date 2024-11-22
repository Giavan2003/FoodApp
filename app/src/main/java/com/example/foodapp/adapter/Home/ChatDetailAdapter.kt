package com.example.foodapp.adapter.Home



import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.databinding.ItemReceiveMessageBinding
import com.example.foodapp.databinding.ItemSendMessageBinding
import com.example.foodapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatDetailAdapter(
    private val context: Context,
    private val messages: List<Message>,
    private val userId: String,
    private val publisherId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SEND_MESSAGE = 0
        private const val TYPE_RECEIVE_MESSAGE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SEND_MESSAGE) {
            val binding = ItemSendMessageBinding.inflate(LayoutInflater.from(context), parent, false)
            SendMessageViewHolder(binding)
        } else {
            val binding = ItemReceiveMessageBinding.inflate(LayoutInflater.from(context), parent, false)
            ReceiveMessageViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == userId) TYPE_SEND_MESSAGE else TYPE_RECEIVE_MESSAGE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SendMessageViewHolder -> holder.bind(message)
            is ReceiveMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SendMessageViewHolder(private val binding: ItemSendMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.txtMessage.text = message.content
            binding.txtTime.text = formatTime(message.timeStamp)
        }
    }

    inner class ReceiveMessageViewHolder(private val binding: ItemReceiveMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.txtMessage.text = message.content
            binding.txtTime.text = formatTime(message.timeStamp)
            binding.imgDoubleCheck.visibility = if (message.isSeen) View.VISIBLE else View.GONE
        }
    }

    private fun formatTime(time: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(time))
    }
}
