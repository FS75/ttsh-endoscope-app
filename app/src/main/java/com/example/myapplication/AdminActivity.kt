package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    val TAG = "AdminActivity"

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadFragment(AdminEmployeesFragment())
        // Upon selecting any item on button nav
        binding.bottomNav2.setOnItemSelectedListener {
            when (it.itemId) {
                // Load admin employees fragment
                R.id.admin_employee -> {
                    loadFragment(AdminEmployeesFragment())
                    true
                }
                // Load admin scopes fragment
                R.id.admin_scope -> {
                    loadFragment(AdminScopesFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(fragment.id.toString())
        transaction.commit()
    }
}