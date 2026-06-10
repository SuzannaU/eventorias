package parcours.android.eventorias

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import parcours.android.eventorias.ui.screen.ADD_ROUTE
import parcours.android.eventorias.ui.screen.LIST_ROUTE
import parcours.android.eventorias.ui.screen.PROFILE_ROUTE
import parcours.android.eventorias.ui.screen.add.AddEventScreen
import parcours.android.eventorias.ui.screen.error.ErrorScreen
import parcours.android.eventorias.ui.screen.list.ListScreen
import parcours.android.eventorias.ui.screen.profile.ProfileScreen
import parcours.android.eventorias.ui.theme.EventoriasTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
        ::onSignInResult,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val userAuthState by viewModel.userAuthState.collectAsStateWithLifecycle()
            val authNetworkState by viewModel.authNetworkState.collectAsStateWithLifecycle()
            EventoriasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!authNetworkState.isAuthConnected) {
                        ErrorScreen(
                            errorMessage = stringResource(R.string.connexion_problem),
                            onRetry = {
                                startSignInActivity()
                            }
                        )
                    } else if (userAuthState.isUserAuthenticated) {
                        EventoriasNavHost(
                            navHostController = navController,
                            onFilterClick = { viewModel.signOut() },
                        )
                    } else {
                        startSignInActivity()
                    }
                }
            }
        }
    }

    private fun startSignInActivity() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.Theme_Eventorias_Login)
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo_eventorias)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            Log.i("TAG", "user signed in")
            viewModel.createUser()
        } else if (response?.error != null) {
            Log.i("TAG", "Error while signing in: ${response.error?.message}")
        }
    }
}

@Composable
fun EventoriasNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    onFilterClick: () -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = LIST_ROUTE,
        modifier = modifier,
    ) {

        composable(route = LIST_ROUTE) {
            ListScreen(
                viewModel = koinViewModel(),
                onAddClick = { navHostController.navigate(ADD_ROUTE) },
                onFilterClick = onFilterClick,
                onProfileClick = { navHostController.navigate(PROFILE_ROUTE) }
            )
        }

        composable(route = ADD_ROUTE) {
            AddEventScreen(
                viewModel = koinViewModel(),
                onBackClick = { navHostController.navigateUp() },
                onSaveSuccessful = { navHostController.navigate(LIST_ROUTE) }
            )
        }

        composable(route = PROFILE_ROUTE) {
            ProfileScreen(
                viewModel = koinViewModel(),
                onEventsClick = { navHostController.navigate(LIST_ROUTE) }
            )
        }
    }
}
