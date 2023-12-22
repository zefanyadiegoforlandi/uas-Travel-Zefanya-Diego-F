package com.example.traintix

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.traintix.databinding.FragmentAddTravelBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddTravelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class AddTravelFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentAddTravelBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val travelCollectionRef = firestore.collection("travel")

    private lateinit var travelDao: TravelDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

            travelDao = TravelRoomDB.getDatabase(requireContext())?.travelDao()!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddTravelBinding.inflate(inflater, container, false)
        val view = binding.root

        with(binding) {
            val train = resources.getStringArray(R.array.train)
            val adapterTrain = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, train)
            spinnerTrain.adapter = adapterTrain

            var selectedTrain = ""
            spinnerTrain.onItemSelectedListener =
                object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedTrain = train[position]
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
                }

            btnAddTravel.setOnClickListener {
                val departure = editTxtDeparture.text.toString().trim()
                val destination = editTxtDestination.text.toString().trim()
                val priceText = editTxtPrice.text.toString().trim()

                if (departure.isEmpty() || destination.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val price = priceText.toIntOrNull()
                    if (price == null) {
                        Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show()
                    } else {
                        val travel = Travel(
                            departure = departure,
                            destination = destination,
                            price = price,
                            train = selectedTrain
                        )

                        val travelDB = TravelDB(
                            departure = departure,
                            destination = destination,
                            price = price,
                            train = selectedTrain
                        )

                        if (isInternetAvailable(requireContext())) {
                            addTravel(travel)
                            Toast.makeText(requireContext(), "Travel added successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            addTravelToLocalDatabase(travelDB)
                            Toast.makeText(requireContext(), "No internet connection. Travel added locally.", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }

        }

        return view
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)?: false
        } else{
            @Suppress("DEPRECATION")
            val networkInfo: android.net.NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected?: false
        }
    }

    private fun addTravelToLocalDatabase(travel: TravelDB) {
        CoroutineScope(Dispatchers.IO).launch {
            travelDao.insert(travel)
        }
    }

    private fun addTravel(travel: Travel) {
        travelCollectionRef.add(travel).addOnSuccessListener {
                documentReference ->
            val createdTravelId = documentReference.id
            travel.id = createdTravelId
            documentReference.set(travel).addOnFailureListener {
                Log.d("MainActivity", "Error updating travel id : ", it)
            }
        }.addOnFailureListener {
            Log.d("MainActivity", "Error updating travel id : ", it)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddTravelFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddTravelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}