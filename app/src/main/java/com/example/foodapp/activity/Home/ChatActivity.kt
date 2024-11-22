package com.example.foodapp.activity.Home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.example.foodapp.adapter.Home.ChatAdapter
import com.example.foodapp.databinding.ActivityChatBinding
import com.example.foodapp.model.ItemChatRoom
import com.example.foodapp.model.Message
import com.example.foodapp.model.User
import java.util.concurrent.atomic.AtomicInteger

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var userId: String
    private lateinit var userReference: DatabaseReference
    private val bunchOfReceiverId = MutableLiveData<ArrayList<String>>()
    private val bunchOfItemChatRoom = ArrayList<ItemChatRoom>()
    private val bunchOfItemChatRoomLive = MutableLiveData<ArrayList<ItemChatRoom>>()
    private lateinit var chatReference: DatabaseReference
    private lateinit var chatListener: ValueEventListener
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getStringExtra("userId") ?: ""

        initToolbar()
        initUI()
        initFilterForSearchView()
        createReference()
        createObserver()
        createChatListener()
        loadReceiverId()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Messages"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initFilterForSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                chatAdapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                chatAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun createObserver() {
        createObserverOfReceiverId()
        createObserverOfItemChatRoom()
    }

    private fun createObserverOfItemChatRoom() {
        bunchOfItemChatRoomLive.observe(this) { chatRooms ->
            chatAdapter.notifyDataSetChanged()
        }
    }

    private fun initUI() {
        chatAdapter = ChatAdapter(this, bunchOfItemChatRoom)
        binding.recycleViewMessage.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun createObserverOfReceiverId() {
        bunchOfReceiverId.observe(this) { receiverIds ->

            loadReceiver(receiverIds)
        }
    }

    private fun loadReceiver(receiverIds: ArrayList<String>) {
        bunchOfItemChatRoom.clear()

        receiverIds.forEach { receiverId ->
            userReference.child(receiverId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val receiver = snapshot.getValue(User::class.java)
                    receiver?.let { includeGetItemChatFromReceiver(it, receiverIds) }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun includeGetItemChatFromReceiver(receiver: User, receiverIds: ArrayList<String>) {

        receiver.userId?.let {
            chatReference.child(it).orderByChild("timeStamp").limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val lastMessage = snapshot.children.first().getValue(Message::class.java)
                            val itemChatRoom = ItemChatRoom(receiver, lastMessage!!)
                            bunchOfItemChatRoom.add(itemChatRoom)
                            if (bunchOfItemChatRoom.size == receiverIds.size) {
                                bunchOfItemChatRoomLive.postValue(bunchOfItemChatRoom)
                            }
                        } else {
                            Toast.makeText(this@ChatActivity, "include", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
        Log.d("sie", bunchOfItemChatRoom.size.toString())
    }

    private fun createChatListener() {
        chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receiverIds = ArrayList<String>()
                includeHandleWhenChatChanged(receiverIds, snapshot)
                bunchOfReceiverId.postValue(receiverIds)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
    }

    private fun includeHandleWhenChatChanged(receiverIds: ArrayList<String>, snapshot: DataSnapshot) {
        snapshot.children.forEach { item ->
            receiverIds.add(item.key!!)
        }
    }

    private fun createReference() {
        chatReference = FirebaseDatabase.getInstance().getReference("Message").child(userId)
        userReference = FirebaseDatabase.getInstance().getReference("Users")
    }

    private fun loadReceiverId() {
        chatReference.addValueEventListener(chatListener)
    }
}