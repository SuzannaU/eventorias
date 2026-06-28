package parcours.android.eventorias.domain.model

import androidx.annotation.StringRes
import parcours.android.eventorias.R
import java.util.Date

data class Event(
    val eventId: String = "",
    val authorId: String? = null,
    val title: String,
    val description: String? = null,
    val dateTime: Date? = null,
    val location: String? = null,
    val pictureUrl: String? = null,
    val category: Category = Category.OTHER,
)

enum class Category(@get:StringRes val labelRes: Int) {
    ART(R.string.category_art),
    TECH(R.string.category_tech),
    CHARITY(R.string.category_charity),
    SPORT(R.string.category_sport),
    FOOD(R.string.category_food),
    OTHER(R.string.category_other),
}