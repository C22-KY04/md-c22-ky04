package com.traveloka.ocr

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.traveloka.ocr.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        var idToken = ""
        val user = FirebaseAuth.getInstance().currentUser
        user!!.getIdToken(true).addOnSuccessListener { result ->
            idToken = "Bearer " + result.token.toString()
            displayKtpList(idToken)
            //Do whatever
//            displayKtpList("Bearer $idToken")
            Log.d("Main Activity", "onCreate: $idToken")
        }

        val layoutManager = LinearLayoutManager(this)
        binding.rvUserKtp.layoutManager = layoutManager

    }

    private fun displayKtpList(idToken: String) {
        Log.d(TAG, "displayKtpList: $idToken")
        showLoading(true)

        val client = ApiConfig.getApiService().searchKtp(idToken, name)
        client.enqueue(object : Callback<KtpResponse> {
            override fun onResponse(call: Call<KtpResponse>, response: Response<KtpResponse>) {
                Log.d(TAG, "onResponse: $response")
                showLoading(false)
                if(response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "onResponse: $responseBody")
                    if(responseBody != null) {
                        setKtpData(responseBody.data)
                    }
                } else {
                    Log.e(TAG, "onResponse: ${response.message()}")
                    Toast.makeText(this@MainActivity, "Data Not Found", Toast.LENGTH_SHORT).show()
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
                item.uid,
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
                name = query.toString()
                displayKtpList(idToken)
//                Toast.makeText(this@MainActivity, query, Toast.LENGTH_SHORT).show()
                searchMenu.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
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
                // Test
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

    private fun signOut() {
        Firebase.auth.signOut()
        //for facebook
        LoginManager.getInstance().logOut();
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