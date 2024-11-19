package com.example.foodapp.adapter.Home


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.auth.FirebaseAuth
import com.example.foodapp.R
import com.example.foodapp.activity.Home.ChatDetailActivity
import com.example.foodapp.databinding.ItemChatBinding
import com.example.foodapp.model.ItemChatRoom


class ChatAdapter(
    private val context: Context,
    private val bunchOfItemChatRooms: ArrayList<ItemChatRoom>
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>(), Filterable {

    private val viewBinderHelper = ViewBinderHelper().apply { setOpenOnlyOne(true) }
    private var currentBunchOfItemChatRooms = bunchOfItemChatRooms

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemChatRoom = currentBunchOfItemChatRooms[position]
        with(holder.binding) {
            viewBinderHelper.bind(SwipeRevealLayout, itemChatRoom.receiver.userId)
            txtNameUser.text = itemChatRoom.receiver.userName
            txtLastMessage.setTextColor(context.getColor(R.color.app_color2))
            if (itemChatRoom.lastMessage?.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                imgNewMessage.visibility = View.INVISIBLE
                txtLastMessage.setTextColor(context.getColor(R.color.gray))
            } else {
                if (itemChatRoom.lastMessage?.isSeen == true) {
                    imgNewMessage.visibility = View.INVISIBLE
                    txtLastMessage.setTextColor(context.getColor(R.color.gray))
                } else {
                    imgNewMessage.visibility = View.VISIBLE
                }
            }
            Glide.with(context)
                .load(itemChatRoom.receiver.avatarURL)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.image_default)
                .into(lnItemChat.imgUser)
            layout.setOnClickListener {
                val intent = Intent(context, ChatDetailActivity::class.java).apply {
                    action = "chatActivity"
                    putExtra("publisher", itemChatRoom.receiver)
                }
                context.startActivity(intent)
            }
            txtLastMessage.text = itemChatRoom.lastMessage?.content ?: ""
        }
    }

    override fun getItemCount(): Int = currentBunchOfItemChatRooms.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val key = constraint.toString().lowercase()
                currentBunchOfItemChatRooms = if (key.trim().isEmpty()) {
                    bunchOfItemChatRooms
                } else {
                    bunchOfItemChatRooms.filter {
                        it.receiver.userName?.lowercase()?.contains(key) ?: false
                    } as ArrayList<ItemChatRoom>
                }
                return FilterResults().apply { values = currentBunchOfItemChatRooms }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currentBunchOfItemChatRooms = results?.values as ArrayList<ItemChatRoom>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)
}
