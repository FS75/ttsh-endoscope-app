package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentScopeScanBinding
import com.example.myapplication.firebase.FirebaseDB
import com.example.myapplication.utilities.NFCUtils
import com.example.myapplication.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ScopeScanningFragment : Fragment() {
    private var _binding: FragmentScopeScanBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    //For camera activity to go to confirmation activity after scanning qrcode
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var qrValue = data?.getStringExtra("value")
                var scopeArray = qrValue?.split(",")

                if (qrValue != null) {
                    sharedViewModel.saveSerial(scopeArray?.get(0).toString())
                    sharedViewModel.saveBrand(scopeArray?.get(1).toString())
                    sharedViewModel.saveType(scopeArray?.get(2).toString())
                    sharedViewModel.saveModel(scopeArray?.get(3).toString())
                    loadFragment(ConfirmationFragment())
                }
            } else {
                Toast.makeText(
                    context,
                    "Activity result error code: ${result.resultCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScopeScanBinding.inflate(inflater, container, false)



        setStaffInfo()
        binding.cameraBtn.setOnClickListener {
            val intent = Intent(activity, CameraQRActivity::class.java)
            resultLauncher.launch(intent)
        }

        binding.proceedBtn.setOnClickListener{
            if(binding.serialEditText.text.toString().isEmpty()||binding.brandEditText.text.toString().isEmpty()||
                binding.typeEditText.text.toString().isEmpty()|| binding.modelEditText.text.toString().isEmpty())
                Toast.makeText(context,"Please fill out all fields",Toast.LENGTH_SHORT).show()
            else {
                sharedViewModel.saveSerial(binding.serialEditText.text.toString())
                sharedViewModel.saveBrand(binding.brandEditText.text.toString())
                sharedViewModel.saveType(binding.typeEditText.text.toString())
                sharedViewModel.saveModel(binding.modelEditText.text.toString())
                loadFragment(ConfirmationFragment())
            }
        }
        binding.manualBtn.setOnClickListener {

            //Set visible
            binding.serialEditText.isVisible = true
            binding.brandEditText.isVisible = true
            binding.typeEditText.isVisible = true
            binding.modelEditText.isVisible = true
            binding.proceedBtn.isVisible = true

            //Set invisible
            binding.imageView.clearAnimation()
            binding.imageView.isInvisible = true
            binding.manualBtn.isInvisible = true

        }

        //Animation for image
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = 2000 // 2 seconds
        anim.repeatCount = 5
        anim.repeatMode = Animation.REVERSE
        binding.imageView.startAnimation(anim)

        return binding.root
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

    override fun onResume() {
        super.onResume()
        val intent = activity?.intent
        //Handle NFC stuff
        if(NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action){
            if(NFCUtils.getCardTag() == "scope") {
                try {
                    //Get NFC data and set accordingly
                    val nfcData = intent.getParcelableArrayExtra("nfcData") as Array<NdefRecord>
                    sharedViewModel.saveSerial(String(nfcData[1].payload).drop(3))
                    sharedViewModel.saveBrand(String(nfcData[2].payload).drop(3))
                    sharedViewModel.saveType(String(nfcData[3].payload).drop(3))
                    sharedViewModel.saveModel(String(nfcData[4].payload).drop(3))
                    //Clear intent
                    intent?.action = ""

                    //Set staff info to ViewModel
                    setStaffInfo()

                    //Load Fragment
                    loadFragment(ConfirmationFragment())
                }catch (e: Exception){
                    intent?.action = ""
                    Toast.makeText(
                        context,
                        R.string.NFC_not_supported,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                intent?.action = ""
                Toast.makeText(
                    context,
                    R.string.NFC_not_supported,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setStaffInfo() {
        var firebaseDB = FirebaseDB()
        firebaseDB.setUpChildListener()

        //If want to put value from viewmodel in variable
        var email = sharedViewModel.email.value.toString()
//        var email2 = ""
        GlobalScope.launch(Dispatchers.IO) {
            firebaseDB.getEmployees() { employees ->
                for (employee in employees) {
                    //check email used to log in against database
                    if (email == employee.email) {
                        sharedViewModel.saveStaffID(employee.id.toString())
                        sharedViewModel.saveStaffName(employee.name.toString())
                    }
                }
            }
        }
    }
}