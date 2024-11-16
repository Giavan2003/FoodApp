package com.example.foodapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.google.firebase.database.FirebaseDatabase


class AddressAdapter(
    private val mContext: Context,
    private val mAddresses: List<Address>,
    private val userId: String
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    private var checkedRadioButton: RadioButton? = null
    private var addressAdapterListener: IAddressAdapterListener? = null

    companion object {
        private const val UPDATE_ADDRESS_REQUEST_CODE = 100
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = mAddresses[position]

        if (address.addressId == GlobalConfig.choseAddressId && address.state == "default") {
            holder.binding.choose.isChecked = true
            holder.binding.defaultText.visibility = View.VISIBLE
            checkedRadioButton = holder.binding.choose
        } else if (address.addressId == GlobalConfig.choseAddressId) {
            holder.binding.choose.isChecked = true
            holder.binding.defaultText.visibility = View.INVISIBLE
            checkedRadioButton = holder.binding.choose
        } else if (address.state == "default") {
            holder.binding.defaultText.visibility = View.VISIBLE
            holder.binding.choose.isChecked = false
        } else {
            holder.binding.choose.isChecked = false
            holder.binding.defaultText.visibility = View.INVISIBLE
        }

        holder.binding.receiverName.text = address.receiverName
        holder.binding.receiverPhoneNumber.text = address.receiverPhoneNumber
        holder.binding.detailAddress.text = address.detailAddress

        holder.binding.choose.setOnClickListener {
            if (holder.binding.choose.isChecked) {
                checkedRadioButton?.isChecked = false
                addressAdapterListener?.onCheckedChanged(address)
                checkedRadioButton = holder.binding.choose
            }
        }

        holder.binding.update.setOnClickListener {
            GlobalConfig.updateAddressId = address.addressId
            val intent = Intent(mContext, UpdateAddAddressActivity::class.java).apply {
                putExtra("mode", "update")
                putExtra("userId", userId)
            }
            (mContext as Activity).startActivityForResult(intent, UPDATE_ADDRESS_REQUEST_CODE)
        }

        holder.itemView.setOnLongClickListener {

            val dialog = CustomAlertDialog(mContext, "Delete this address?")


            CustomAlertDialog.binding.btnYes.setOnClickListener {
                if (address.state == "default") {

                    FailToast(mContext, "You cannot delete the default address!").showToast()
                    CustomAlertDialog.alertDialog.dismiss()
                } else {

                    FirebaseDatabase.getInstance().getReference()
                        .child("Address")
                        .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                        .child(address.addressId!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                SuccessfulToast(mContext, "Delete address successfully!").showToast()
                                CustomAlertDialog.alertDialog.dismiss()
                                addressAdapterListener?.onDeleteAddress()
                            }
                        }
                }
            }


            CustomAlertDialog.binding.btnNo.setOnClickListener {
                CustomAlertDialog.alertDialog.dismiss()
            }


            CustomAlertDialog.showAlertDialog()

            true
        }
    }

    override fun getItemCount(): Int = mAddresses.size

    fun setAddressAdapterListener(listener: IAddressAdapterListener) {
        addressAdapterListener = listener
    }

    class ViewHolder(val binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root)
}





