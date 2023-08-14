package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.example.myapplication.databinding.FragmentSampleScopeBinding
import com.example.myapplication.firebase.FirebaseDB
import com.example.myapplication.utilities.NFCUtils
import com.example.myapplication.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [SampleScopeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SampleScopeFragment : Fragment() {
    private var _binding: FragmentSampleScopeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseDB: FirebaseDB
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var qrValue = data?.getStringExtra("value")
                var assistantArray = qrValue?.split(",")

                if (qrValue != null) {
                    if (assistantArray?.get(0) == null || assistantArray?.get(1) == "") {
                        Toast.makeText(
                            context,
                            qrValue.toString() + " is not a valid id and name",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.assistantidEditText.setText(assistantArray?.get(0))
                        binding.assistantnameEditText.setText(assistantArray?.get(1))
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSampleScopeBinding.inflate(inflater, container, false)

        //get data from bundle here
        sharedViewModel.saveEmail(arguments?.get("email").toString())
        sharedViewModel.savePw((arguments?.get("pw").toString()))

        binding.loginBtn.setOnClickListener {
            val assistantId = binding.assistantidEditText.text.toString()
            Log.d("SampleScopeFragment", " before function:$assistantId")
            //Check Assistant Id
            if(assistantId.isNotEmpty()){
                GlobalScope.launch(Dispatchers.Main) {
                    checkEmployeeId(assistantId) { result ->
                        if (result) {
                            //Move to the next fragment in the process
                            sharedViewModel.saveAID(assistantId)
                            sharedViewModel.saveAName(binding.assistantnameEditText.text.toString())
                            loadFragment(ScopeScanningFragment())
                        } else {
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    context,
                                    "Assistant entered: $assistantId is not valid",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            } else{
                Toast.makeText(
                    context,
                    "There is no assistant details entered",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.cameraBtn.setOnClickListener {
            val intent = Intent(activity, CameraQRActivity::class.java)
            resultLauncher.launch(intent)
        }

        return binding.root
    }
    private suspend fun checkEmployeeId(employeeId: String, callback: (result: Boolean) -> Unit){
        //Access firebase DB to get nurse data
        // Access firebase DB to get nurse data
        firebaseDB.getEmployee(employeeId) { employee ->
            val result = employee != null
            callback(result)
            Log.d("SampleScopeFragment", "$result")
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

    override fun onResume() {
        super.onResume()
        val intent = activity?.intent
        Log.d("SampleScopeFrag", "NFC ONRESUME")
        //Handle NFC stuff
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            if (NFCUtils.getCardTag() == "assistant") {
                val nfcData = intent.getParcelableArrayExtra("nfcData") as Array<NdefRecord>
                sharedViewModel.saveAID(String(nfcData[1].payload).drop(3))
                sharedViewModel.saveAName(String(nfcData[4].payload).drop(3))
                loadFragment(ScopeScanningFragment())
            } else {
                Toast.makeText(
                    context,
                    R.string.NFC_not_supported,
                    Toast.LENGTH_SHORT
                ).show()
            }
            intent?.action = ""
        }
    }
}