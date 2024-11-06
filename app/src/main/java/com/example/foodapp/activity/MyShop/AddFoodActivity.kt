package com.example.foodapp.activity.MyShop

import android.Manifest
import android.app.Activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.foodapp.Interface.APIService
import com.example.foodapp.R
import com.example.foodapp.RetrofitClient

import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityAddFoodBinding
import com.example.foodapp.dialog.UploadDialog
import com.example.foodapp.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddFoodActivity : AppCompatActivity() {
    private var binding: ActivityAddFoodBinding? = null
    private val TAG = "Add Food"
    private var position = 0
    private val PERMISSION_REQUEST_CODE = 10001
    private var uploadDialog: UploadDialog? = null
    private var uri1: Uri? = null
    private var uri2: Uri? = null
    private var uri3: Uri? = null
    private var uri4: Uri? = null
    private var img1 = ""
    private var img2 = ""
    private var img3 = ""
    private var img4 = ""

    //Biến old để lưu lại giá trị hình cũ cần phải xóa trước khi cập nhật lại
    private var imgOld1 = ""
    private var imgOld2 = ""
    private var imgOld3 = ""
    private var imgOld4 = ""
    private var productUpdate: Product? = null
    private var checkUpdate = false
    private var userId: String? = null
    private lateinit var apiService: APIService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")
        //Nhận intent từ edit--------------
        val intentUpdate = intent
        //userId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = "1"
        if (intentUpdate != null && intentUpdate.hasExtra("Product updating")) {
            productUpdate = intentUpdate.getSerializableExtra("Product updating") as Product?
            checkUpdate = true
            binding!!.lnAddFood.btnAddProduct.text = "Update"
            binding!!.lnAddFood.edtNameOfProduct.setText(productUpdate!!.productName)
            binding!!.lnAddFood.edtAmount.setText(productUpdate!!.remainAmount.toString() + "")
            binding!!.lnAddFood.edtDescp.setText(productUpdate!!.description)
            binding!!.lnAddFood.edtPrice.setText(productUpdate!!.productPrice.toString() + "")
            if (productUpdate!!.productType == "Drink") {
                binding!!.lnAddFood.rbDrink.isChecked = true
            } else {
                binding!!.lnAddFood.rbFood.isChecked = true
            }
            imgOld1 = productUpdate!!.productImage1 ?: ""
            imgOld2 = productUpdate!!.productImage2 ?: ""
            imgOld3 = productUpdate!!.productImage3 ?: ""
            imgOld4 = productUpdate!!.productImage4 ?: ""
            if (imgOld1.isNotEmpty()) {
                binding!!.layout1.visibility = View.GONE
                Glide.with(this)
                    .asBitmap()
                    .load(imgOld1)
                    .placeholder(R.drawable.background_loading_layout)
                    .into(binding!!.imgProduct1) // Directly use the ImageView
            }
            if (!imgOld2.isEmpty()) {
                binding!!.layout2.visibility = View.GONE
                Glide.with(this)
                    .asBitmap()
                    .load(imgOld2)
                    .placeholder(R.drawable.background_loading_layout)
                    .into(binding!!.imgProduct2) // Directly use the ImageView
            }
            if (!imgOld3.isEmpty()) {
                binding!!.layout3.visibility = View.GONE
                Glide.with(this)
                    .asBitmap()
                    .load(imgOld3)
                    .placeholder(R.drawable.background_loading_layout)
                    .into(binding!!.imgProduct4) // Directly use the ImageView
            }
            if (!imgOld4.isEmpty()) {
                binding!!.layout4.visibility = View.GONE
                Glide.with(this)
                    .asBitmap()
                    .load(imgOld4)
                    .placeholder(R.drawable.background_loading_layout)
                    .into(binding!!.imgProduct4) // Directly use the ImageView
            }
        }
        //---------------------------------
        position = -1
        binding!!.addImage1.setOnClickListener {
            position = 1
            //checkRuntimePermission();
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            pickImageLauncher.launch(intent)
        }
        binding!!.addImage2.setOnClickListener {
            position = 2
            //checkRuntimePermission();
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            pickImageLauncher.launch(intent)
        }
        binding!!.addImage3.setOnClickListener {
            position = 3
            //checkRuntimePermission();
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            pickImageLauncher.launch(intent)
        }
        binding!!.addImage4.setOnClickListener {
            position = 4
            //checkRuntimePermission();
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            pickImageLauncher.launch(intent)
        }
        binding!!.lnAddFood.btnAddProduct.setOnClickListener {
            if (checkLoi()) {
                uploadDialog = UploadDialog(this@AddFoodActivity)
                uploadDialog!!.show()
                uploadImage(FIRST_IMAGE)
            }
        }
        binding!!.imgBack.setOnClickListener { finish() }
    }

    private fun deleteOldImage(position: Int) {
        val imageURL = StringBuilder()
        handleImagePosition(imageURL, position)
        if (!imageURL.toString().isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageURL.toString()).delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (position == FOURTH_IMAGE) {
                            uploadDialog!!.dismiss()
                            SuccessfulToast(
                                this@AddFoodActivity,
                                "Delete old image successfully!"
                            ).showToast()
                            finish()
                        } else {
                            deleteOldImage(position + 1)
                        }
                    } else {
                        FailToast(this@AddFoodActivity, "Error delete image: $imageURL").showToast()
                    }
                }
        } else {
            if (position != FOURTH_IMAGE) {
                deleteOldImage(position + 1)
            } else {
                uploadDialog!!.dismiss()
                SuccessfulToast(this@AddFoodActivity, "Delete old image successfully!").showToast()
                finish()
            }
        }
    }

    private fun handleImagePosition(imageURL: StringBuilder, position: Int) {
        if (position == FIRST_IMAGE) {
            if (img1 != imgOld1) {
                imageURL.append(imgOld1)
            }
        } else if (position == SECOND_IMAGE) {
            if (img2 != imgOld2) {
                imageURL.append(imgOld2)
            }
        } else if (position == THIRD_IMAGE) {
            if (img3 != imgOld3) {
                imageURL.append(imgOld3)
            }
        } else {
            if (img4 != imgOld4) {
                imageURL.append(imgOld4)
            }
        }
    }

    fun pickImg() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("image/*")
                    pickImageLauncher.launch(intent)
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                    FailToast(this@AddFoodActivity, "Permission denied!").showToast()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                    FailToast(this@AddFoodActivity, "Permission denied!").showToast()
                }
            }).check()
    }

    fun checkLoi(): Boolean {
        return try {
            val name = binding!!.lnAddFood.edtNameOfProduct.text.toString()
            val price = (binding!!.lnAddFood.edtPrice.text.toString() + ".0").toDouble()
            val amount = binding!!.lnAddFood.edtAmount.text.toString().toInt()
            val description = binding!!.lnAddFood.edtDescp.text.toString()
            if (!checkUpdate) {
                if (img1.isEmpty() || img2.isEmpty() || img3.isEmpty() || img4.isEmpty()) {
                    createDialog("Điền đủ 4 hình").create().show()
                    return false
                } else if (name.isEmpty() || name.length < 8) {
                    createDialog("Tên ít nhất phải từ 8 kí tự và không được bỏ trống").create()
                        .show()
                    return false
                } else if (price < 5000.0) {
                    createDialog("Giá phải từ 5000 trở lên").create().show()
                    return false
                } else if (amount <= 0) {
                    createDialog("Số lượng phải lớn hơn 0").create().show()
                    return false
                } else if (description.isEmpty() || description.length < 10) {
                    createDialog("Phần mô tả phải từ 10 ký tự trở lên và không được bỏ trống").create()
                        .show()
                    return false
                }
            } else if (name.isEmpty() || name.length < 8) {
                createDialog("Tên ít nhất phải từ 8 kí tự và không được bỏ trống").create().show()
                return false
            } else if (price < 5000.0) {
                createDialog("Giá phải từ 5000 trở lên").create().show()
                return false
            } else if (amount <= 0) {
                createDialog("Số lượng phải lớn hơn 0").create().show()
                return false
            } else if (description.isEmpty() || description.length < 10) {
                createDialog("Phần mô tả phải từ 10 ký tự trở lên và không được bỏ trống").create()
                    .show()
                return false
            }
            true
        } catch (e: Exception) {
            createDialog("Price và Amount chỉ được nhập ký tự là số và không được bỏ trống").create()
                .show()
            false
        }
    }

    fun createDialog(content: String?): AlertDialog.Builder {
        val builder = AlertDialog.Builder(this@AddFoodActivity)
        builder.setTitle("Thông báo")
        builder.setMessage(content)
        builder.setPositiveButton(
            "OK"
        ) { dialogInterface, _ -> dialogInterface.cancel() }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, _ -> dialogInterface.cancel() }
        builder.setIcon(R.drawable.icon_dialog_alert_addfood)
        return builder
    }

    fun uploadProduct(tmp: Product) {
        apiService = RetrofitClient.retrofit!!.create(APIService::class.java)
        if (checkUpdate) {
            tmp.productId = productUpdate!!.productId
            apiService.updateProduct(tmp).enqueue(object : Callback<Product?> {
                override fun onResponse(call: Call<Product?>, response: Response<Product?>) {
                    if (response.isSuccessful) {
                        uploadDialog!!.dismiss()
                        val reIntent = Intent()
                        reIntent.putExtra("productId", tmp.productId)
                        reIntent.putExtra("productName", tmp.productName)
                        reIntent.putExtra("productPrice", tmp.productPrice)
                        reIntent.putExtra("productImage1", tmp.productImage1)
                        reIntent.putExtra("productImage2", tmp.productImage2)
                        reIntent.putExtra("productImage3", tmp.productImage3)
                        reIntent.putExtra("productImage4", tmp.productImage4)
                        reIntent.putExtra("ratingStar", tmp.ratingStar)
                        reIntent.putExtra("productDescription", tmp.description)
                        reIntent.putExtra("publisherId", tmp.publisherId)
                        reIntent.putExtra("sold", tmp.sold)
                        reIntent.putExtra("productType", tmp.productType)
                        reIntent.putExtra("remainAmount", tmp.remainAmount)
                        reIntent.putExtra("ratingAmount", tmp.ratingAmount)
                        reIntent.putExtra("state", tmp.state)
                        reIntent.putExtra("userId", userId)
                        reIntent.putExtra("userName", tmp)
                        setResult(10, reIntent)
                        finish()
                        SuccessfulToast(
                            this@AddFoodActivity,
                            "Update product successfully"
                        ).showToast()
                        Log.d("Update", "Cập nhật sản phẩm thành công")
                    } else {
                        uploadDialog!!.dismiss()
                        FailToast(this@AddFoodActivity, "Some error occurred!").showToast()
                        Log.e("Update", "Lỗi cập nhật sản phẩm")
                    }
                }

                override fun onFailure(call: Call<Product?>, t: Throwable) {
                    Log.e("Update1", "Lỗi cập nhật sản phẩm")
                }
            })
        } else {
            apiService.addProduct(tmp).enqueue(object : Callback<Product?> {
                override fun onResponse(call: Call<Product?>, response: Response<Product?>) {
                    if (response.isSuccessful) {
                        uploadDialog!!.dismiss()
                        finish()
                        SuccessfulToast(
                            this@AddFoodActivity,
                            "Add product successfully!"
                        ).showToast()
                        Log.d(TAG, "Thêm sản phẩm thành công")
                    } else {
                        uploadDialog!!.dismiss()
                        FailToast(this@AddFoodActivity, "Some error occurred!").showToast()
                        Log.e(TAG, "Lỗi thêm sản phẩm")
                    }
                }

                override fun onFailure(call: Call<Product?>, t: Throwable) {
                    Log.e(TAG, t.message!!)
                }
            })
        }
    }

    fun uploadImage(position: Int) {
        var uri = uri1
        if (position == SECOND_IMAGE) {
            uri = uri2
        }
        if (position == THIRD_IMAGE) {
            uri = uri3
        }
        if (position == FOURTH_IMAGE) {
            uri = uri4
        }
        if (uri != null) {
            val storage = FirebaseStorage.getInstance()
            val reference = storage.reference.child("Product Image")
                .child(System.currentTimeMillis().toString() + "")
            reference.putFile(uri).addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { uri ->
                    if (position == FOURTH_IMAGE) {
                        val img4 = uri.toString()
                        val name = binding!!.lnAddFood.edtNameOfProduct.text.toString()
                        val price = binding!!.lnAddFood.edtPrice.text.toString()
                        val amount = binding!!.lnAddFood.edtAmount.text.toString()
                        val description = binding!!.lnAddFood.edtDescp.text.toString()
                        val tmp = Product(
                            name,
                            img1,
                            img2,
                            img3,
                            img4,
                            Integer.valueOf(price),
                            if (binding!!.lnAddFood.rbFood.isChecked) "Food" else "Drink",
                            Integer.valueOf(amount),
                            0,
                            description,
                            0.0,
                            0,
                            userId
                        )
                        uploadProduct(tmp)
                    } else {
                        if (position == FIRST_IMAGE) {
                            img1 = uri.toString()
                        } else if (position == SECOND_IMAGE) {
                            img2 = uri.toString()
                        } else {
                            img3 = uri.toString()
                        }
                        uploadImage(position + 1)
                    }
                }
            }
        } else {
            if (position != FOURTH_IMAGE) {
                if (position == FIRST_IMAGE) img1 =
                    imgOld1 else if (position == SECOND_IMAGE) img2 =
                    imgOld2 else if (position == THIRD_IMAGE) img3 = imgOld3
                uploadImage(position + 1)
            } else {
                img4 = imgOld4
                val name = binding!!.lnAddFood.edtNameOfProduct.text.toString()
                val price = binding!!.lnAddFood.edtPrice.text.toString()
                val amount = binding!!.lnAddFood.edtAmount.text.toString()
                val description = binding!!.lnAddFood.edtDescp.text.toString()
                val tmp = Product(
                    name,
                    img1,
                    img2,
                    img3,
                    img4,
                    Integer.valueOf(price),
                    if (binding!!.lnAddFood.rbFood.isChecked) "Food" else "Drink",
                    Integer.valueOf(amount),
                    0,
                    description,
                    0.0,
                    0,
                    userId
                )
                uploadProduct(tmp)
            }
        }
    }

    var pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentData = result.data
                if (intentData != null) {
                    when (position) {
                        1 -> {
                            uri1 = intentData.data
                            img1 = uri1.toString()
                            binding?.layout1?.visibility = View.GONE
                            binding?.imgProduct1?.setImageURI(uri1)
                        }
                        2 -> {
                            uri2 = intentData.data
                            img2 = uri2.toString()
                            binding?.layout2?.visibility = View.GONE
                            binding?.imgProduct2?.setImageURI(uri2)
                        }
                        3 -> {
                            uri3 = intentData.data
                            img3 = uri3.toString()
                            binding?.layout3?.visibility = View.GONE
                            binding?.imgProduct3?.setImageURI(uri3)
                        }
                        4 -> {
                            uri4 = intentData.data
                            img4 = uri4.toString()
                            binding?.layout4?.visibility = View.GONE
                            binding?.imgProduct4?.setImageURI(uri4)
                        }
                    }
                }
            }
        }

    private fun checkRuntimePermission() {
        if (isPermissionGranted()) {
            pickImg()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            buildAlertPermissionDialog().create().show()
        } else {
            requestRuntimePermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImg()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                buildAlertDeniedPermissionDialog().create().show()
            } else {
                checkRuntimePermission()
            }
        }
    }

    private fun buildAlertPermissionDialog(): AlertDialog.Builder {
        val builderDialog = AlertDialog.Builder(this)
        builderDialog.setTitle("Notice")
            .setMessage("Bạn cần cấp quyền để thực hiện tính năng này")
            .setPositiveButton(
                "Ok"
            ) { dialogInterface, _ ->
                requestRuntimePermission()
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
        return builderDialog
    }

    private fun buildAlertDeniedPermissionDialog(): AlertDialog.Builder {
        val builderDialog = AlertDialog.Builder(this)
        builderDialog.setTitle("Notice")
            .setMessage("Bạn cần vào cài đặt để cài đặt cho tính năng này")
            .setPositiveButton(
                "Setting"
            ) { dialogInterface, _ ->
                startActivity(createIntentToAppSetting())
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
        return builderDialog
    }

    private fun createIntentToAppSetting(): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        return intent
    }

    private fun requestRuntimePermission() {
        ActivityCompat.requestPermissions(
            this@AddFoodActivity, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), PERMISSION_REQUEST_CODE
        )
    }

    private fun isPermissionGranted(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val FIRST_IMAGE = 1
        private const val SECOND_IMAGE = 2
        private const val THIRD_IMAGE = 3
        private const val FOURTH_IMAGE = 4
    }
}