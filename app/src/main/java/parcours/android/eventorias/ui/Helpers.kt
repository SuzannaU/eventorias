package parcours.android.eventorias.ui

import parcours.android.eventorias.BuildConfig
import java.net.URLEncoder
import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun Date.formatEventDate(): String {
    val locale = Locale.getDefault()

    val formatter = DateFormat.getDateInstance(DateFormat.LONG, locale)
    return formatter.format(this)
}

fun Date.formatEventTime(): String {
    val locale = Locale.getDefault()

    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    return formatter.format(this)
}

fun getStaticMapUrl(location: String?) : String? {
    if (location.isNullOrBlank()) return null

    val apiKey = BuildConfig.MAPS_API_KEY
    val encodedLocation = URLEncoder.encode(location, "UTF-8")

    return "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=$encodedLocation" +
            "&zoom=15" +
            "&size=400x200" +
            "&markers=color:red%7C$encodedLocation" +
            "&key=$apiKey"
}