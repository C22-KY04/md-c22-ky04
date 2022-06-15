package com.traveloka.ocr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.traveloka.ocr.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.detail)

        tampilData()
    }

    private fun tampilData(){
        val ktp = intent.getParcelableExtra<DataItem>("EXTRA_DETAIL") as DataItem
        Glide.with(applicationContext)
            .load(ktp.attachment)
            .into(binding.imageView)
        binding.phNama.text = ktp.name
        binding.phNik.text = ktp.idNumber
        binding.phTtl.text = ktp.placeDateOfBirth
        binding.phGender.text = ktp.gender
        binding.phDarah.text = ktp.bloodType
        binding.phAlamat.text = ktp.address
        binding.phAgama.text = ktp.religion
        binding.phJob.text = ktp.occupation
        binding.phKwn.text = ktp.nationality
    }

}