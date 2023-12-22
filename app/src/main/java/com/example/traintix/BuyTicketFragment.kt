package com.example.traintix

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.fragment.findNavController
import com.example.traintix.databinding.FragmentBuyTicketBinding
import com.example.traintix.databinding.FragmentHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BuyTicketFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BuyTicketFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentBuyTicketBinding
    private lateinit var prefManager: PrefManager
    private val firestore = FirebaseFirestore.getInstance()
    private val orderCollectionRef = firestore.collection("orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBuyTicketBinding.inflate(inflater, container, false)
        val view = binding.root

        prefManager = PrefManager.getInstance(requireContext())
        val username = prefManager.getUsername()

        with(binding) {
            val classes = resources.getStringArray(R.array.classes)
            val adapterClass = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, classes)
            spinnerClass.adapter = adapterClass

            var ticketPrice = 0
            if (ListTravelFragment.travelId.isNotEmpty()) {
                getTravelDataFromFirestore(ListTravelFragment.travelId) { receivedPrice ->
                    ticketPrice = receivedPrice
                }
            }

            btnDatePicker.setOnClickListener {
                showDatePickerDialog(editTxtDate)
            }

            txtTotalPrice.text = formatPrice(ticketPrice)

            var classPrice = ticketPrice
            var selectedClass = ""
            spinnerClass.onItemSelectedListener =
                object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedClass = classes[position]
                        when (selectedClass) {
                            "Economy" -> {
                                classPrice = ticketPrice
                            }
                            "Business" -> {
                                classPrice = ticketPrice + 30000
                            }
                            "Executive" -> {
                                classPrice = ticketPrice + 100000
                            }
                            "Luxury" -> {
                                classPrice = ticketPrice + 200000
                            }
                        }
                        txtTotalPrice.text = formatPrice(classPrice)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
                }

            var currentPrice: Int
            var additionalFacilities = ""
            btnPaketSatu.setOnClickListener {
                currentPrice = classPrice + 20000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_satu)
                additionalFacilities = getString(R.string.paket_satu)
            }
            btnPaketDua.setOnClickListener {
                currentPrice = classPrice + 12000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_dua)
                additionalFacilities = getString(R.string.paket_dua)
            }
            btnPaketTiga.setOnClickListener {
                currentPrice = classPrice + 10000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_tiga)
                additionalFacilities = getString(R.string.paket_tiga)
            }
            btnPaketEmpat.setOnClickListener {
                currentPrice = classPrice + 25000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_empat)
                additionalFacilities = getString(R.string.paket_empat)
            }
            btnPaketLima.setOnClickListener {
                currentPrice = classPrice + 15000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_lima)
                additionalFacilities = getString(R.string.paket_lima)
            }
            btnPaketEnam.setOnClickListener {
                currentPrice = classPrice + 45000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_enam)
                additionalFacilities = getString(R.string.paket_enam)
            }
            btnPaketTujuh.setOnClickListener {
                currentPrice = classPrice + 50000
                txtTotalPrice.text = formatPrice(currentPrice)
                txtPaket.text = getString(R.string.paket_tujuh)
                additionalFacilities = getString(R.string.paket_tujuh)
            }

            btnBook.setOnClickListener {
                val passengerName = editTxtPassengerName.text.toString().trim()
                val selectedDate = editTxtDate.text.toString().trim()

                if (passengerName.isEmpty() || selectedDate.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    getUserDataFromFirestore(username) { userId ->
                        val order = Order(
                            userID = userId,
                            travelID = ListTravelFragment.travelId,
                            trainClass = selectedClass,
                            date = selectedDate,
                            passengerName = passengerName,
                            additionalFacilities = additionalFacilities
                        )
                        bookTicket(order)
                        val action = BuyTicketFragmentDirections.actionBuyTicketFragmentToHistoryFragment2()
                        findNavController().navigate(action)
                    }
                }
            }

        }

        return view
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            if (selectedDate.before(calendar)) {
                Toast.makeText(requireContext(), "Please choose a date from today onwards", Toast.LENGTH_SHORT).show()
            } else {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.format(selectedDate.time)
                editText.setText(date)
            }
        }, year, month, dayOfMonth)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 //
        datePickerDialog.show()
    }


    @SuppressLint("SetTextI18n")
    private fun getTravelDataFromFirestore(travelId: String, callback: (Int) -> Unit) {
        val travelCollectionRef = FirebaseFirestore.getInstance().collection("travel")

        travelCollectionRef
            .whereEqualTo("id", travelId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val travelDocument = documents.documents[0]
                    val departure = travelDocument.getString("departure")
                    val destination = travelDocument.getString("destination")
                    val price = travelDocument.getLong("price")?.toInt() ?: 0
                    val train = travelDocument.getString("train")
                    val formattedPrice = formatPrice(price)

                    with(binding){
                        txtDeparture.text = "Departure \n$departure"
                        txtDestination.text = "Destination \n$destination"
                        txtPrice.text = formattedPrice
                        txtTrain.text = train

                        txtTotalPrice.text = formattedPrice
                    }
                    callback(price)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
                callback(0)
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

    private fun bookTicket(order: Order) {
        orderCollectionRef.add(order).addOnSuccessListener { documentReference ->
            val createdTravelId = documentReference.id
            order.id = createdTravelId
            documentReference.set(order).addOnFailureListener {
                Log.d("MainActivity", "Error updating travel id : ", it)
            }

            showNotification("Yay! Enjoy your trip <3", "Your ticket has been booked successfully. Don't forget to check your ticket in Your Orders menu!")
        }.addOnFailureListener {
            Log.d("MainActivity", "Error updating travel id : ", it)
        }
    }

    private fun showNotification(title: String, message: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(requireContext(), FragmentHistoryBinding::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(requireContext(), "my_channel_id")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling ActivityCompat#requestPermissions
                return
            }
            notify(1, notificationBuilder.build())
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BuyTicketFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BuyTicketFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun formatPrice(price: Int): String {
        val localeID = Locale("id", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(price.toLong())
    }
}