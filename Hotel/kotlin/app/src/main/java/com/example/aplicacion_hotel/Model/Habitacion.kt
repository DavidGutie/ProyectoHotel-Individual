package com.example.aplicacion_hotel.Model

data class Amenity(
    val _id: String = "",
    val name: String = "",
    val icon: String = "",
    val category: String = ""
)

data class PetPolicy(
    val habitacionId: String = "",
    val numero: Int = 0,
    val tipo: String = "",
    val pets_allowed: Boolean = false,
    val pet_supplement_per_night: Double = 0.0,
    val aceptaMascotas: Boolean = false,
    val politicaMascotas: String = "",
    val suplementoMascota: Double = 0.0,
    val maxMascotas: Int = 0
) {
    val admiteMascotas: Boolean
        get() = pets_allowed || aceptaMascotas

    val suplementoMascotaNoche: Double
        get() = listOf(pet_supplement_per_night, suplementoMascota).firstOrNull { it > 0 } ?: 0.0
}

data class Habitacion(
    val _id: String,
    val numero: Int,
    val tipo: String,
    val descripcion: String,
    val imagen: String = "",
    val imagenes: List<String> = emptyList(),
    val precionoche: Double,
    val rate: Double,
    val max_ocupantes: Int,
    val disponible: Boolean,
    val oferta: Boolean,
    val amenities: List<Amenity> = emptyList(),
    val pets_allowed: Boolean = false,
    val pet_supplement_per_night: Double = 0.0,
    val aceptaMascotas: Boolean = false,
    val politicaMascotas: String = "",
    val suplementoMascota: Double = 0.0,
    val maxMascotas: Int = 0,
    val servicios: List<String> = emptyList()
) {
    val admiteMascotas: Boolean
        get() = pets_allowed || aceptaMascotas

    val suplementoMascotaNoche: Double
        get() = listOf(pet_supplement_per_night, suplementoMascota).firstOrNull { it > 0 } ?: 0.0
}
