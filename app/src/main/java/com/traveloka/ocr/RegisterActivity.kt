package com.traveloka.ocr

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityMainBinding
import com.traveloka.ocr.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnRegis.setOnClickListener {
            val name = binding.nameRegis.text.toString()
            val email = binding.emailRegis.text.toString()
            val password = binding.passRegis.text.toString()

//            if(name.length>0 && email.length>0 && password.length>0){
            regis(name, email, password)
//            }
//            else{
//                Toast.makeText(
//                    baseContext, "Regis failed.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        }

        binding.textView3.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun regis(name: String, email: String, password: String) {
        Log.i(TAG, "value of email" + email)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(
                        baseContext, "User Created.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                                reload()
                            } else {
                                Toast.makeText(
                                    baseContext, "register failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload();
        }
    }

    private fun reload() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))
    }
}