package com.example.traintix

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.traintix.databinding.FragmentProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), LogoutConfirmationFragment.LogoutConfirmationListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentProfileBinding
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        prefManager = PrefManager.getInstance(requireContext())
        val username = prefManager.getUsername()

        if (username.isNotEmpty()) {
            getUserDataFromFirestore(username)
        }

        with(binding) {
            logout.setOnClickListener {
                showLogoutConfirmationDialog()
            }
        }

        return view
    }

    private fun showLogoutConfirmationDialog() {
        val logoutConfirmationDialog = LogoutConfirmationFragment()
        logoutConfirmationDialog.setLogoutConfirmationListener(this)
        logoutConfirmationDialog.show(parentFragmentManager, "LogoutConfirmationDialog")
    }

    @SuppressLint("SetTextI18n")
    private fun getUserDataFromFirestore(username: String) {
        val userCollectionRef = FirebaseFirestore.getInstance().collection("users")

        userCollectionRef
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    val name = userDocument.getString("name")
                    val nim = userDocument.getString("nim")
                    with(binding){
                        profileName.text = "$name \n $nim"
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.d("TAG", "get failed with ", exception)
            }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        prefManager.setLoggedIn(false)
        prefManager.clear()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListTravelAdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListTravelAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}