package com.example.traintix

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.traintix.databinding.ItemOrderBinding
import com.google.firebase.firestore.FirebaseFirestore

class OrderAdapter (private var listOrder: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.ItemOrderViewHolder>() {

    inner class ItemOrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "ResourceAsColor")
        fun bind(order: Order) {
            with(binding) {
                getTravelDataFromFirestore(order.travelID) { travel ->
                    txtDeparture.text = "Destination\n${travel.destination}"
                    txtDestination.text = "Departure\n${travel.departure}"
                    txtTrain.text = "${travel.train} ${order.trainClass}"
                }
                txtPassengerName.text = order.passengerName
                txtAdditionalFacilities.text = order.additionalFacilities
                txtDate.text = order.date
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemOrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemOrderViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listOrder.size
    }

    override fun onBindViewHolder(holder: ItemOrderViewHolder, position: Int) {
        holder.bind(listOrder[position])
    }

    private fun getTravelDataFromFirestore(travelId: String, callback: (Travel) -> Unit) {
        val travelCollectionRef = FirebaseFirestore.getInstance().collection("travel")

        travelCollectionRef
            .whereEqualTo("id", travelId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val travelDocument = documents.documents[0]
                    val departure = travelDocument.getString("departure") ?: ""
                    val destination = travelDocument.getString("destination") ?: ""
                    val train = travelDocument.getString("train") ?: ""

                    val travel = Travel(travelId, departure, destination, train)

                    callback(travel)
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.d("TAG", "getTravelDataFromFirestore failed with ", exception)
                callback(Travel("", "", "", ""))
            }
    }

}