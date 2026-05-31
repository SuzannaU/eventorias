package parcours.android.eventorias.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.ui.DefaultDispatcherProvider
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.screen.add.AddEventViewModel
import parcours.android.eventorias.ui.screen.list.ListViewModel

val appModule = module {
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
    single<FirebaseStorage> { FirebaseStorage.getInstance() }

    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<UserRepository> { UserRepository(get(), get()) }
    single<EventRepository> { EventRepository(get(), get()) }

    viewModel { ListViewModel(get(), get()) }
    viewModel { AddEventViewModel(get(), get()) }
}