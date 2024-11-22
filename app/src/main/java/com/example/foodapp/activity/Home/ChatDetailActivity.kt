package com.example.foodapp.activity.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.foodapp.R
import com.example.foodapp.adapter.Home.ChatDetailAdapter
import com.example.foodapp.databinding.ActivityChatDetailBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.model.Message
import com.example.foodapp.model.Notification
import com.example.foodapp.model.User

class ChatDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatDetailBinding
    private var publisherId: String? = null
    private val publisher = MutableLiveData<User>()
    private val messages = ArrayList<Message>()
    private lateinit var userId: String
    private lateinit var uploadDialog: LoadingDialog
    private lateinit var chatDetailAdapter: ChatDetailAdapter
    private val messageReference = FirebaseDatabase.getInstance().getReference("Message")
    private var messageListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launchLoadingDialog()
        registerForObserver()
        registerListenerForMessage()
        initData()
    }

    private fun registerListenerForMessage() {
        messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) ?: return
                if (message.senderId != userId) {
                    val messageReferenceOfSender =
                        message.idMessage?.let {
                            messageReference.child(userId).child(publisherId!!).child(
                                it
                            ).child("seen")
                        }
                    val messageReferenceOfReceiver =
                        message.idMessage?.let {
                            messageReference.child(publisherId!!).child(userId).child(
                                it
                            ).child("seen")
                        }
                    if (messageReferenceOfSender != null) {
                        setMessageSeen(messageReferenceOfSender)
                    }
                    if (messageReferenceOfReceiver != null) {
                        setMessageSeen(messageReferenceOfReceiver)
                    }
                }
                messages.add(message)
                chatDetailAdapter.notifyItemInserted(messages.size - 1)
                binding.recycleViewMessage.scrollToPosition(chatDetailAdapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val newMessage = snapshot.getValue(Message::class.java) ?: return
                val messageKey = snapshot.key ?: return

                for (i in messages.indices) {
                    if (messages[i].idMessage == messageKey) {
                        messages[i] = newMessage
                        chatDetailAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
    }

    private fun initUI() {
        createAdapter()
        loadDataIntoUI()
        initEventOfComponent()
    }

    private fun createAdapter() {
        chatDetailAdapter = ChatDetailAdapter(this, messages, userId, publisherId!!)
    }

    private fun loadDataIntoUI() {
        val currentPublisher = publisher.value ?: return
        Glide.with(this)
            .load(currentPublisher.avatarURL)
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.image_default)
            .into(binding.imgPublisher)
        binding.txtNamePublisher.text = currentPublisher.userName
        binding.recycleViewMessage.layoutManager = LinearLayoutManager(this)
        binding.recycleViewMessage.adapter = chatDetailAdapter
    }

    private fun initEventOfComponent() {
        setBackEvent()
        setSendMessageEvent()
    }

    private fun setSendMessageEvent() {
        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            sendMessage(message)
        }
    }

    private fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            val newMessage = Message(message, userId, System.currentTimeMillis(), false)
            loadMessageToFirebase(newMessage)
        }
    }

    private fun loadMessageToFirebase(newMessage: Message) {
        val userMessageReference = messageReference.child(userId).child(publisherId!!).push()
        newMessage.idMessage = userMessageReference.key
        userMessageReference.setValue(newMessage).addOnSuccessListener {
            newMessage.idMessage?.let { it1 ->
                messageReference.child(publisherId!!).child(userId).child(
                    it1
                ).setValue(newMessage)
            }
            binding.edtMessage.setText("")
            pushSendMessageNotification()
        }
    }

    private fun pushSendMessageNotification() {
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sender = snapshot.getValue(User::class.java) ?: return
                    val title = "New message"
                    val content = "${sender.userName} sent you a new message. Please check it!"
                    val notification = if (sender.avatarURL?.isNotEmpty() == true) {
                        sender.avatarURL?.let {
                            FirebaseNotificationHelper.createNotification(
                                title, content, it, "None", "None", "None", sender
                            )
                        }
                    } else {
                        FirebaseNotificationHelper.createNotification(
                            title, content, "https://t4.ftcdn.net/jpg/01/18/03/35/360_F_118033506_uMrhnrjBWBxVE9sYGTgBht8S5liVnIeY.jpg",
                            "None", "None", "None", sender
                        )
                    }
                    if (notification != null) {
                        FirebaseNotificationHelper(this@ChatDetailActivity)
                            .addNotification(publisherId!!, notification, object : FirebaseNotificationHelper.DataStatus {
                                override fun DataIsLoaded(notificationList: List<Notification>, notificationListToNotify: List<Notification>) {}
                                override fun DataIsInserted() {}
                                override fun DataIsUpdated() {}
                                override fun DataIsDeleted() {}
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setBackEvent() {
        binding.imgBack.setOnClickListener { finish() }
    }

    private fun launchLoadingDialog() {
        createLoadingDialog()
        uploadDialog.show()
    }

    private fun createLoadingDialog() {
        uploadDialog = LoadingDialog(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun registerForObserver() {
        publisher.observe(this, Observer {
            if (it != null) {
                handleChangedObserver()
            }
        })
    }

    private fun handleChangedObserver() {
        loadMessage()
        initUI()
        uploadDialog.dismiss()
    }

    private fun initData() {
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadIntent()
    }

    private fun loadMessage() {
        FirebaseDatabase.getInstance().getReference("Message").child(userId).child(publisherId!!)
            .addChildEventListener(messageListener!!)
    }

    private fun setMessageSeen(reference: DatabaseReference) {
        reference.setValue(true)
    }

    private fun loadIntent() {
        val intent = intent
        when {
            isFromChatActivity(intent.action) -> handleIntentFromChatActivity(intent)
            isFromHomeActivity(intent.action) -> handleIntentFromHomeActivity(intent)
            else -> handleIntentFromProductInfoActivity(intent)
        }
    }

    private fun handleIntentFromHomeActivity(intent: Intent) {
        val user = intent.getSerializableExtra("publisher") as User
        publisherId = user.userId
        notifyToObserver(publisher, user)
    }

    private fun handleIntentFromProductInfoActivity(intent: Intent) {
        publisherId = intent.getStringExtra("publisherId")
        initPublisher(publisherId!!)
    }

    private fun handleIntentFromChatActivity(intent: Intent) {
        val publisherTemp = intent.getSerializableExtra("publisher") as User
        publisherId = publisherTemp.userId
        notifyToObserver(publisher, publisherTemp)
    }

    private fun notifyToObserver(mutableLiveData: MutableLiveData<User>, obj: User) {
        mutableLiveData.postValue(obj)
    }

    private fun initPublisher(publisherId: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(publisherId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    publisher.postValue(snapshot.getValue(User::class.java))
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun isFromChatActivity(action: String?) = action.equals("chatActivity", ignoreCase = true)
    private fun isFromHomeActivity(action: String?) = action.equals("homeActivity", ignoreCase = true)
}
