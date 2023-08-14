package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.databinding.FragmentAdminBinding

/**
 * A simple [Fragment] subclass.
 * Use the [AdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminFragment : Fragment() {
    val TAG = "AdminFragment"

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var qrValue = data?.getStringExtra("value")

                Log.d(TAG, qrValue.toString())

                if (qrValue != null) {
                    Toast.makeText(activity, qrValue.toString(), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    activity, "Activity result error code: ${result.resultCode}", Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        setListeners()
    }

    private fun setListeners() {
        // add scope btn on click listener
        binding.scopesBtn.setOnClickListener {
            loadFragment(AdminScopesFragment())
        }

        // display scopes btn on click listener
        binding.employeesBtn.setOnClickListener {
            loadFragment(AdminEmployeesFragment())
        }

        binding.testCameraBtn.setOnClickListener {
            val intent = Intent(activity, CameraQRActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.container, fragment)
        transaction?.addToBackStack(fragment.id.toString())
        transaction?.commit()
    }
}