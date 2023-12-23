package com.example.movierecommender

data class Movie(
    val index: Int,
    val title: String,
    val overview: String,
    val fullPosterPath: String,
    val explanation: String? = null
)
