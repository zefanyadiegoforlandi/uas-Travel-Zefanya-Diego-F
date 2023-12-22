package com.example.traintix

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.traintix.MainActivity.Companion.viewPagers
import com.example.traintix.databinding.FragmentLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val userCollectionRef = firestore.collection("users")
    private var passwordVisible = false

    private lateinit var binding: FragmentLoginBinding
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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        prefManager = PrefManager.getInstance(requireContext())

        with(binding) {

            txtSignUp.setOnClickListener {
                viewPagers.currentItem = 0
            }

            btnShowPw.setOnClickListener {
                val currentInputType = editTxtPassword.inputType

                val newInputType = if (!passwordVisible) {
                    btnShowPw.setImageResource(R.drawable.baseline_visibility_off_24)
                    currentInputType and InputType.TYPE_TEXT_VARIATION_PASSWORD.inv()
                } else {
                    btnShowPw.setImageResource(R.drawable.baseline_visibility_24)
                    currentInputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

                editTxtPassword.inputType = newInputType
                editTxtPassword.setSelection(editTxtPassword.text?.length?: 0)

                passwordVisible = !passwordVisible
            }

            btnSignIn.setOnClickListener {
                val usernameInput = editTxtUsername.text.toString()
                val passwordInput = editTxtPassword.text.toString()

                if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                    if (usernameInput.isEmpty()) {
                        editTxtUsername.error = "Please fill out the blank field!"
                    }
                    if (passwordInput.isEmpty()) {
                        editTxtPassword.error = "Please fill out the blank field!"
                    }
                }
                else {
                    userCollectionRef
                        .whereEqualTo("username", usernameInput)
                        .whereEqualTo("password", passwordInput)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                editTxtUsername.error = "Username or password is wrong!"
                                editTxtPassword.error = "Username or password is wrong!"
                            }
                            else {
                                val userDocument = documents.documents[0]
                                val userRole = userDocument.getString("role")

                                if ("admin" == userRole) {
                                    // User is an admin
                                    prefManager.saveUsername(usernameInput)
                                    prefManager.savePassword(passwordInput)
                                    prefManager.saveRole(userRole)
                                    prefManager.setLoggedIn(true)
                                    val intent = Intent(activity, AdminActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    // User is a non-admin
                                    prefManager.saveUsername(usernameInput)
                                    prefManager.savePassword(passwordInput)
                                    prefManager.saveRole("user")
                                    prefManager.setLoggedIn(true)
                                    val intent = Intent(activity, NonAdminActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("TAG", "get failed with ", exception)
                        }
                }
            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}