package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.myapplication.databinding.FragmentAdminScopesBinding
import com.example.myapplication.entities.Scope
import com.example.myapplication.firebase.FirebaseDB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [AdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminScopesFragment : Fragment() {
    val TAG = "AdminScopesFragment"

    private var _binding: FragmentAdminScopesBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseDB: FirebaseDB

    override fun onStart() {
        super.onStart()

        firebaseDB = FirebaseDB()
        firebaseDB.setUpChildListener()

        setListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminScopesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        // add scope btn on click listener
        binding.addScopeBtn.setOnClickListener {
            val scopeSerialEditText = binding.scopeSerial
            val scopeBrandEditText = binding.scopeBrand
            val scopeTypeEditText = binding.scopeType
            val scopeModelEditText = binding.scopeModel
            val scopeNurseEditText = binding.scopeNurse
            val scopeShiftEditText = binding.scopeShift
            val scopeDateEditText = binding.scopeDate
            val scopeStatusEditText = binding.scopeStatus
            val scopeAssistantEditText = binding.scopeAssistant
            val scopeTimeEditText = binding.scopeTime

            val scopeSerial = scopeSerialEditText.text.toString()
            val scopeBrand = scopeBrandEditText.text.toString()
            val scopeType = scopeTypeEditText.text.toString()
            val scopeModel = scopeModelEditText.text.toString()
            val scopeNurse = scopeNurseEditText.text.toString()
            val scopeShift = scopeShiftEditText.text.toString()
            val scopeDate = scopeDateEditText.text.toString()
            val scopeStatus = scopeStatusEditText.text.toString()
            val scopeAssistant = scopeAssistantEditText.text.toString()
            val scopeTime = scopeTimeEditText.text.toString()

            // validation for all fields
            if (scopeSerial != "" && scopeBrand != "" && scopeType != "" && scopeModel != ""
                && scopeNurse != "" && scopeShift != "" && scopeDate != "" && scopeStatus != ""
                && scopeAssistant != "" && scopeTime != ""
            ) {
                // instantiate new scope
                var scope = Scope(
                    scopeAssistant,
                    scopeBrand,
                    scopeDate,
                    scopeModel,
                    scopeNurse,
                    scopeSerial,
                    scopeShift,
                    scopeStatus,
                    scopeTime,
                    scopeType,
                )
                // add the scope to firebase
                firebaseDB.addScope(scope)

                // clear all edit text fields
                scopeSerialEditText.text.clear()
                scopeBrandEditText.text.clear()
                scopeTypeEditText.text.clear()
                scopeModelEditText.text.clear()
                scopeNurseEditText.text.clear()
                scopeShiftEditText.text.clear()
                scopeDateEditText.text.clear()
                scopeStatusEditText.text.clear()
                scopeAssistantEditText.text.clear()
                scopeTimeEditText.text.clear()
            } else {
                Toast.makeText(activity, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
            }
        }

        // display scopes btn on click listener
        binding.displayScopesBtn.setOnClickListener {
            try {
                var displayScopeDetails = binding.displayScopeDetails

                // launch coroutine and get all scopes from firebase
                GlobalScope.launch {
                    firebaseDB.getScopes { scopes ->
                        activity?.runOnUiThread {
                            // for each scope, add them into a text view
                            for (scope in scopes) {
                                displayScopeDetails.append("Assistant: ${scope.assistant}\n")
                                displayScopeDetails.append("Brand: ${scope.brand}\n")
                                displayScopeDetails.append("Date: ${scope.date}\n")
                                displayScopeDetails.append("Model: ${scope.model}\n")
                                displayScopeDetails.append("Nurse: ${scope.nurse}\n")
                                displayScopeDetails.append("Serial No: ${scope.serialNo}\n")
                                displayScopeDetails.append("Shift: ${scope.shift}\n")
                                displayScopeDetails.append("Status: ${scope.status}\n")
                                displayScopeDetails.append("Time: ${scope.time}\n")
                                displayScopeDetails.append("Type: ${scope.type}\n\n")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "DB is empty")
                Toast.makeText(activity, R.string.database_is_empty, Toast.LENGTH_SHORT).show()
            }
        }

        // delete scope btn on click listener
        binding.deleteScopeBtn.setOnClickListener {
            try {
                val scopeSerial = binding.scopeSerial.text.toString()

                // validation for scope serial number
                if (scopeSerial != "") {
                    // launch coroutine and get scope's details from firebase
                    GlobalScope.launch {
                        firebaseDB.getScope(scopeSerial) { scope ->
                            if (scope == null) {
                                activity?.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Scope with inputted serial number $scopeSerial does not exist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // delete scope since it exists
//                                Log.d("Frag", scope.toString())
                                GlobalScope.launch {
                                    firebaseDB.deleteScope(scopeSerial) { res ->
                                        // if operation was successful
                                        if (res == "OK") {
                                            activity?.runOnUiThread {
                                                Toast.makeText(
                                                    context,
                                                    "Scope with serial number $scopeSerial deleted successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        // if operation was unsuccessful
                                        else {
                                            activity?.runOnUiThread {
                                                Toast.makeText(
                                                    context,
                                                    "Scope with serial number $scopeSerial deleted unsuccessfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(activity, R.string.please_input_serial_no, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {

            }
        }

        // update scope btn on click listener
        binding.updateScopeBtn.setOnClickListener {
            val scopeSerial = binding.scopeSerial.text.toString()
            val scopeAssistant = binding.scopeAssistant.text.toString()
            val scopeNurse = binding.scopeNurse.text.toString()
            val scopeShift = binding.scopeShift.text.toString()
            val scopeDate = binding.scopeDate.text.toString()
            val scopeStatus = binding.scopeStatus.text.toString()
            val scopeTime = binding.scopeTime.text.toString()
            var newScope: Scope

            // validation for scope serial number
            if (scopeSerial != "") {
                // validation for other fields
                if (scopeAssistant != "" && scopeNurse != "" && scopeShift != ""
                    && scopeDate != "" && scopeStatus != "" && scopeTime != ""
                ) {
                    try {
                        // launch coroutine and get scope's detail's from firebase
                        GlobalScope.launch {
                            firebaseDB.getScope(scopeSerial) { scope ->
                                // if scope does not exist
                                if (scope == null) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Scope with inputted serial number $scopeSerial does not exist",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                // if scope does not exist
                                else {
                                    // create a new scope
                                    newScope = Scope(
                                        scopeAssistant,
                                        scope.brand,
                                        scopeDate,
                                        scope.model,
                                        scopeNurse,
                                        scopeSerial,
                                        scopeShift,
                                        scopeStatus,
                                        scopeTime,
                                        scope.type
                                    )

                                    GlobalScope.launch {
                                        firebaseDB.updateScopeDetails(scopeSerial, newScope) { res ->
                                            // if operation was successful
                                            if (res == "OK") {
                                                activity?.runOnUiThread {
                                                    Toast.makeText(
                                                        context,
                                                        "Scope with serial number $scopeSerial updated successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            // if operation was unsuccessful
                                            else {
                                                activity?.runOnUiThread {
                                                    Toast.makeText(
                                                        context,
                                                        "Scope with serial number $scopeSerial updated unsuccessfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {

                    }
                }
                else {
                    Toast.makeText(activity, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, R.string.please_input_serial_no, Toast.LENGTH_SHORT).show()
            }
        }
    }
}