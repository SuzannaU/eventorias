package parcours.android.eventorias.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import parcours.android.eventorias.MainViewModel
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.ImageRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.ui.DefaultDispatcherProvider
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.screen.add.AddEventViewModel
import parcours.android.eventorias.ui.screen.detail.DetailViewModel
import parcours.android.eventorias.ui.screen.list.ListViewModel
import parcours.android.eventorias.ui.screen.profile.ProfileViewModel

val appModule = module {
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
    single<FirebaseStorage> { FirebaseStorage.getInstance() }
    single<FirebaseMessaging> { FirebaseMessaging.getInstance() }

    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<UserRepository> { UserRepository(get(), get()) }
    single<EventRepository> { EventRepository(get(), get()) }
    single<ImageRepository> { ImageRepository() }

    viewModel { MainViewModel(get(), get()) }
    viewModel { ListViewModel(get(), get()) }
    viewModel { AddEventViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { (eventId: String) -> DetailViewModel(get(), eventId) }
}