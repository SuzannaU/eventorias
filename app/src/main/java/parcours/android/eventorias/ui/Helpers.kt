package parcours.android.eventorias.ui

import androidx.annotation.StringRes
import parcours.android.eventorias.BuildConfig
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.model.Category
import java.net.URLEncoder
import java.text.DateFormat
import java.util.Date
import java.util.Locale

val Category.labelRes: Int
    @StringRes
    get() = when (this) {
        Category.ART -> R.string.category_art
        Category.TECH -> R.string.category_tech
        Category.CHARITY -> R.string.category_charity
        Category.SPORT -> R.string.category_sport
        Category.FOOD -> R.string.category_food
        Category.OTHER -> R.string.category_other
    }

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