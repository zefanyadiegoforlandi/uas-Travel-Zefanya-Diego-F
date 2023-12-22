package com.example.traintix

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.traintix.databinding.ItemTravelBinding
import java.text.NumberFormat
import java.util.Locale

typealias OnClickTravel = (Travel) -> Unit
class TravelAdapter(
    private var listTravel: List<Travel>,
    private val onClickTravel: OnClickTravel) :
    RecyclerView.Adapter<TravelAdapter.ItemTravelViewHolder>() {

    inner class ItemTravelViewHolder(private val binding: ItemTravelBinding) :
        RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n", "ResourceAsColor")
            fun bind(travel: Travel) {
                with(binding) {
                    val formattedPrice = formatPrice(travel.price)
                    txtTrain.text = travel.train
                    txtDeparture.text = "Destination\n${travel.destination}"
                    txtDestination.text = "Departure\n${travel.departure}"
                    txtPrice.text = formattedPrice
                    itemView.setOnClickListener {
                        onClickTravel(travel)
                    }
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemTravelViewHolder {
        val binding = ItemTravelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemTravelViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listTravel.size
    }

    override fun onBindViewHolder(holder: ItemTravelViewHolder, position: Int) {
        holder.bind(listTravel[position])
    }

    private fun formatPrice(price: Int): String {
        // Use NumberFormat to format the price in IDR
        val localeID = Locale("id", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(price.toLong())
    }
}