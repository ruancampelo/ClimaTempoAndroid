package br.infnet.climatempoandroid.model

data class TempoModel(

    val base: String,
    val clouds: Nuvens,
    val cod: Int,
    val coord: Coordenadas,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val rain: Chuva,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Tempo>,
    val wind: Vento
)
