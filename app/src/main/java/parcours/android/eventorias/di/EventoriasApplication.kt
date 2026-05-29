package parcours.android.eventorias.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EventoriasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EventoriasApplication)
            modules(appModule)
        }
    }
}