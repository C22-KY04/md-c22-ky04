package com.traveloka.ocr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityPreviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Preview"
        showLoading(false)

        val user = FirebaseAuth.getInstance().currentUser
        user!!.getIdToken(true).addOnSuccessListener { result ->
            idToken = "Bearer " + result.token.toString()
            Log.d(TAG, "onCreate: $idToken")
        }

        setOcrPreviewData()

        binding.btnTakePicture.setOnClickListener {
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSave.setOnClickListener {
            uploadIdCard()
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

    private fun setOcrPreviewData() {
        val image = intent.getStringExtra(IMAGE)
        attachment = image.toString()

        val name = intent.getStringExtra(NAME)
        val idNumber = intent.getStringExtra(ID_NUMBER)
        val placeAndDob = intent.getStringExtra(PLACE_AND_DOB)
        val gender = intent.getStringExtra(GENDER)
        val bloodType = intent.getStringExtra(BLOOD_TYPE)
        val address = intent.getStringExtra(ADDRESS)
        val neighborhood = intent.getStringExtra(NEIGHBORHOOD)
        val village = intent.getStringExtra(VILLAGE)
        val subDistrict = intent.getStringExtra(SUB_DISTRICT)
        val district = intent.getStringExtra(DISTRICT)
        val province = intent.getStringExtra(PROVINCE)
        val religion = intent.getStringExtra(RELIGION)
        val maritalStatus = intent.getStringExtra(MARITAL_STATUS)
        val occupation = intent.getStringExtra(OCCUPATION)
        val nationality = intent.getStringExtra(NATIONALITY)
        val expiredDate = intent.getStringExtra(EXPIRED_DATE)

        Glide.with(this)
            .load(image)
            .into(binding.imgPhoto)
        binding.etName.setText(name)
        binding.etNin.setText(idNumber)
        binding.etPlaceAndDob.setText(placeAndDob)
        binding.etGender.setText(gender)
        binding.etBloodType.setText(bloodType)
        binding.etAddress.setText(address)
        binding.etNeighborhood.setText(neighborhood)
        binding.etVillage.setText(village)
        binding.etSubDistrict.setText(subDistrict)
        binding.etDistrict.setText(district)
        binding.etProvince.setText(province)
        binding.etReligion.setText(religion)
        binding.etMaritalStatus.setText(maritalStatus)
        binding.etOccupation.setText(occupation)
        binding.etNationality.setText(nationality)
        binding.etExpiredDate.setText(expiredDate)
    }

    private fun uploadIdCard() {
        showLoading(true)

        val image = attachment
        val name = binding.etName.text.toString()
        val nin = binding.etNin.text.toString()
        val placeAndDob = binding.etPlaceAndDob.text.toString()
        val gender = binding.etGender.text.toString()
        val bloodType = binding.etBloodType.text.toString()
        val address = binding.etAddress.text.toString()
        val neighborhood = binding.etNeighborhood.text.toString()
        val village = binding.etVillage.text.toString()
        val subDistrict = binding.etSubDistrict.text.toString()
        val district = binding.etDistrict.text.toString()
        val province = binding.etProvince.text.toString()
        val religion = binding.etReligion.text.toString()
        val maritalStatus = binding.etMaritalStatus.text.toString()
        val occupation = binding.etOccupation.text.toString()
        val nationality = binding.etNationality.text.toString()
        val expiredDate = binding.etExpiredDate.text.toString()

        val client = ApiConfig
            .getIdCardsApiService()
            .uploadIdCard(
                idToken,
                province,
                district,
                nin,
                name,
                placeAndDob,
                gender,
                bloodType,
                address,
                neighborhood,
                village,
                subDistrict,
                religion,
                maritalStatus,
                occupation,
                nationality,
                expiredDate,
                image
            )

        client.enqueue(object: Callback<IdCardUploadResponse> {
            override fun onResponse(
                call: Call<IdCardUploadResponse>,
                response: Response<IdCardUploadResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                if(response.isSuccessful && responseBody?.status == "Created") {
                    Toast.makeText(this@PreviewActivity, getString(R.string.id_card_success), Toast.LENGTH_LONG).show()
                    val intent = Intent(this@PreviewActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@PreviewActivity, getString(R.string.id_card_fail), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<IdCardUploadResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@PreviewActivity, getString(R.string.id_card_fail), Toast.LENGTH_LONG).show()
            }

        })
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
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "PreviewActivity"

        const val IMAGE = "image"
        const val NAME = "name"
        const val ID_NUMBER = "id_number"
        const val PLACE_AND_DOB = "place_and_dob"
        const val GENDER = "gender"
        const val BLOOD_TYPE = "blood_type"
        const val ADDRESS = "address"
        const val NEIGHBORHOOD = "neighborhood"
        const val VILLAGE = "village"
        const val SUB_DISTRICT = "sub_district"
        const val DISTRICT = "district"
        const val PROVINCE = "province"
        const val RELIGION = "religion"
        const val MARITAL_STATUS = "marital_status"
        const val OCCUPATION = "occupation"
        const val NATIONALITY = "nationality"
        const val EXPIRED_DATE = "expired_date"

        var attachment = "attachment"
        var idToken = ""
    }
}