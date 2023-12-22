package com.example.traintix

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.traintix.MainActivity.Companion.viewPagers
import com.example.traintix.databinding.FragmentRegisterBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val userCollectionRef = firestore.collection("users")
    private var passwordVisible = false
    private var confirmPasswordVisible = false

    private lateinit var binding: FragmentRegisterBinding

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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        with(binding) {

            txtSignUp.setOnClickListener {
                viewPagers.currentItem = 1
            }

            // show date picker by clicking date picker icon
            btnDatePicker.setOnClickListener {
                showDatePickerDialog(editTxtDateOfBirth)
            }

            // show password by clicking show password icon
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

            btnShowPw2.setOnClickListener {
                val currentInputType = editTxtPassword.inputType

                val newInputType = if (!confirmPasswordVisible) {
                    btnShowPw2.setImageResource(R.drawable.baseline_visibility_off_24)
                    currentInputType and InputType.TYPE_TEXT_VARIATION_PASSWORD.inv()
                } else {
                    btnShowPw2.setImageResource(R.drawable.baseline_visibility_24)
                    currentInputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

                editTxtConfirmPassword.inputType = newInputType
                editTxtConfirmPassword.setSelection(editTxtPassword.text?.length?: 0)

                confirmPasswordVisible = !confirmPasswordVisible
            }

            btnSignUp.setOnClickListener {
                val usernameInput = editTxtUsername.text.toString()
                val nameInput = editTxtName.text.toString()
                val nimInput = editTxtNim.text.toString()
                val passwordInput = editTxtPassword.text.toString()
                val confirmPasswordInput = editTxtConfirmPassword.text.toString()
                val dateOfBirthInput = editTxtDateOfBirth.text.toString()

                // check if the field is empty
                if (usernameInput.isEmpty()) {
                    editTxtUsername.error = "Username is required"
                } else {
                    editTxtUsername.error = null
                }

                if (nameInput.isEmpty()) {
                    editTxtName.error = "Name is required"
                } else {
                    editTxtUsername.error = null
                }

                if (nimInput.isEmpty()) {
                    editTxtNim.error = "NIM is required"
                } else {
                    editTxtNim.error = null
                }

                if (passwordInput.isEmpty()) {
                    editTxtPassword.error = "Password is required"
                } else {
                    editTxtPassword.error = null
                }

                if (confirmPasswordInput.isEmpty()) {
                    editTxtConfirmPassword.error = "Confirm Password is required"
                } else {
                    if (confirmPasswordInput != passwordInput) {
                        editTxtConfirmPassword.error = "Password doesn't match"
                    } else {
                        editTxtConfirmPassword.error = null
                    }
                }

                if (dateOfBirthInput.isEmpty()) {
                    editTxtDateOfBirth.error = "Date of Birth is required"
                } else {
                    editTxtDateOfBirth.error = null
                }

                // if the field is not empty
                if (editTxtUsername.error == null && editTxtNim.error == null &&
                    editTxtPassword.error == null && editTxtDateOfBirth.error == null
                ) {
                    val age = Calendar.getInstance().get(Calendar.YEAR) - dateOfBirthInput
                        .split("/")[2].toInt()

                    // check if the age is at least 15 years old
                    if (age < 15) {
                        editTxtDateOfBirth.error = "You must at least 15 years old"
                        return@setOnClickListener
                    }else{
                        val createUser = User(
                            username = usernameInput,
                            name = nameInput,
                            nim = nimInput,
                            dateOfBirth = dateOfBirthInput,
                            password = passwordInput
                        )
                        addUser(createUser)
                        setEmptyField()
                        viewPagers.currentItem = 1
                    }
                }
            }

        }
        return view
    }

    private fun addUser(user: User) {
        userCollectionRef.add(user).addOnSuccessListener {
                documentReference ->
            val createdUserId = documentReference.id
            user.id = createdUserId
            documentReference.set(user).addOnFailureListener {
                Log.d("MainActivity", "Error updating user id : ", it)
            }
        }.addOnFailureListener {
            Log.d("MainActivity", "Error updating user id : ", it)
        }
    }

    private fun setEmptyField() {
        with(binding) {
            editTxtUsername.setText("")
            editTxtName.setText("")
            editTxtNim.setText("")
            editTxtPassword.setText("")
            editTxtDateOfBirth.setText("")
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) - 15
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog( requireContext(), {
                _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.format(selectedDate.time)
            editText.setText(date)
        },
            year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}