package com.traveloka.ocr

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityVerificationBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.verify_id_card)
        showLoading(false)

        val user = FirebaseAuth.getInstance().currentUser
        user!!.getIdToken(true).addOnSuccessListener { result ->
            idToken = "Bearer " + result.token.toString()
            Log.d(TAG, "onCreate: $idToken")
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnCamera.setOnClickListener { startCameraX() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnProcess.setOnClickListener {
            processingOcr()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val searchMenu = menu.findItem(R.id.menu_search)
        searchMenu.isVisible = false

        val verifyMenu = menu.findItem(R.id.menu_verify)
        verifyMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_verify -> {
                val intent = Intent(this, VerificationActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_languages -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }

            R.id.menu_logout -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.want_to_logout))
                    .setPositiveButton(getString(R.string.yes)){ _, _ -> signOut()
                        Toast.makeText(applicationContext, getString(R.string.success_logout), Toast.LENGTH_LONG).show()}
                    .setNegativeButton(getString(R.string.no)){ _, _->}
                    .show()
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        //for google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            getFile = myFile

            val bitmap = BitmapFactory.decodeFile(myFile.path)
            val ei = ExifInterface(myFile.path)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val rotatedBitmap: Bitmap = when(orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f, true)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f, true)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f, true)
                else -> bitmap
            }
            binding.imgCard.setImageBitmap(rotatedBitmap)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@VerificationActivity)
            getFile = myFile

            binding.imgCard.setImageURI(selectedImg)
        }
    }

    private fun processingOcr() {
        if(getFile != null) {
            showLoading(true)
            val file = reduceFileImage(getFile as File)

            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestImageFile
            )

            val client = ApiConfig.getOcrApiService().processingOcr(idToken, imageMultipart)
            client.enqueue(object: Callback<OcrUploadResponse> {
                override fun onResponse(
                    call: Call<OcrUploadResponse>,
                    response: Response<OcrUploadResponse>
                ) {
                    showLoading(false)
                    Log.d(TAG, "onResponse: $response")
                    val responseBody = response.body()
                    if(response.isSuccessful && responseBody?.status == "OK") {
                        Toast.makeText(this@VerificationActivity, getString(R.string.ocr_success), Toast.LENGTH_LONG).show()

                        val intent = Intent(this@VerificationActivity, PreviewActivity::class.java)
                        intent.putExtra(PreviewActivity.IMAGE, responseBody.data.attachment)
                        intent.putExtra(PreviewActivity.NAME, responseBody.data.name)
                        intent.putExtra(PreviewActivity.ID_NUMBER, responseBody.data.idNumber)
                        intent.putExtra(PreviewActivity.PLACE_AND_DOB, responseBody.data.placeDateOfBirth)
                        intent.putExtra(PreviewActivity.GENDER, responseBody.data.gender)
                        intent.putExtra(PreviewActivity.BLOOD_TYPE, responseBody.data.bloodType)
                        intent.putExtra(PreviewActivity.ADDRESS, responseBody.data.address)
                        intent.putExtra(PreviewActivity.NEIGHBORHOOD, responseBody.data.neighborhood)
                        intent.putExtra(PreviewActivity.VILLAGE, responseBody.data.village)
                        intent.putExtra(PreviewActivity.SUB_DISTRICT, responseBody.data.subdistrict)
                        intent.putExtra(PreviewActivity.DISTRICT, responseBody.data.district)
                        intent.putExtra(PreviewActivity.PROVINCE, responseBody.data.province)
                        intent.putExtra(PreviewActivity.RELIGION, responseBody.data.religion)
                        intent.putExtra(PreviewActivity.MARITAL_STATUS, responseBody.data.maritalStatus)
                        intent.putExtra(PreviewActivity.OCCUPATION, responseBody.data.occupation)
                        intent.putExtra(PreviewActivity.NATIONALITY, responseBody.data.nationality)
                        intent.putExtra(PreviewActivity.EXPIRED_DATE, responseBody.data.expiryDate)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@VerificationActivity, getString(R.string.ocr_fail), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<OcrUploadResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@VerificationActivity, getString(R.string.ocr_fail), Toast.LENGTH_LONG).show()
                }

            })
        } else {
            Toast.makeText(this@VerificationActivity, getString(R.string.take_picture_first), Toast.LENGTH_LONG).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "VerificationActivity"
        var idToken = ""
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}