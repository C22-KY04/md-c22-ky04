package com.traveloka.ocr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.traveloka.ocr.databinding.ActivityVerifiedBinding

class VerifiedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifiedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifiedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
    }
}