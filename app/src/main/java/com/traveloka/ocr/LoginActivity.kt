package com.traveloka.ocr

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        showLoading(false)
        supportActionBar?.setTitle(R.string.login)
        emailValidate()
        passwordValidate()

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(baseContext, getString(R.string.form_not_null), Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonGoogle.setOnClickListener {
            googleSignIn()
        }

        binding.buttonFb.setOnClickListener{
            binding.buttonFb2.callOnClick()
        }

        binding.toRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        //google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //facebook
        FacebookSdk.sdkInitialize(getApplicationContext())
        AppEventsLogger.activateApp(this)
        callbackManager = CallbackManager.Factory.create()
        binding.buttonFb2.setReadPermissions("email", "public_profile")
        binding.buttonFb2.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
                showLoading(true)
            }
            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }
            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
    }

    //login with email
    private fun login(email: String, password: String) {
        showLoading(true)
        val userUid = auth.uid
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    Toast.makeText(baseContext, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    reload()
                    finish()
                } else {
                    showLoading(false)
                    Toast.makeText(this, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    //login with google
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //login with facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }
                reload()
                showLoading(false)
            }
    }

    //onActivityResult for google and facebook
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
                showLoading(true)
            } catch (e: ApiException) {
                Log.w(TAG, "Google Sign In Failed", e)
            }
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data)        }
    }

    //login with google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }
                showLoading(false)
                reload()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun emailValidate() {
        binding.emailLogin.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailLogin.error = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val emailValue = binding.emailLogin.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            return "Invalid Email Address"
        }
        return null
    }

    private fun passwordValidate() {
        binding.passLogin.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passLogin.error = validPass()
            }
        }
    }

    private fun validPass(): String? {
        val passValue = binding.passLogin.text.toString()
        if (passValue.length < 6) {
            return "Minimum 6 Character Password"
        }
        return null
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    companion object {
        const val RC_SIGN_IN = 1001
    }
}