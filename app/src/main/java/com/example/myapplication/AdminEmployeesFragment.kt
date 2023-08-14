package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.myapplication.databinding.FragmentAdminEmployeesBinding
import com.example.myapplication.entities.Employee
import com.example.myapplication.entities.Scope
import com.example.myapplication.firebase.FirebaseDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [AdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminEmployeesFragment : Fragment() {
    val TAG = "AdminScopesFragment"

    private var _binding: FragmentAdminEmployeesBinding? = null
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
        _binding = FragmentAdminEmployeesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        // add employee btn on click listener
        binding.addEmployeeBtn.setOnClickListener {
            val employeeIdEditText = binding.employeeId
            val employeeNameEditText = binding.employeeName
            val employeeTypeEditText = binding.employeeType
            val employeeEmailEditText = binding.employeeEmail

            val employeeId = employeeIdEditText.text.toString()
            val employeeName = employeeNameEditText.text.toString()
            val employeeType = employeeTypeEditText.text.toString()
            val employeeEmail = employeeEmailEditText.text.toString()

            // validation for empty fields
            if (employeeId != "" && employeeName != "" && employeeType != "" && employeeEmail != "") {
                // instantiate new employee
                var employee = Employee(
                    employeeId,
                    employeeName,
                    employeeType,
                    employeeEmail
                )
                // add the employee to firebase
                firebaseDB.addEmployee(employee)

                // clear all edit text fields
                employeeIdEditText.text.clear()
                employeeNameEditText.text.clear()
                employeeTypeEditText.text.clear()
                employeeEmailEditText.text.clear()
            } else {
                Toast.makeText(activity, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
            }
        }

        // display employees btn on click listener
        binding.displayEmployeesBtn.setOnClickListener {
            try {
                var displayEmployeeDetails = binding.displayEmployeeDetails

                if (binding.employeeId.text.toString() != "") {

                }

                // launch coroutine and get all employees from firbase
                GlobalScope.launch {
                    firebaseDB.getEmployees { employees ->
                        activity?.runOnUiThread {
                            // for each employee, add them into a text view
                            for (employee in employees) {
                                displayEmployeeDetails.append("Id: ${employee.id}\n")
                                displayEmployeeDetails.append("Name: ${employee.name}\n")
                                displayEmployeeDetails.append("Type: ${employee.type}\n")
                                displayEmployeeDetails.append("Email: ${employee.email}\n\n")
                            }
                        }
                    }
                }


            } catch (e: Exception) {
                Log.d(TAG, "DB is empty")
                Toast.makeText(activity, R.string.database_is_empty, Toast.LENGTH_SHORT).show()
            }
        }

        // delete employee btn on click listener
        binding.deleteEmployeeBtn.setOnClickListener {
            try {
                val employeeId = binding.employeeId.text.toString()

                // validation for empty employee id
                if (employeeId != "") {
                    // launch coroutine and get employee's details from firebase
                    GlobalScope.launch {
                        firebaseDB.getEmployee(employeeId) { employee ->
                            if (employee == null) {
                                activity?.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Employee with inputted id $employeeId does not exist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // delete scope since it exists
//                                Log.d("Frag", scope.toString())
                                GlobalScope.launch {
                                    firebaseDB.deleteEmployee(employeeId) { res ->
                                        // if operation was successful
                                        if (res == "OK") {
                                            activity?.runOnUiThread {
                                                Toast.makeText(
                                                    context,
                                                    "Employee with id $employeeId deleted successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        // if operation was unsuccessful
                                        else {
                                            activity?.runOnUiThread {
                                                Toast.makeText(
                                                    context,
                                                    "Employee with id $employeeId deleted unsuccessfully",
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
                    Toast.makeText(activity, R.string.please_input_employee_id, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {

            }
        }

        // update employee btn on click listener
        binding.updateEmployeeBtn.setOnClickListener {
            val employeeId = binding.employeeId.text.toString()
            val employeeName = binding.employeeName.text.toString()
            val employeeType = binding.employeeType.text.toString()
            val employeeEmail = binding.employeeEmail.text.toString()

            var newEmployee: Employee

            // validation for employee id
            if (employeeId != "") {
                // validation for all other fields
                if (employeeName != "" && employeeType != "" && employeeEmail != "") {
                    try {
                        // launch coroutine and get employee's details
                        GlobalScope.launch {
                            firebaseDB.getEmployee(employeeId) { employee ->
                                // if employee does not exist
                                if (employee == null) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Employee with inputted id $employeeId does not exist",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                // if employee exists
                                else {
                                    // instantiate new employee
                                    newEmployee = Employee(
                                        employeeId,
                                        employeeName,
                                        employeeType,
                                        employeeEmail
                                    )

                                    // launch coroutine and update employee with new employee
                                    // entity, based on his/her id
                                    GlobalScope.launch {
                                        firebaseDB.updateEmployeeDetails(employeeId, newEmployee) { res ->
                                            // if operation was successful
                                            if (res == "OK") {
                                                activity?.runOnUiThread {
                                                    Toast.makeText(
                                                        context,
                                                        "Employee with id $employeeId updated successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            // if operation was unsuccessful
                                            else {
                                                activity?.runOnUiThread {
                                                    Toast.makeText(
                                                        context,
                                                        "Employee with id $employeeId updated unsuccessfully",
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
                Toast.makeText(activity, R.string.please_input_employee_id, Toast.LENGTH_SHORT).show()
            }
        }
    }
}