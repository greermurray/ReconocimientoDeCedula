package com.rodelag.tecnologia.cedulaprueba.comun

fun String.esUnaCedula(): Boolean {
    //INFO: Expressión regular para validar cédula de extranjeros y nacionales, ejemplo: E-8-207322 o 8-890-582
    val patronCedula = Regex("[A-Za-z]?-?\\d{1,4}-?\\d{1,4}-?\\d{1,4}")
    return patronCedula.matches(this)
}