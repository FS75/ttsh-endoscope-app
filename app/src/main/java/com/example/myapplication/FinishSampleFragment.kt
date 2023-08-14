package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentFinishSampleBinding
import com.example.myapplication.entities.Scope
import com.example.myapplication.firebase.FirebaseDB
import com.example.myapplication.utilities.NFCUtils
import com.example.myapplication.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FinishSampleFragment : Fragment() {
    private var _binding: FragmentFinishSampleBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseDB: FirebaseDB

    private val sharedViewModel: SharedViewModel by activityViewModels()

    //For camera activity to confirm that scope scanned is same as the one used initially
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var qrValue = data?.getStringExtra("value")
                var scopeArray = qrValue?.split(",")

                if (qrValue != null) {
                    if (scopeArray?.get(0).toString() == sharedViewModel.Serial.value.toString()) {
                        finishSample()
                    } else {
                        Toast.makeText(
                            context,
                            R.string.wrong_scoped_scan_please_try_again,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "Activity result error code: ${result.resultCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onStart() {
        super.onStart()

        firebaseDB = FirebaseDB()
        firebaseDB.setUpChildListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFinishSampleBinding.inflate(inflater, container, false)
        binding.submitBtn.setOnClickListener{
            if (binding.pwEditText.text.toString() == sharedViewModel.pw.value.toString()) {
                finishSample()
            } else {
            Toast.makeText(context, R.string.invalid_password, Toast.LENGTH_SHORT).show()
        }
        }
        binding.pwBtn.setOnClickListener {
            //Check if password matches login page, if it does then change scope status to 2 and update scope fields in DB appropriately
            binding.nfcImageView.clearAnimation()
            binding.nfcImageView.isVisible = false
            binding.pwEditText.isVisible = true
            binding.submitBtn.isVisible = true
            binding.pwBtn.isInvisible = true
        }

        binding.cameraBtn.setOnClickListener {
            val intent = Intent(activity, CameraQRActivity::class.java)
            resultLauncher.launch(intent)
        }


        //Animation for image
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = 2000 // 2 seconds
        anim.repeatCount = 5
        anim.repeatMode = Animation.REVERSE
        binding.nfcImageView.startAnimation(anim)

        return binding.root
    }

    fun finishSample() {
        val AID = sharedViewModel.AID.value.toString()
        val Brand = sharedViewModel.Brand.value.toString()
        val Date = sharedViewModel.Date.value.toString()
        val Model = sharedViewModel.Model.value.toString()
        val NID = sharedViewModel.staffID.value.toString()
        val Serial = sharedViewModel.Serial.value.toString()
        val Shift = "Night"
        val Time = sharedViewModel.Time.value.toString()
        val Type = sharedViewModel.Type.value.toString()
        var newScope: Scope

        try {
            GlobalScope.launch(Dispatchers.Main) {
                firebaseDB.getScope(Serial) { res ->
                    if (res == null) {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                context,
                                "Scope with inputted serial number $Serial does not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        loadFragment(SampleScopeFragment())
                    } else {
                        newScope = Scope(
                            AID, Brand, Date, Model, NID, Serial, Shift, "2", Time, Type
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            firebaseDB.updateScopeDetails(Serial, newScope) { res ->
                                if (res == "OK") {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Scope with serial number $Serial updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    loadFragment(StatusFragment())
                                } else {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Scope with serial number $Serial updated unsuccessfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    loadFragment(SampleScopeFragment())
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            activity?.runOnUiThread {
                Toast.makeText(
                    context,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            loadFragment(SampleScopeFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = activity?.intent
        //Handle NFC stuff
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            if (NFCUtils.getCardTag() == "scope") {
                val nfcData = intent.getParcelableArrayExtra("nfcData") as Array<NdefRecord>
                val serialNo = String(nfcData[1].payload).drop(3)
                val Serial = sharedViewModel.Serial.value.toString()

                //Ensure that scope scan was the same as previous
                if (serialNo == Serial) {
                    finishSample()
                } else {
                    Toast.makeText(
                        context, R.string.wrong_scoped_scan_please_try_again, Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context, R.string.NFC_not_supported, Toast.LENGTH_SHORT
                ).show()
            }
            intent?.action = ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.container, fragment)
        transaction?.addToBackStack(fragment.id.toString())
        transaction?.commit()
    }
}