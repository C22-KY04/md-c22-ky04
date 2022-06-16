package com.traveloka.ocr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.detail)

        tampilData()
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
}