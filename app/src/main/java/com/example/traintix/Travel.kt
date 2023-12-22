package com.example.traintix

import java.io.Serializable

data class Travel(
    var id: String = "",
    var departure: String = "",
    var destination: String = "",
    var train: String = "",
    var price: Int = 0
): Serializable
