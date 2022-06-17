package com.traveloka.ocr

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var defaultList = listOf<DataItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        user!!.getIdToken(true).addOnSuccessListener { result ->
            idToken = "Bearer " + result.token.toString()
            displayKtpList(idToken)
            Log.d("Main Activity", "onCreate: $idToken")
        }

        val layoutManager = LinearLayoutManager(this)
        binding.rvUserKtp.layoutManager = layoutManager

    }

    private fun displayKtpList(idToken: String) {
        Log.d(TAG, "displayKtpList: $idToken")
        showLoading(true)

        val client = ApiConfig.getIdCardsApiService().getIdCardList(idToken, name)
        client.enqueue(object : Callback<KtpResponse> {
            override fun onResponse(call: Call<KtpResponse>, response: Response<KtpResponse>) {
                Log.d(TAG, "onResponse: $response")
                showLoading(false)
                if(response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "onResponse: $responseBody")
                    if(responseBody != null) {
                        setKtpData(responseBody.data)
                        if(defaultList.isEmpty()){
                            defaultList = responseBody.data
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: ${response.message()}")
                    Toast.makeText(this@MainActivity, getString(R.string.data_not_found), Toast.LENGTH_LONG).show()
                    setKtpData(emptyList())
                }
            }

            override fun onFailure(call: Call<KtpResponse>, t: Throwable) {
                showLoading(false)
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun setKtpData(ktpResponse: List<DataItem>) {
        val listOfKtp = ArrayList<DataItem>()
        for(item in ktpResponse) {
            val ktp = DataItem(
                item.idNumber,
                item.address,
                item.occupation,
                item.gender,
                item.expiryDate,
                item.religion,
                item.maritalStatus,
                item.province,
                item.nationality,
                item.attachment,
                item.subdistrict,
                item.district,
                item.bloodType,
                item.name,
                item.placeDateOfBirth,
                item.neighborhood,
                item.village
            )
            listOfKtp.add(ktp)
        }

        val adapter = KtpListAdapter(listOfKtp)
        binding.rvUserKtp.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenu = menu.findItem(R.id.menu_search).actionView as SearchView

        searchMenu.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchMenu.queryHint = resources.getString(R.string.search_hint)
        searchMenu.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query == null){
                    setKtpData(defaultList)
                    Toast.makeText(this@MainActivity, getString(R.string.data_not_found) + defaultList.toString(), Toast.LENGTH_LONG).show()
                }else{
                    name = query.toString()
                    displayKtpList(idToken)
                    searchMenu.clearFocus()
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrEmpty()){
                    setKtpData(defaultList)
                }
                return true
            }
        })

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

    private fun signOut() {
        Firebase.auth.signOut()
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

    private fun showLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
    
    companion object {
        const val TAG = "MainActivity"
        var name = ""
        var idToken = ""
    }
}