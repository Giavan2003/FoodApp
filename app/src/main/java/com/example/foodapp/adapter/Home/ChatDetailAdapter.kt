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
    private val ds: ArrayList<Message>,
    private val userId: String,
    private val publisherId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_SEND = 1001
        const val ITEM_RECEIVE = 1002
        const val NULL_ITEM = 1003
    }

    private val formatHour = SimpleDateFormat("HH:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SEND) {
            SendViewHolder(ItemSendMessageBinding.inflate(LayoutInflater.from(context), parent, false))
        } else {
            ReceiveViewHolder(ItemReceiveMessageBinding.inflate(LayoutInflater.from(context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = ds[position]

        if (getItemViewType(position) == ITEM_SEND) {
            val viewHolder = holder as SendViewHolder
            with(viewHolder.binding) {
                txtMessage.text = message.content
                val timeText = formatHour.format(Date(message.timeStamp))
                if (message.isSeen) {
                    txtTime.text = "$timeText Seen"
                    imgCheck.visibility = View.GONE
                } else {
                    txtTime.text = "$timeText Sent"
                    imgDoubleCheck.visibility = View.GONE
                }
            }
        } else {
            val viewHolder = holder as ReceiveViewHolder
            with(viewHolder.binding) {
                txtMessage.text = message.content
                txtTime.text = formatHour.format(Date(message.timeStamp))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (ds.isNotEmpty()) {
            val message = ds[position]
            if (message.senderId == userId) ITEM_SEND else ITEM_RECEIVE
        } else {
            NULL_ITEM
        }
    }

    override fun getItemCount(): Int = ds.size

    inner class SendViewHolder(val binding: ItemSendMessageBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ReceiveViewHolder(val binding: ItemReceiveMessageBinding) : RecyclerView.ViewHolder(binding.root)
}
