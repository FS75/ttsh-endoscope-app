package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentStatusBinding
import com.example.myapplication.entities.Scope
import com.example.myapplication.firebase.FirebaseDB
import com.example.myapplication.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.net.ssl.SSLEngineResult.Status

/**
 * A simple [Fragment] subclass.
 * Use the [StatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusFragment : Fragment() {
    val TAG = "StatusFragment"
    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var firebaseDB: FirebaseDB
    private lateinit var touchHelper: ItemTouchHelper

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        userRecyclerView = binding.scopeList
        userRecyclerView.layoutManager = LinearLayoutManager(activity)
        userRecyclerView.setHasFixedSize(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sharedViewModel.ScopeArrayList.value?.clear()
    }

    override fun onStart() {
        super.onStart()
        firebaseDB = FirebaseDB()
        firebaseDB.setUpChildListener()
        setSwipeListener()
        setListeners()

    }

    private fun setSwipeListener() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                //Do nothing
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                //check if the status 3, update to 1 when swiped
                if (sharedViewModel.ScopeArrayList.value?.get(position)?.status == "3") {
                    var scopeId = sharedViewModel.ScopeArrayList.value?.get(position)?.serialNo
                    Log.d(TAG,"onSwiped: $scopeId")

                    //Update the db to 2
                    GlobalScope.launch{
                        if (scopeId != null) {
                            firebaseDB.updateScopeField(scopeId, "status", "1"){ res ->
                                if (res == "OK") {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Succesffuly Updated Scope",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                else {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Unsuccessfully Updated Scope",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                            var result =sharedViewModel.ScopeArrayList.value?.get(position)?.status
                            Log.d(TAG,"$result")
                            // Update the UI components on the main/UI thread
                            withContext(Dispatchers.Main) {
                                // Update the UI components on the main/UI thread
                                var adapter = StatusAdapter(sharedViewModel, position)
                                userRecyclerView.adapter = adapter
                                loadFragment(StatusFragment())
                            }
                        }
                    }
                    //notify the adapter of the data change
                    userRecyclerView.adapter?.notifyItemChanged(position)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.parseColor("#c27878"))
                //draw the red background for swiping to the left
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        touchHelper = ItemTouchHelper(simpleItemTouchCallback)
        touchHelper.attachToRecyclerView(userRecyclerView)
    }

    fun setListeners() {
        var selectedPosition: Int = -1
        var isFirstLoad = true
        GlobalScope.launch {
            // use getScopes function
            firebaseDB.getScopesStatusObjectList { scopesStatusObjectList ->
                for (scopeStatusObject in scopesStatusObjectList) {
                    sharedViewModel.addToStatusList(scopeStatusObject)
                }

                Log.d(TAG, "Scope Data: ${sharedViewModel.ScopeArrayList}")

                var adapter = StatusAdapter(sharedViewModel, selectedPosition)
                userRecyclerView.adapter = adapter
                if (isFirstLoad) {
                    isFirstLoad = false
                    userRecyclerView.post {
                        userRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
                    }
                }
                adapter.setOnItemClickListener(object : StatusAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val recyclerView = binding.scopeList
                        recyclerView.adapter?.notifyDataSetChanged()

                        // update the selected position
                        if (selectedPosition == position) {
                            selectedPosition = -1
                        } else {
                            selectedPosition = position
                        }

                        var selectedScopeStatus =
                            adapter.scopeList.value?.get(position)?.status?.toInt()
                        recyclerView.smoothScrollToPosition(position)
                        recyclerView.postDelayed({
                            //For selected position
                            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                            if (viewHolder != null) {
                                //switch case to check which status scope selected
                                when (selectedScopeStatus) {
                                    1 -> {
                                        val cardView =
                                            viewHolder.itemView.findViewById<LinearLayout>(R.id.cardviewStatusOne)
                                        cardView.setBackgroundResource(R.drawable.selected_item_background)
                                    }
                                    2 -> {
                                        val cardView =
                                            viewHolder.itemView.findViewById<LinearLayout>(R.id.cardviewStatusTwo)
                                        cardView.setBackgroundResource(R.drawable.selected_item_background)
                                    }
                                    else -> {
                                        val cardView =
                                            viewHolder.itemView.findViewById<LinearLayout>(R.id.cardviewStatusThree)
                                        cardView.setBackgroundResource(R.drawable.selected_item_background)
                                    }
                                }
                            }
                        }, 200)
                        //Get variables to pass into populate scrollview functions
                        val scopeSerial = adapter.scopeList.value?.get(position)?.serialNo!!
                        val nurseId = adapter.scopeList.value?.get(position)!!.nurse!!
                        val assistantId = adapter.scopeList.value?.get(position)!!.assistant!!
                        Log.d(TAG, "Populate scrollView: $scopeSerial")
                        Log.d(TAG, "Populate scrollView: $nurseId")
                        Log.d(TAG, "Populate scrollView: $assistantId")
                        //To populate staff scrollview
                        populateScrollViewStaff(nurseId, assistantId)
                        //To populate scope scrollview
                        populateScrollViewScope(scopeSerial)
                    }
                })
            }
        }
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.container, fragment)
        transaction?.addToBackStack(fragment.id.toString())
        transaction?.commit()
    }
    private fun populateScrollViewScope(scopeSerial: String) {
        //variables to populate details
        var displayScopeDetails = binding.displayScopeDetailsStatus
        var displaySamplingDetails = binding.displaySamplingDetails
        var displayScopeText = binding.scopeDetails
        var displaySamplingText = binding.samplingDetails
        GlobalScope.launch(Dispatchers.Main) {
            firebaseDB.getScope(scopeSerial) { scope ->
                displayScopeDetails.setText("")
                displaySamplingDetails.setText("")
//                Log.d(TAG, "child: $child")
//                Log.d(TAG, "child: ${child.value}")
                activity?.runOnUiThread {
                    if (scope != null) {
                        //Update Sample details & Scope details on main thread
                        //Updating Sampling Details -> Date, Time
                        var text = resources.getString(R.string.samplingDetails)
                        displaySamplingText.setText(text)

                        displaySamplingDetails.append("Date: ${scope.date}\n")
                        displaySamplingDetails.append("Time: ${scope.time}\n")
                        //Updating Scope Details
                        //Sampling Details -> Date, Time
                        //Scope Details-> SerialNo, Brand, Type, Model, Status
                        text = resources.getString(R.string.scopeDetails)
                        displayScopeText.setText(text)
                        displayScopeDetails.append("Serial: ${scope.serialNo}\n")
                        displayScopeDetails.append("Brand: ${scope.brand}\n")
                        displayScopeDetails.append("Type: ${scope.type}\n")
                        displayScopeDetails.append("Model: ${scope.model}\n")
                        //displaying the different statuses meaning
                        if (scope.status == "1") {
                            var spannableText = SpannableString("Status: Not Sampled \n")
                            var color = ForegroundColorSpan(Color.parseColor("#c27878"))
                            spannableText.setSpan(
                                color,
                                0,
                                spannableText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            displayScopeDetails.append(spannableText)
                        } else if (scope.status == "2") {
                            var spannableText = SpannableString("Status: Sampled \n")
                            var color = ForegroundColorSpan(Color.parseColor("#78c281"))
                            spannableText.setSpan(
                                color,
                                0,
                                spannableText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            displayScopeDetails.append(spannableText)
                        } else {
                            var spannableText =
                                SpannableString("Status: Sent to Infection Control \n")
                            var color = ForegroundColorSpan(Color.parseColor("#5ca2d7"))
                            spannableText.setSpan(
                                color,
                                0,
                                spannableText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            displayScopeDetails.append(spannableText)
                        }
                    }
                }
            }
        }
    }

    private fun populateScrollViewStaff(nurseId: String, assistantId: String) {
        //variables to populate details
        var displayStaffDetails = binding.displayStaffDetails
        var displayEmployeeText = binding.employeeDetails
        var staffList = arrayListOf<String>()
        //Access firebase DB to get nurse data
        GlobalScope.launch {
            // Access firebase DB to get nurse data
            firebaseDB.getEmployee(nurseId) { employee ->
                if (employee != null) {

                    staffList.add(employee.id.toString())
                    staffList.add(employee.name.toString())
//                  Log.d(TAG, "nurse: ${child.key.toString()}")
                }
            }
            //Access firebase DB to get assistant data
            firebaseDB.getEmployee(assistantId) { employee ->
                if (employee != null) {
                    staffList.add(employee.id.toString())
                    staffList.add(employee.name.toString())
//                  Log.d(TAG, "assistant: ${child.key.toString()}")
                }
                displayStaffDetails.setText("")
                Log.d(TAG, "StaffListInside= $staffList")
                activity?.runOnUiThread {
                    var text = resources.getString(R.string.employeeDetails)
                    displayEmployeeText.setText(text)
                    displayStaffDetails.append("Nurse ID: ${staffList[0]}\n")
                    displayStaffDetails.append("Nurse Name: ${staffList[1]}\n")
                    displayStaffDetails.append("Assistant ID: ${staffList[2]}\n")
                    displayStaffDetails.append("Assistant Name: ${staffList[3]}\n")
                }
            }
        }
    }
}