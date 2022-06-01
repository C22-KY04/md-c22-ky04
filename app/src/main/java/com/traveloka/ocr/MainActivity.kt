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

        var idToken = ""
        val user = FirebaseAuth.getInstance().currentUser
        user!!.getIdToken(true).addOnSuccessListener { result ->
            idToken = result.token.toString()
            //Do whatever
            displayKtpList(idToken)
            Log.d("Main Activity", "onCreate: $idToken")
        }

        val layoutManager = LinearLayoutManager(this)
        binding.rvUserKtp.layoutManager = layoutManager

    }

    private fun displayKtpList(idToken: String) {
        Log.d(TAG, "displayKtpList: $idToken")
        showLoading(true)

        val client = ApiConfig.getApiService().getKtp(idToken)
        client.enqueue(object : Callback<List<KtpResponse>> {
            override fun onResponse(call: Call<List<KtpResponse>>, response: Response<List<KtpResponse>>) {
                Log.d(TAG, "onResponse: $response")
                showLoading(false)
                if(response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "onResponse: $responseBody")
                    if(responseBody != null) {
                        setKtpData(responseBody)
                    }
                } else {
                    Log.e(TAG, "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<KtpResponse>>, t: Throwable) {
                showLoading(false)
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun setKtpData(ktpResponse: List<KtpResponse>) {
        val listOfKtp = ArrayList<Ktp>()
        for(item in ktpResponse) {
            val ktp = Ktp(
                item.idNumber,
                item.fullName
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
                Toast.makeText(this@MainActivity, query, Toast.LENGTH_SHORT).show()
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
    }
}