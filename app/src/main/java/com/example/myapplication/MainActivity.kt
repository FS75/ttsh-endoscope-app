package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.nfc.NfcAdapter
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utilities.NFCUtils

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    //NFC stuff
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set up NFC Adapter
        NFCUtils.setNFCAdapter(nfcAdapter, this)

        //NFC Process Start
        NFCUtils.onNFCStart(this, javaClass)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //send data from activity to fragment here
        val fragment = SampleScopeFragment()
        val email = intent.getStringExtra("email")
        val pw = intent.getStringExtra("pw")
        //Bundle for transferring data to activity
        val bundle = Bundle()
        bundle.putString("email", email)
        bundle.putString("pw", pw)
        fragment.arguments = bundle
        loadFragment(fragment)
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.schedule -> {
                    loadFragment(ScheduleFragment())
                    true
                }
                R.id.sample_scope -> {
                    loadFragment(fragment)
                    true
                }
                R.id.status -> {
                    loadFragment(StatusFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(fragment.id.toString())
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                // Show confirmation dialog before logging out
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirm Logout")
                builder.setMessage("Are you sure you want to log out?")
                builder.setPositiveButton("Yes") { _, _ ->
                    // Clear user session and go back to login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                builder.setNegativeButton("No", null)
                builder.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val nfcData = NFCUtils.onNFCDetected(intent, applicationContext)
            if (nfcData!=null) {
                intent.putExtra("nfcData", nfcData)
                setIntent(intent)
            }else{
                Toast.makeText(
                    this,
                    R.string.NFC_not_supported,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        NFCUtils.onNFCPause(this)
        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        NFCUtils.onNFCResume(this)
    }
}