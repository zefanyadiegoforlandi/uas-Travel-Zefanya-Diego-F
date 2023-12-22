package com.example.traintix

data class Order(
    var id: String = "",
    var userID: String = "",
    var travelID: String = "",
    var passengerName: String = "",
    var date: String = "",
    var trainClass: String = "",
    var additionalFacilities: String = ""
)
