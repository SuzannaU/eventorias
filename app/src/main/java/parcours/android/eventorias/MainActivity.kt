package parcours.android.eventorias

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import parcours.android.eventorias.ui.screen.add.AddEventScreen
import parcours.android.eventorias.ui.screen.add.AddEventViewModel
import parcours.android.eventorias.ui.screen.list.ListScreen
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
        //startSignInActivity()
        setContent {
            val navController = rememberNavController()

            EventoriasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EventoriasNavHost(
                        modifier = Modifier.padding(innerPadding),
                        navHostController = navController,
                    )
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
            .setAlwaysShowSignInMethodScreen(true)
            .setLogo(R.drawable.logo_eventorias)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            Log.d("TAG", "user signed in")
            //viewModel.createUser()
        } else if (response?.error != null) {
            Log.d("TAG", "Error while signing in: ${response.error?.message}")
        }
    }
}

@Composable
fun EventoriasNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
) {
    NavHost(
        navController = navHostController,
        startDestination = "eventList",
        modifier = modifier,
    ) {
        composable(route = "eventList") {
            ListScreen(
                viewModel = koinViewModel(),
                onAddClick = { navHostController.navigate("addEvent") }
            )
        }

        composable(route = "addEvent") {
            val addViewModel: AddEventViewModel = koinViewModel()
            AddEventScreen(
                viewModel = addViewModel,
                onBackClick = { navHostController.navigateUp() },
                onValidateClick = { },
            )
        }
    }
}
