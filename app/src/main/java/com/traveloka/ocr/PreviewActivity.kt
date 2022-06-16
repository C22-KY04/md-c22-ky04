package com.traveloka.ocr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Preview"

        setOcrPreviewData()
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

    private fun setOcrPreviewData() {
        val image = intent.getStringExtra(IMAGE)
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

    private fun signOut() {
        Firebase.auth.signOut()
        //for facebook
        LoginManager.getInstance().logOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
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
    }
}