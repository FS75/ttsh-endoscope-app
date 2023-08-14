package com.example.myapplication.obsolete

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication.entities.Employee
import com.example.myapplication.entities.Scope

// Obsolete class, wont be using as we now have firebase, but we should just leave it here

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    val TAG = "DBHelper: "

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        var query : String

        // only uncomment this if we want to drop the table on fresh start
        // this is for testing purposes
        // generally we want the data to persist, so we wont drop table
        query = "DROP TABLE IF EXISTS $SCOPES_TABLE"
        db.execSQL(query)

        query = "DROP TABLE IF EXISTS $EMPLOYEES_TABLE"
        db.execSQL(query)

        // below is a sqlite query, where column names
        // along with their data types is given
        query = """
                    CREATE TABLE $SCOPES_TABLE(
                        $ID_COL INTEGER PRIMARY KEY,
                        $SERIAL_COL TEXT,
                        $BRAND_COL TEXT,
                        $TYPE_COL TEXT,
                        $MODEL_COL TEXT,
                        $NURSE_COL TEXT,
                        $SHIFT_COL TEXT,
                        $DATE_COL TEXT,
                        $STATUS_COL TEXT
                    )
                """


        db.execSQL(query)

        query = """
                    CREATE TABLE $EMPLOYEES_TABLE(
                        $ID_COL INTEGER PRIMARY KEY,
                        $ID_COL2 TEXT,
                        $NAME_COL TEXT,
                        $TYPE_COL2 TEXT
                    )
                """


        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS $SCOPES_TABLE")
        onCreate(db)
    }

    // below method is to get
    // all data from our database
    fun getScopes(): Cursor? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM $SCOPES_TABLE", null)
    }

    fun getEmployees(): Cursor? {

        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM $EMPLOYEES_TABLE", null)
    }

    // This method is for adding data in our database
    fun addScope(scope: Scope){

        // values to be inserted later on
        // we do it this way instead of below as it is more efficient to do so
        // (and it looks nicer)
        val values = ContentValues().apply {
            put(SERIAL_COL, scope.serialNo)
            put(BRAND_COL, scope.brand)
            put(TYPE_COL, scope.type)
            put(MODEL_COL, scope.model)
            put(NURSE_COL, scope.nurse)
            put(SHIFT_COL, scope.shift)
            put(DATE_COL, scope.date)
            put(STATUS_COL, scope.status)
        }

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        val db = this.writableDatabase // writableDatabase seems too op
        var failure : Long = -1

        // all values are inserted into database
        if (db.insert(SCOPES_TABLE, null, values) == failure) {
            Log.d(TAG, "Failed to insert scope")
        }

        else {
            Log.d(TAG, "Added Scope to DB:\nNAME: " + scope.serialNo +
                    "\nBRAND: " + scope.brand +
                    "\nTYPE: " + scope.type +
                    "\nMODEL: " + scope.model +
                    "\nNURSE: " + scope.nurse +
                    "\nSHIFT: " + scope.shift +
                    "\nDATE: " + scope.date +
                    "\nSTATUS: " + scope.status)
        }

        // closing our database
        db.close()
    }

    fun addEmployee(employee: Employee){

        val values = ContentValues().apply {
            put(ID_COL2, employee.id)
            put(NAME_COL, employee.name)
            put(TYPE_COL2, employee.type)
        }

        val db = this.writableDatabase // writableDatabase seems too op
        var failure : Long = -1

        // all values are inserted into database
        if (db.insert(EMPLOYEES_TABLE, null, values) == failure) {
            Log.d(TAG, "Failed to insert employee")
        }

        else {
            Log.d(TAG, "Added Employee to DB:\nID: " + employee.id +
                    "\nNAME: " + employee.name +
                    "\nTYPE: " + employee.type)
        }

        // closing our database
        db.close()
    }

    fun deleteScope(serialNumber : String) : Int {
        val db = this.writableDatabase

        val deleteResult = db.delete(SCOPES_TABLE, "serialNo=?", arrayOf(serialNumber))

        db.close()
        return deleteResult
    }

    fun deleteEmployee(empId : String) : Int {
        val db = this.writableDatabase

        val deleteResult = db.delete(EMPLOYEES_TABLE, "empId=?", arrayOf(empId))

        db.close()
        return deleteResult
    }

    fun updateScopeDetails(serialNumber: String, nurse : String, shift : String, date : String, status : String) : Int {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(NURSE_COL, nurse)
            put(SHIFT_COL, shift)
            put(DATE_COL, date)
            put(STATUS_COL, status)
        }

        val updateResult = db.update(SCOPES_TABLE, values, "serialNo=?", arrayOf(serialNumber))

        db.close()
        return updateResult
    }

    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "FAKE_TTSH_DB"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        val SCOPES_TABLE = "Scopes"
        val EMPLOYEES_TABLE = "Employees"

        val ID_COL = "id"

        val SERIAL_COL = "serialNo"
        val BRAND_COL = "brand"
        val TYPE_COL = "type"
        val MODEL_COL = "model"
        val NURSE_COL = "nurse"
        val SHIFT_COL = "shift"
        val DATE_COL = "date"
        val STATUS_COL = "status"

        val ID_COL2 = "empId"
        val NAME_COL = "name"
        val TYPE_COL2 = "type"
    }
}