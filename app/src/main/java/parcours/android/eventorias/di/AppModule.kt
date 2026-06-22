package parcours.android.eventorias.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import parcours.android.eventorias.data.datasource.EventDataSource
import parcours.android.eventorias.data.datasource.FirebaseEventDataSource
import parcours.android.eventorias.data.datasource.FirebaseUserDataSource
import parcours.android.eventorias.data.datasource.UserDataSource
import parcours.android.eventorias.data.repository.FirebaseEventRepository
import parcours.android.eventorias.data.repository.FirebaseUserRepository
import parcours.android.eventorias.data.repository.ImageRepositoryImpl
import parcours.android.eventorias.data.service.FcmNotificationService
import parcours.android.eventorias.data.service.FirebaseAuthService
import parcours.android.eventorias.data.service.GeocoderService
import parcours.android.eventorias.data.service.LocationService
import parcours.android.eventorias.domain.repository.EventRepository
import parcours.android.eventorias.domain.repository.ImageRepository
import parcours.android.eventorias.domain.repository.UserRepository
import parcours.android.eventorias.domain.service.AuthService
import parcours.android.eventorias.domain.service.NotificationService
import parcours.android.eventorias.ui.DefaultDispatcherProvider
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.MainViewModel
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
    single<UserDataSource> { FirebaseUserDataSource(get(), get()) }
    single<AuthService> { FirebaseAuthService(get()) }
    single<LocationService> { GeocoderService(get()) }
    single<UserRepository> { FirebaseUserRepository(get()) }
    single<EventDataSource> { FirebaseEventDataSource(get(), get()) }
    single<EventRepository> { FirebaseEventRepository(get(), get()) }
    single<ImageRepository> { ImageRepositoryImpl() }
    single<NotificationService> { FcmNotificationService(get()) }

    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { ListViewModel(get(), get()) }
    viewModel { AddEventViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { (eventId: String) -> DetailViewModel(get(), get(), eventId) }
}