package com.example.movierecommender

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val fullPosterPath: String,
    val explanation: String? = null
)
