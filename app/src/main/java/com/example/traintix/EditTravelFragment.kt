package com.example.traintix

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.traintix.databinding.FragmentEditTravelBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditTravelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditTravelFragment : Fragment(), DeleteConfirmationFragment.DeleteConfirmationListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentEditTravelBinding
    private var updateId = ""
    private val firestore = FirebaseFirestore.getInstance()
    private val travelCollectionRef = firestore.collection("travel")

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
        binding = FragmentEditTravelBinding.inflate(inflater, container, false)
        val view = binding.root

        if(ListTravelAdminFragment.travelId.isNotEmpty()) {
            getTravelDataFromFirestore(ListTravelAdminFragment.travelId)
        }

        with(binding) {
            val train = resources.getStringArray(R.array.train)
            val adapterTrain = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, train)
            spinnerTrain.adapter = adapterTrain
        }

        return view
    }

    private fun getTravelDataFromFirestore(travelId: String) {
        val travelCollectionRef = FirebaseFirestore.getInstance().collection("travel")
        val travelDocumentRef = travelCollectionRef.document(travelId)

        travelDocumentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val travelDocument = documentSnapshot.data
                    val departure = travelDocument?.get("departure") as? String
                    val destination = travelDocument?.get("destination") as? String
                    val price = travelDocument?.get("price") as? Long
                    val train = travelDocument?.get("train") as? String

                    with(binding) {
                        editTxtDeparture.setText(departure)
                        editTxtDestination.setText(destination)
                        editTxtPrice.setText(price.toString())

                        spinnerTrain.setSelection(getIndex(spinnerTrain, train.toString()))

                        updateId = travelId

                        val trains = resources.getStringArray(R.array.train)
                        val adapterTrains = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            trains
                        )
                        spinnerTrain.adapter = adapterTrains
                        val trainPosition = trains.indexOf(train)
                        spinnerTrain.setSelection(trainPosition)

                        var updatedTrain = ""
                        spinnerTrain.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    updatedTrain = trains[position]
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                    TODO("Not yet implemented")
                                }
                            }
                        btnUpdate.setOnClickListener {
                            val updatedDeparture = editTxtDeparture.text.toString()
                            val updatedDestination = editTxtDestination.text.toString()
                            val updatedPrice = editTxtPrice.text.toString().toInt()

                            Log.d("TAG", "Updated train: $updatedTrain")

                            val editedTravel = Travel(
                                departure = updatedDeparture,
                                destination = updatedDestination,
                                price = updatedPrice,
                                train = updatedTrain
                            )
                            updateTravel(editedTravel)
                            Toast.makeText(
                                requireContext(),
                                "Travel edited successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }

                        btnDelete.setOnClickListener {
                            showDeleteConfirmationDialog()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.d("TAG", "get failed with ", exception)
            }

    }

    private fun getIndex(spinnerTrain: AppCompatSpinner, toString: String): Int {
        for (i in 0 until spinnerTrain.count) {
            if (spinnerTrain.getItemAtPosition(i).toString() == toString) {
                return i
            }
        }
        return 0
    }

    private fun updateTravel(travel: Travel) {
        travel.id = updateId
        travelCollectionRef.document(updateId).set(travel).addOnFailureListener {
            Log.d("TAG", "Error updating travel", it)
        }
    }

    private fun showDeleteConfirmationDialog() {
        val deleteConfirmationDialog = DeleteConfirmationFragment()
        deleteConfirmationDialog.setDeleteConfirmationListener(this)
        deleteConfirmationDialog.show(parentFragmentManager, "DeleteConfirmationDialog")
    }

    private fun deleteTravel(travelId: String) {
        val documentReference = travelCollectionRef.document(travelId)

        documentReference
            .delete()
            .addOnSuccessListener {
                Log.d("TAG", "Document deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error deleting document", e)
            }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditTravelFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditTravelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        deleteTravel(updateId)
        Toast.makeText(
            requireContext(),
            "Travel deleted successfully",
            Toast.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }
}