package com.example.myapplication.firebase

import android.util.Log
import com.example.myapplication.entities.Employee
import com.example.myapplication.entities.Scope
import com.example.myapplication.entities.ScopeStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class FirebaseDB {

    interface IFirebaseDB {
        fun getScope(id: String): Task<DataSnapshot>
    }

    private val TAG = "FirebaseDB"

    private val database = Firebase.database(
        "https://my-application-a4ed1-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val dbRef = database.getReference("ttsh-database")

    fun setUpChildListener() {
        val childListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                /*val data = dataSnapshot.children.map {
                    child -> Log.d(TAG, child.toString())
                }*/
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val data = dataSnapshot.children.map { child ->
                    Log.d(TAG, child.toString())
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }

        dbRef.addChildEventListener(childListener)
    }

    fun addEmployee(newEmployee: Employee) {
        dbRef.child("employees").child(newEmployee.id.toString()).setValue(newEmployee)
    }

    fun addScope(newScope: Scope) {
        dbRef.child("scopes").child(newScope.serialNo.toString()).setValue(newScope)
    }

    suspend fun getEmployee(id: String, callback: (Employee?) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("employees").child(id).get().addOnSuccessListener {  res ->
                if (res.value == null){
                    callback(null)
                }
                else {
                    val employee = Employee(
                        res.child("id").value.toString(),
                        res.child("name").value.toString(),
                        res.child("type").value.toString(),
                    )
                    callback(employee)
                }
            }
        }
    }

    suspend fun getEmployees(callback: (List<Employee>) -> Unit) {
        lateinit var employee: Employee
        var listOfEmployees = mutableListOf<Employee>()

        withContext(Dispatchers.IO) {
            dbRef.child("employees").get().addOnSuccessListener { res ->
                for (child in res.children) {
                    employee = Employee(
                        child.child("id").value.toString(),
                        child.child("name").value.toString(),
                        child.child("type").value.toString(),
                        child.child("email").value.toString()
                    )

                    listOfEmployees.add(employee)
                }
                callback(listOfEmployees)
            }
        }
    }

    suspend fun getScopesStatusObjectList(callback: (List<ScopeStatus>) -> Unit) {
        var listOfScopesStatusObjects = mutableListOf<ScopeStatus>()

        withContext(Dispatchers.IO) {
            dbRef.child("scopes").get().addOnSuccessListener { res ->
                for (child in res.children) {
                    val scope = child.getValue(ScopeStatus::class.java)

                    if (scope != null) {
                        listOfScopesStatusObjects.add(scope)
                    }
                }
                callback(listOfScopesStatusObjects)
            }
        }
    }

    suspend fun getScope(id: String, callback: (Scope?) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("scopes").child(id).get().addOnSuccessListener {  res ->
                if (res.value == null){
                    callback(null)
                }
                else {
                    val scope = Scope(
                        res.child("assistant").value.toString(),
                        res.child("brand").value.toString(),
                        res.child("date").value.toString(),
                        res.child("model").value.toString(),
                        res.child("nurse").value.toString(),
                        res.child("serialNo").value.toString(),
                        res.child("shift").value.toString(),
                        res.child("status").value.toString(),
                        res.child("time").value.toString(),
                        res.child("type").value.toString()
                    )
                    callback(scope)
                }
            }
        }
    }

    suspend fun getScopes(callback: (List<Scope>) -> Unit)  {
        lateinit var scope: Scope
        var listOfScopes = mutableListOf<Scope>()

        withContext(Dispatchers.IO) {
            dbRef.child("scopes").get().addOnSuccessListener { res ->
                for (child in res.children) {
                    scope = Scope(
                        child.child("assistant").value.toString(),
                        child.child("brand").value.toString(),
                        child.child("date").value.toString(),
                        child.child("model").value.toString(),
                        child.child("nurse").value.toString(),
                        child.child("serialNo").value.toString(),
                        child.child("shift").value.toString(),
                        child.child("status").value.toString(),
                        child.child("time").value.toString(),
                        child.child("type").value.toString()
                    )

                    listOfScopes.add(scope)
                }
                callback(listOfScopes)
//                Log.d("Firebase", listOfScopes.toString())
            }
        }
    }

    // function to update a single detail (field) of an employee
    // pass in the id of the employee, field you want to update and value to be updated
    fun updateEmployeeField(id: String, field: String, value: String) {
        // get current employee
        // update a specific field
        dbRef.child("employees").child(id).child(field).setValue(value)
    }

    // function to update part of or all details of an employee
    // pass in the id of employee and an Employee entity
    suspend fun updateEmployeeDetails(id: String, newEmployee: Employee, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("employees").child(id).setValue(newEmployee)
                .addOnSuccessListener {
                    callback("OK")
                }
                .addOnFailureListener {
                    callback("BAD")
                }
        }
    }

    // function to update a single detail (field) of a scope
    // pass in the id of the scope, field you want to update and value to be updated
    suspend fun updateScopeField(id: String, field: String, value: String, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("scopes").child(id).child(field).setValue(value)
                .addOnSuccessListener {
                    callback("OK")
                }
                .addOnFailureListener {
                    callback("BAD")
                }
        }

    }

    // function to update part of or all details of a scope
    // pass in the id of scope and a Scope entity
    suspend fun updateScopeDetails(id: String, newScope: Scope, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("scopes").child(id).setValue(newScope)
                .addOnSuccessListener {
                    callback("OK")
                }
                .addOnFailureListener {
                    callback("BAD")
                }
        }
    }

    suspend fun deleteScope(id: String, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("scopes").child(id).removeValue()
                .addOnSuccessListener {
                    callback("OK")
                }
                .addOnFailureListener {
                    callback("BAD")
                }
        }
    }

    suspend fun deleteEmployee(id: String, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            dbRef.child("employees").child(id).removeValue()
                .addOnSuccessListener {
                    callback("OK")
                }
                .addOnFailureListener {
                    callback("BAD")
                }
        }
    }

    // experimental function to add data to firebase
    // we should not be calling this anywhere after app is released
    fun initialSeed() {
        /*addEmployee("0", "Juleus0", "Staff")
        addEmployee("1", "Juleus1", "Assistant")
        addScope("0", "a", "b", "c", "d", "e", "f", "g")*/
    }
}
