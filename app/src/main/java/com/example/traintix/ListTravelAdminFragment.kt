package com.example.traintix

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traintix.databinding.FragmentListTravelAdminBinding
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
 * Use the [ListTravelAdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListTravelAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentListTravelAdminBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val travelsCollectionRef = firestore.collection("travel")
    private val travelListLiveData: MutableLiveData<List<Travel>> by lazy {
        MutableLiveData<List<Travel>>()
    }

    private lateinit var travelDao: TravelDAO

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
        binding = FragmentListTravelAdminBinding.inflate(inflater, container, false)
        val view = binding.root

        travelDao = TravelRoomDB.getDatabase(requireContext())?.travelDao()!!

        with(binding) {
            btnAddTravel.setOnClickListener {
                val action = ListTravelAdminFragmentDirections.actionListTravelAdminFragment2ToAddTravelFragment()
                findNavController().navigate(action)
            }

            btnSync.setOnClickListener {
                syncLocalDataWithFirebase()
            }
        }

        observeTravel()
        getAllTravels()
        return view
    }

    private fun getAllTravels() {
        observeTravelsChange()
    }

    override fun onResume() {
        super.onResume()
        getAllTravels()
    }

    private fun observeTravelsChange() {
        travelsCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity",
                    "Error Listening for travel changes:", error)
            }
            val travels = snapshots?.toObjects(Travel::class.java)
            if (travels != null) {
                travelListLiveData.postValue(travels)
            }
        }
    }

    //update adapter tiap livedata berubah
    private fun observeTravel() {
        travelListLiveData.observe(viewLifecycleOwner) { travels->
            val adapterTravel = TravelAdapter(travels) { travel ->
                val action = ListTravelAdminFragmentDirections.actionListTravelAdminFragment2ToEditTravelFragment()
                travelId = travel.id
                findNavController().navigate(action)
            }

            with(binding) {
                rvListTravel.apply{
                    adapter = adapterTravel
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
    }

    private fun addTravel(travel: Travel) {
        travelsCollectionRef.add(travel).addOnSuccessListener {
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

    private fun syncLocalDataWithFirebase() {
        travelDao.allTravels.observe(viewLifecycleOwner) { localTravels ->
            localTravels?.let {
                for (localTravel in it) {
                    val travel = Travel(
                        departure = localTravel.departure,
                        destination = localTravel.destination,
                        price = localTravel.price,
                        train = localTravel.train
                    )
                    addTravel(travel)
                    deleteTravelFromRoom(localTravel)
                }
            }
        }
    }

    private fun deleteTravelFromRoom(localTravel: TravelDB) {
        CoroutineScope(Dispatchers.IO).launch {
            travelDao.delete(localTravel)
        }
    }


    companion object {
        var travelId: String = ""
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