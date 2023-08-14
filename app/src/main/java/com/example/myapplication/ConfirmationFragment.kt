package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentConfirmationBinding
import com.example.myapplication.firebase.FirebaseDB
import com.example.myapplication.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConfirmationFragment : Fragment() {
    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseDB: FirebaseDB
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()

        firebaseDB = FirebaseDB()
        firebaseDB.setUpChildListener()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)

        var editFlag = false

        //Get current date and time and place it inside the respective columns
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val stf = SimpleDateFormat("HH:mm:ss")
        binding.dateEditText.setText(sdf.format(Date()))
        binding.timeEditText.setText(stf.format(Date()))

        //Employee details
        sharedViewModel.staffID.observe(
            viewLifecycleOwner,
            { staffID -> binding.staffidEditText.setText(staffID) })
        sharedViewModel.staffName.observe(
            viewLifecycleOwner,
            { staffName -> binding.staffnameEditText.setText(staffName) })
        sharedViewModel.AID.observe(
            viewLifecycleOwner,
            { AID -> binding.assistIDEditText.setText(AID) })
        sharedViewModel.AName.observe(
            viewLifecycleOwner,
            { AName -> binding.assistNameEditText.setText(AName) })

        //Scope Details
        sharedViewModel.Serial.observe(
            viewLifecycleOwner,
            { Serial -> binding.serialEditText.setText(Serial) })
        sharedViewModel.Brand.observe(
            viewLifecycleOwner,
            { Brand -> binding.brandEditText.setText(Brand) })
        sharedViewModel.Type.observe(
            viewLifecycleOwner,
            { Type -> binding.typeEditText.setText(Type) })
        sharedViewModel.Model.observe(
            viewLifecycleOwner,
            { Model -> binding.modelEditText.setText(Model) })


        binding.submitBtn.setOnClickListener {
            //TODO:CHANGE TO FRAGMENT AND SAVE ALL DETAILS TO SHAREDVIEWMODEL
            val staffId = binding.staffidEditText.text.toString()
            val assistantId = binding.assistIDEditText.text.toString()

            if(staffId.isNotEmpty() && assistantId.isNotEmpty()){
                //Check Staff and Assistant Id before going to next page and saving to viewmodel
                GlobalScope.launch(Dispatchers.Main) {
                    checkEmployeeId(staffId, assistantId){result ->
                        if(result){
                            //Add to viewmodel the employee details
                            sharedViewModel.saveStaffID(binding.staffidEditText.text.toString())
                            sharedViewModel.saveStaffName(binding.staffnameEditText.text.toString())
                            sharedViewModel.saveAID(binding.assistIDEditText.text.toString())
                            sharedViewModel.saveAName(binding.assistNameEditText.text.toString())

                            //Add to viewmodel the sampling details
                            sharedViewModel.saveDate(binding.dateEditText.text.toString())
                            sharedViewModel.saveTime(binding.timeEditText.text.toString())

                            //Add to viewmodel the scope details
                            sharedViewModel.saveSerial(binding.serialEditText.text.toString())
                            sharedViewModel.saveBrand(binding.brandEditText.text.toString())
                            sharedViewModel.saveType(binding.typeEditText.text.toString())
                            sharedViewModel.saveModel(binding.modelEditText.text.toString())

                            //Navigate to the next step
                            loadFragment(FinishSampleFragment())
                        }
                        else{
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    context,
                                    "Assistant/Staff entered is not valid",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(
                    context,
                    "There is no assistant/staff details entered",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        //Only allow editing of columns if edit button is pressed
        binding.editBtn.setOnClickListener {
            if (editFlag == false) {
                enableAll()
                binding.submitBtn.isEnabled = false
                editFlag = true
            } else {
                disableAll()
                binding.staffidEditText.isEnabled = false
                binding.submitBtn.isEnabled = true
                editFlag = false
            }
        }

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

    private fun enableAll() {
        binding.staffidEditText.isEnabled = true
        binding.staffnameEditText.isEnabled = true
        binding.assistIDEditText.isEnabled = true
        binding.assistNameEditText.isEnabled = true
        binding.dateEditText.isEnabled = true
        binding.timeEditText.isEnabled = true
        binding.serialEditText.isEnabled = true
        binding.brandEditText.isEnabled = true
        binding.typeEditText.isEnabled = true
        binding.modelEditText.isEnabled = true
    }

    private fun disableAll() {
        binding.staffidEditText.isEnabled = false
        binding.staffnameEditText.isEnabled = false
        binding.assistIDEditText.isEnabled = false
        binding.assistNameEditText.isEnabled = false
        binding.dateEditText.isEnabled = false
        binding.timeEditText.isEnabled = false
        binding.serialEditText.isEnabled = false
        binding.brandEditText.isEnabled = false
        binding.typeEditText.isEnabled = false
        binding.modelEditText.isEnabled = false
    }

    private suspend fun checkEmployeeId(nurseId: String, assistantId: String, callback: (result: Boolean) -> Unit) {
        var count = 0
        var nurseResult = false
        var assistantResult = false

        firebaseDB.getEmployee(nurseId) { employee ->
            nurseResult = employee != null
            count++
            if (count == 2) {
                callback(nurseResult && assistantResult)
            }
        }

        firebaseDB.getEmployee(assistantId) { employee ->
            assistantResult = employee != null
            count++
            if (count == 2) {
                callback(nurseResult && assistantResult)
            }
        }
    }
}