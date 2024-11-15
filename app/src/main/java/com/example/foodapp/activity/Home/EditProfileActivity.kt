package com.example.foodapp.activity.Home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityEditProfileBinding
import com.example.foodapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var imageUrl: String? = null
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        userId = intent.getStringExtra("userId") ?: ""

        initToolbar()
        initDatePickerDialog()
        initImagePickerActivity()

        getInfo()

        binding.profileImage.setOnClickListener { openImagePicker() }
        binding.changePhoto.setOnClickListener { openImagePicker() }

        binding.userName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                checkProfileChanges()
            }
        })

        binding.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                checkProfileChanges()
            }
        })

        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                checkProfileChanges()
            }
        })

        binding.birthDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                checkProfileChanges()
            }
        })

        binding.datePicker.setOnClickListener { datePickerDialog.show() }

        binding.update.setOnClickListener { updateInfo() }
    }

    private fun checkProfileChanges() {
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.update.isEnabled = !(it.userName == binding.userName.text.toString() &&
                                it.email == binding.email.text.toString() &&
                                it.phoneNumber == binding.phoneNumber.text.toString() &&
                                it.birthDate == binding.birthDate.text.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateInfo() {
        val emailTxt = binding.email.text.toString().trim()
        val phoneNumberTxt = binding.phoneNumber.text.toString().trim()
        val userNameTxt = binding.userName.text.toString().trim()

        if (emailTxt.isEmpty()) {
            FailToast(this, "Email must not be empty!").showToast()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            FailToast(this, "Invalid email!!").showToast()
            return
        }

        if (phoneNumberTxt.isEmpty()) {
            FailToast(this, "Phone number must not be empty!").showToast()
            return
        }

        if (userNameTxt.isEmpty()) {
            FailToast(this, "User name must not be empty!").showToast()
            return
        }

        val map: MutableMap<String, Any?> = hashMapOf(
            "avatarURL" to imageUrl,
            "birthDate" to binding.birthDate.text.toString(),
            "email" to binding.email.text.toString(),
            "phoneNumber" to binding.phoneNumber.text.toString(),
            "userName" to binding.userName.text.toString()
        )
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .updateChildren(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SuccessfulToast(this, "Updated successfully!").showToast()
                } else {
                    FailToast(this, "Failed to update").showToast()
                }
            }

        finish()
    }

    private fun deleteOldImage() {
        imageUrl?.let {
            FirebaseStorage.getInstance().getReferenceFromUrl(it).delete()
        }
    }

    private fun initImagePickerActivity() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let { uploadImage(it) }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        imagePickerLauncher.launch(intent)
    }

    private fun getInfo() {
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        Glide.with(applicationContext)
                            .load(it.avatarURL)
                            .placeholder(R.drawable.default_avatar)
                            .into(binding.profileImage)
                        binding.userName.setText(it.userName)
                        binding.email.setText(it.email)
                        binding.phoneNumber.setText(it.phoneNumber)
                        binding.birthDate.setText(it.birthDate)
                        imageUrl = it.avatarURL
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun uploadImage(imageUri: Uri) {
        val pd = ProgressDialog(this).apply { setMessage("Uploading") }
        pd.show()

        val fileRef = FirebaseStorage.getInstance().getReference("Users")
            .child("${System.currentTimeMillis()}.${getFileExtension(imageUri)}")

        fileRef.putFile(imageUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    deleteOldImage()

                    imageUrl = uri.toString()
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.profile_image)
                        .into(binding.profileImage)

                    pd.dismiss()

                    binding.update.isEnabled = true
                }
            }
        }
    }

    private fun initDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            binding.birthDate.setText(getDMY(dayOfMonth, month + 1, year))
        }

        val style = AlertDialog.BUTTON_NEGATIVE

        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        val dateSplit = it.birthDate!!.split("/")
                        val day = dateSplit[0].toInt()
                        val month = dateSplit[1].toInt()
                        val year = dateSplit[2].toInt()

                        datePickerDialog = DatePickerDialog(this@EditProfileActivity, style, dateSetListener, year, month - 1, day)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Edit Profile"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            if (binding.update.isEnabled) {
                val customAlertDialog = CustomAlertDialog(this, "Save changes?")

                CustomAlertDialog.binding.btnYes.setOnClickListener {
                    CustomAlertDialog.alertDialog.dismiss()
                    updateInfo()
                }

                CustomAlertDialog.binding.btnNo.setOnClickListener {
                    CustomAlertDialog.alertDialog.dismiss()
                    finish()
                }

                CustomAlertDialog.showAlertDialog()
            } else {
                finish()
            }
        }

    }

    private fun getDMY(day: Int, month: Int, year: Int): String {
        return when {
            day in 1..9 && month in 1..9 -> "0$day/0$month/$year"
            day in 1..9 -> "0$day/$month/$year"
            month in 1..9 -> "$day/0$month/$year"
            else -> "$day/$month/$year"
        }
    }

    private fun getFileExtension(uri: Uri): String {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)) ?: ""
    }
}
