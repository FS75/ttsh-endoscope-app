package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.entities.Scope
import com.example.myapplication.entities.ScopeStatus

class SharedViewModel : ViewModel() {

    // Declaration of variables BELOW

    private var _email = MutableLiveData("")
    val email: LiveData<String> = _email
    private var _pw = MutableLiveData("")
    val pw: LiveData<String> = _pw

    //Employee Details
    private var _staffID = MutableLiveData("")
    val staffID: LiveData<String> = _staffID
    private var _staffName = MutableLiveData("")
    val staffName: LiveData<String> = _staffName
    private var _AID = MutableLiveData("test")
    val AID: LiveData<String> = _AID
    private var _AName = MutableLiveData("test")
    val AName: LiveData<String> = _AName

    //Sampling Details
    private var _Date = MutableLiveData("")
    val Date: LiveData<String> = _Date
    private var _Time = MutableLiveData("")
    val Time: LiveData<String> = _Time

    //Scope Details
    private var _Serial = MutableLiveData("")
    val Serial: LiveData<String> = _Serial
    private var _Brand = MutableLiveData("")
    val Brand: LiveData<String> = _Brand
    private var _Type = MutableLiveData("")
    val Type: LiveData<String> = _Type
    private var _Model = MutableLiveData("")
    val Model: LiveData<String> = _Model

    // Status variables
    private var _ScopeArrayList = MutableLiveData(ArrayList<ScopeStatus>())
    val ScopeArrayList: LiveData<ArrayList<ScopeStatus>> = _ScopeArrayList

    // Schedule Variables
    private var _ScheduleArrayList = MutableLiveData(ArrayList<Scope>())
    val ScheduleArrayList: LiveData<ArrayList<Scope>> = _ScheduleArrayList

    // Function to call to save to viewModel from fragments
    fun saveEmail(newE: String) {
        _email.value = newE
    }

    fun savePw(newPw: String) {
        _pw.value = newPw
    }

    fun saveStaffID(newSID: String) {
        _staffID.value = newSID
    }

    fun saveStaffName(newSN: String) {
        _staffName.value = newSN
    }

    fun saveAID(newAID: String) {
        _AID.value = newAID
    }

    fun saveAName(newAName: String) {
        _AName.value = newAName
    }

    fun saveDate(newDate: String) {
        _Date.value = newDate
    }

    fun saveTime(newTime: String) {
        _Time.value = newTime
    }

    fun saveSerial(newSerial: String) {
        _Serial.value = newSerial
    }

    fun saveBrand(newBrand: String) {
        _Brand.value = newBrand
    }

    fun saveType(newType: String) {
        _Type.value = newType

    }

    fun saveModel(newModel: String) {
        _Model.value = newModel
    }

    fun addToStatusList(scopeStatus: ScopeStatus) {
        _ScopeArrayList.value?.add(scopeStatus)
    }

}