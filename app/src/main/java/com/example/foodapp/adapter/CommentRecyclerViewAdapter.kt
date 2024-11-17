package com.example.foodapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.example.foodapp.databinding.ItemCommentListBinding
import com.example.foodapp.model.Comment

class CommentRecyclerViewAdapter(
    private val context: Context,
    private val commentList: List<Comment>,
    private val keys: List<String>
) : RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentListBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]

        // Retrieve username from Firebase
        FirebaseDatabase.getInstance().getReference("Users")
            .child(comment.publisherId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("userName").getValue(String::class.java)
                    holder.binding.txtRecCommentUsername.text = name ?: "Unknown User"
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })

        // Set other comment details
        holder.binding.txtRecCommentComment.text = comment.commentDetail
        holder.binding.recCommentRatingBar.rating = comment.rating
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    class ViewHolder(val binding: ItemCommentListBinding) : RecyclerView.ViewHolder(binding.root)
}
