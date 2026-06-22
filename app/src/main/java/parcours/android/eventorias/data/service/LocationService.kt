package parcours.android.eventorias.data.service

interface LocationService {

    suspend fun getEventCoordinates(addressString: String): Pair<Double, Double>?
    suspend fun getEventAddress(coordinates: Pair<Double, Double>?): String?
}