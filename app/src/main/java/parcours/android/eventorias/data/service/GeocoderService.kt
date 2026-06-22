package parcours.android.eventorias.data.service


import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class GeocoderService(
    private val context: Context,
) : LocationService {

    override suspend fun getEventCoordinates(addressString: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {

            val geocoder = Geocoder(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->

                    geocoder.getFromLocationName(
                        addressString,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val coords = if (addresses.isNotEmpty()) {
                                    Pair(
                                        addresses[0].latitude,
                                        addresses[0].longitude
                                    )
                                } else null
                                Log.d("TAG", "coordinates from geocoder: $coords")
                                continuation.resume(coords)
                            }


                            override fun onError(errorMessage: String?) {
                                super.onError(errorMessage)

                            }

                        }
                    )
                }
            } else {
                val addresses = geocoder.getFromLocationName(addressString, 1)
                addresses?.firstOrNull()?.let { Pair(it.latitude, it.longitude) }
            }
        }
    }

    override suspend fun getEventAddress(coordinates: Pair<Double, Double>?): String? {
        if (coordinates == null) return null
        return withContext(Dispatchers.IO) {

            val geocoder = Geocoder(context)

            val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(
                        coordinates.first,
                        coordinates.second,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                continuation.resume(addresses.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                super.onError(errorMessage)

                            }

                        }
                    )
                }
            } else {
                geocoder.getFromLocation(coordinates.first, coordinates.second, 1)?.firstOrNull()
            }

            address?.let {
                val sb = StringBuilder()
                it.subThoroughfare?.let { street -> sb.append(street).append(" ") }
                it.thoroughfare?.let { street -> sb.append(street).append(" ,") }
                it.locality?.let { city -> sb.append(city).append(" ") }
                it.postalCode?.let { zip -> sb.append(zip) }
                sb.toString().trim().ifEmpty { null }
            }
        }
    }
}