    package com.example.traintix

    import android.os.Bundle
    import android.util.Log
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.lifecycle.MutableLiveData
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.traintix.databinding.FragmentHistoryBinding
    import com.google.firebase.firestore.FirebaseFirestore

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private const val ARG_PARAM1 = "param1"
    private const val ARG_PARAM2 = "param2"

    /**
     * A simple [Fragment] subclass.
     * Use the [HistoryFragment.newInstance] factory method to
     * create an instance of this fragment.
     */
    class HistoryFragment : Fragment() {
        // TODO: Rename and change types of parameters
        private var param1: String? = null
        private var param2: String? = null

        private lateinit var binding: FragmentHistoryBinding
        private val firestore = FirebaseFirestore.getInstance()
        private val orderCollectionRef = firestore.collection("orders")
        private val orderListLiveData: MutableLiveData<List<Order>> by lazy {
            MutableLiveData<List<Order>>()
        }
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
            binding = FragmentHistoryBinding.inflate(inflater, container, false)
            val view = binding.root
            prefManager = PrefManager.getInstance(requireContext())

            observeOrder()
            getAllOrders()

            with(binding) {
                datePicker.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    checkTrip(formattedDate)
                }
            }

            return view
        }

        private fun getAllOrders() {
            val username = prefManager.getUsername()
            getUserDataFromFirestore(username) { userId ->
                observeOrdersChange(userId)
            }
        }

        override fun onResume() {
            super.onResume()
            getAllOrders()
        }

        private fun observeOrdersChange(userId: String) {
            orderCollectionRef.whereEqualTo("userID", userId)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.d("MainActivity", "Error Listening for order changes:", error)
                    }
                    val orders = snapshots?.toObjects(Order::class.java)
                    if (orders != null) {
                        orderListLiveData.postValue(orders)
                    }
                }
        }

        //update adapter tiap livedata berubah
        private fun observeOrder() {
            orderListLiveData.observe(viewLifecycleOwner) { orders ->
                val adapterTravel = OrderAdapter(orders)

                with(binding) {
                    rvListOrder.apply {
                        adapter = adapterTravel
                        layoutManager = LinearLayoutManager(requireContext())
                    }
                }
            }
        }

        private fun getUserDataFromFirestore(username: String, callback: (String) -> Unit) {
            val userCollectionRef = FirebaseFirestore.getInstance().collection("users")

            userCollectionRef
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDocument = documents.documents[0]
                        val userId = userDocument.getString("id").toString()
                        callback(userId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                    callback("")
                }
        }

        private fun checkTrip(selectedDate: String) {
            val username = prefManager.getUsername()
            getUserDataFromFirestore(username) { userId ->
                orderCollectionRef
                    .whereEqualTo("userID", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d("TAG", "Checking trip for date: $selectedDate")

                        var foundTrip = false

                        for (document in documents) {
                            val documentDate = document.getString("date")
                            Log.d("TAG", "Document Date: $documentDate")

                            if (documentDate == selectedDate) {
                                foundTrip = true
                                break
                            }
                        }

                        val message = if (foundTrip) {
                            "You have a trip on this day"
                        } else {
                            "You don't have a trip on this day"
                        }

                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "get failed with ", exception)
                    }
            }
        }


        companion object {
            /**
             * Use this factory method to create a new instance of
             * this fragment using the provided parameters.
             *
             * @param param1 Parameter 1.
             * @param param2 Parameter 2.
             * @return A new instance of fragment HistoryFragment.
             */
            // TODO: Rename and change types and number of parameters
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                HistoryFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }
    }