package com.example.foodapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.GlobalConfig
import com.example.foodapp.Interface.IAddressAdapterListener
import com.example.foodapp.activity.Cart_PlaceOrder.UpdateAddAddressActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ItemAddressBinding
import com.example.foodapp.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AddressAdapter(
    private val context: Context,
    private val addresses: List<Address>?,
    private val userId: String
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    private var checkedRadioButton: RadioButton? = null
    private var addressAdapterListener: IAddressAdapterListener? = null
    private companion object {
        const val UPDATE_ADDRESS_REQUEST_CODE = 100
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addresses?.get(position) ?: return

        with(holder.binding) {
            when {
                address.addressId == GlobalConfig.choseAddressId && address.state == "default" -> {
                    choose.isChecked = true
                    defaultText.visibility = View.VISIBLE
                    checkedRadioButton = choose
                }
                address.addressId == GlobalConfig.choseAddressId -> {
                    choose.isChecked = true
                    defaultText.visibility = View.INVISIBLE
                    checkedRadioButton = choose
                }
                address.state == "default" -> {
                    defaultText.visibility = View.VISIBLE
                    choose.isChecked = false
                }
                else -> {
                    choose.isChecked = false
                    defaultText.visibility = View.INVISIBLE
                }
            }

            receiverName.text = address.receiverName
            receiverPhoneNumber.text = address.receiverPhoneNumber
            detailAddress.text = address.detailAddress

            choose.setOnClickListener {
                if (choose.isChecked) {
                    checkedRadioButton?.isChecked = false
                    addressAdapterListener?.onCheckedChanged(address)
                    checkedRadioButton = choose
                }
            }

            update.setOnClickListener {
                GlobalConfig.updateAddressId = address.addressId
                val intent = Intent(context, UpdateAddAddressActivity::class.java).apply {
                    putExtra("mode", "update")
                    putExtra("userId", userId)
                }
                (context as Activity).startActivityForResult(intent, UPDATE_ADDRESS_REQUEST_CODE)
            }

            root.setOnLongClickListener {
                val customAlertDialog = CustomAlertDialog(context, "Delete this address?").apply {
                    binding.btnYes.setOnClickListener {
                        if (address.state == "default") {
                            FailToast(context, "You cannot delete the default address!").showToast()
                            alertDialog.dismiss()
                        } else {
                            address.addressId?.let { addressId ->
                                FirebaseDatabase.getInstance()
                                    .getReference("Address")
                                    .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                                    .child(addressId)
                                    .removeValue()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            SuccessfulToast(context, "Delete address successfully!").showToast()
                                            alertDialog.dismiss()
                                            addressAdapterListener?.onDeleteAddress()
                                        }
                                    }
                            }
                        }
                    }
                    binding.btnNo.setOnClickListener {
                        alertDialog.dismiss()
                    }
                }

                customAlertDialog.showAlertDialog()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return addresses?.size ?: 0
    }

    fun setAddressAdapterListener(listener: IAddressAdapterListener) {
        this.addressAdapterListener = listener
    }

    class ViewHolder(val binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root)
}





