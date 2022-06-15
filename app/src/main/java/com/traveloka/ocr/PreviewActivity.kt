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
        val name = "Aimar Wibowo"
        val nik = "3376011005010002"
        val placeAndDob = "Tegal, 10 Mei 2001"
        val gender = "Laki - Laki"
        val bloodType = "B"
        val address = "Jalan Kapten Piere Tendean No. 10"
        val neighborhood = "008 / 009"
        val village = "Tegal Sari"
        val subDistrict = "Tegal Barat"
        val district = "Kota Tegal"
        val province = "Jawa Tengah"
        val religion = "Katholik"
        val maritalStatus = "Belum Kawin"
        val occupation = "Pelajar / Mahasiswa"
        val nationality = "WNI"
        val expiredDate = "Seumur Hidup"

        Glide.with(this)
            .load("https://about.lovia.life/wp-content/uploads/2020/05/ktp-1024x660.jpg")
            .into(binding.imgPhoto)
        binding.etName.setText(name)
        binding.etNin.setText(nik)
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
}