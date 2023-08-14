package com.example.myapplication

import android.content.Intent
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.utilities.NFCUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    val TAG = "LoginActivity"

    //NFC stuff
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }

    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Enable data binding for the login layout XML file
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        // Set the click listener for the login button
        loginBinding.btnLogin.setOnClickListener {
            val email = loginBinding.editEmail.text.toString().trim()
            val password = loginBinding.editPassword.text.toString()
            signIn(email, password)
        }

        //Set up NFC Adapter
        NFCUtils.setNFCAdapter(nfcAdapter, this)

        //NFC Process Start
        NFCUtils.onNFCStart(this, javaClass)
    }

    //NFC will call this intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action

        //If the intent action is of NFC type
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val nfcData = NFCUtils.onNFCDetected(intent, applicationContext)
            if (NFCUtils.getCardTag() == "staff") {
                if (nfcData != null) {
                    val email = String(nfcData[2].payload).drop(3)
                    val password = String(nfcData[3].payload).drop(3)
                    loginBinding.editEmail.setText(email)
                    loginBinding.editPassword.setText(password)

                    signIn(email, password)
                }
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

    private fun signIn(email: String, password: String) {
        // admin account to showcase if time permits
        if (email == "Admin" && password == "admin") {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        } else if (email.isBlank() || password.isBlank()){
            Toast.makeText(
                this, R.string.authentication_failure, Toast.LENGTH_SHORT
            ).show()
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to Main Activity
                    val intent = Intent(this, MainActivity::class.java)
                    //viewModel.saveStaffID(email)
                    //viewModel.saveStaffName()
                    intent.putExtra("email", email)
                    intent.putExtra("pw", password)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    runOnUiThread {
                        Toast.makeText(
                            this, R.string.authentication_failure, Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        }
    }
}