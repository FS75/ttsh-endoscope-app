package com.example.myapplication

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.viewmodel.SharedViewModel
import com.example.myapplication.firebase.FirebaseDB
import android.content.Intent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentScheduleBinding
import com.example.myapplication.entities.Scope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList



/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduleFragment : Fragment() {

    val TAG = "ScheduleFragment"
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var tableRecyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var scheduleList: LiveData<ArrayList<Scope>>
    private lateinit var firebaseDB: FirebaseDB
    private lateinit var selectedSpinnerItem: String
    private lateinit var sortSpinner: Spinner

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        firebaseDB = FirebaseDB()
        firebaseDB.setUpChildListener()

        binding.imageButtonHome.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        setUpSpinner()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        tableRecyclerView = binding.tableRecyclerView
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scheduleList.value?.clear()
        _binding = null
    }


    // To fetch the scope information from the DB and store it as schedule object
    private fun fetchDataFromFireBase() {
        scheduleList = sharedViewModel.ScheduleArrayList
        GlobalScope.launch {
            firebaseDB.getScopes { scopes ->    // Retrieve all the scopes and store into one scopes object
                val sortedScopes = when (selectedSpinnerItem) {
                    "Brand" -> scopes.sortedWith(compareBy { it.brand })
                    "Serial Number" -> scopes.sortedWith(compareBy { it.serialNo })
                    "Scope Type" -> scopes.sortedWith(compareBy { it.type })
                    "Model Number" -> scopes.sortedWith(compareBy { it.model })
                    "Shift (Day/Night)" -> scopes.sortedWith(compareBy { it.shift })
                    "Date" -> scopes.sortedWith(compareBy { it.date })
                    else -> emptyList() // Handle unknown spinner item
                }
                // We only need scope of status 1 which is not yet sampled scopes to be displayed
                for (scope in sortedScopes){
                    if (scope.status == "1"){
                        scheduleList.value?.add(scope)
                        tableRecyclerView.layoutManager = LinearLayoutManager(activity)
                        tableRecyclerView.setHasFixedSize(true)
                        scheduleAdapter = ScheduleAdapter(sharedViewModel, scheduleList)
                        tableRecyclerView.adapter = scheduleAdapter
                    }
                }
            }
        }
    }


    // To allow user to pick which item to appear first in the schedule table
    private fun setUpSpinner() {
        sortSpinner = binding.spinnerSort
        val spinnerItems = listOf("Brand",
            "Serial Number",
            "Scope Type",
            "Model Number",
            "Shift (Day/Night)",
            "Date")

        val arrayAdapter = ArrayAdapter(requireContext(),R.layout.simple_spinner_item, spinnerItems)
        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = arrayAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                selectedSpinnerItem = spinnerItems[position]
                fetchDataFromFireBase()
                scheduleList.value?.clear()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }






}

