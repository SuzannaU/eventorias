package parcours.android.eventorias.ui

import com.google.firebase.Timestamp
import java.text.DateFormat
import java.util.Locale



fun Timestamp.formatEventDate(): String {
    val locale = Locale.getDefault()
    val date = this.toDate()

    val formatter = DateFormat.getDateInstance(DateFormat.LONG, locale)
    return formatter.format(date)
        //.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

//fun formatEventDate(timestamp: Timestamp?): String? {
//    if (timestamp == null) return null
//    val locale = Locale.getDefault()
//    val date = timestamp.toDate()
//
//    val formatter = DateFormat.getDateInstance(DateFormat.LONG, locale)
//    return formatter.format(date)
//        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
//}

fun Timestamp.formatEventTime(): String {
    val locale = Locale.getDefault()
    val date = this.toDate()

    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    return formatter.format(date)
}