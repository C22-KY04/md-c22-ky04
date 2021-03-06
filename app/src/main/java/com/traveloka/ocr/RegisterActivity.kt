package com.traveloka.ocr

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        showLoading(false)
        supportActionBar?.setTitle(R.string.register)
        emailValidate()
        passwordValidate()

        binding.btnRegis.setOnClickListener {
            val name = binding.nameRegis.text.toString()
            val email = binding.emailRegis.text.toString()
            val password = binding.passRegis.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                regis(name, email, password)
            } else {
                Toast.makeText(baseContext, getString(R.string.form_not_null), Toast.LENGTH_SHORT).show()
            }
        }

        binding.toLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    private fun regis(name: String, email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    Toast.makeText(baseContext, getString(R.string.register_success), Toast.LENGTH_SHORT).show()

                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                                startActivity(Intent(applicationContext, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(baseContext, "User profile update failed.", Toast.LENGTH_SHORT).show()
                            }
                        }

                } else {
                    showLoading(false)
                    Toast.makeText(this, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun emailValidate() {
        binding.emailRegis.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailRegis.error = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val emailValue = binding.emailRegis.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            return "Invalid Email Address"
        }
        return null
    }

    private fun passwordValidate() {
        binding.passRegis.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passRegis.error = validPass()
            }
        }
    }

    private fun validPass(): String? {
        val passValue = binding.passRegis.text.toString()
        if (passValue.length < 6) {
            return "Minimum 6 Character Password"
        }
        return null
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

}