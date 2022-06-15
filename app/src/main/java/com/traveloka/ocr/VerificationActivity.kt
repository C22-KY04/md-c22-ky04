package com.traveloka.ocr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.facebook.login.LoginManager
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

        title = getString(R.string.verify_ktp)
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
                //
            }

            R.id.menu_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton("Yes"){_, _ -> signOut()
                        Toast.makeText(applicationContext, "Account logged out", Toast.LENGTH_LONG).show()}
                    .setNegativeButton("No"){_,_->}
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
                    "Permission is not granted.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        //for facebook
        LoginManager.getInstance().logOut()
        //for google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
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
                    Toast.makeText(this@VerificationActivity, "onResponse = $response", Toast.LENGTH_LONG).show()
//                    Toast.makeText(this@VerificationActivity, "Token = $idToken", Toast.LENGTH_LONG).show()
                    val responseBody = response.body()
                    Toast.makeText(this@VerificationActivity, "Data = $responseBody", Toast.LENGTH_LONG).show()
                    if(response.isSuccessful && responseBody?.status == "OK") {
                        Toast.makeText(this@VerificationActivity, "Data = $responseBody", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@VerificationActivity, "Fail 1", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<OcrUploadResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@VerificationActivity, "Fail 2", Toast.LENGTH_LONG).show()
                }

            })
        } else {
            Toast.makeText(this@VerificationActivity, "Please take picture of your ID Card first", Toast.LENGTH_SHORT).show()
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