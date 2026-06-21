package parcours.android.eventorias.domain.model

import java.util.Date

data class Event(
    val eventId: String = "",
    val author: User? = null,
    val title: String,
    val description: String? = null,
    val dateTime: Date? = null,
    val location: String? = null,
    val pictureUrl: String? = null,
    val category: Category = Category.OTHER,
)

enum class Category { ART, TECH, CHARITY, SPORT, FOOD, OTHER }