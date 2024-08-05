import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MiliLamaTeacherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

@Composable
fun MiliLamaTeacherAppContent() {
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    val userSettings: UserSettings = viewModel()
    val appState: AppState = viewModel()
    val coroutineScope = rememberCoroutineScope()

    if (authenticationViewModel.state == AuthenticationState.SignedIn) {
        ContentView(userSettings = userSettings, appState = appState, authenticationViewModel = authenticationViewModel)
    } else {
        ContentView(userSettings = userSettings, appState = appState, authenticationViewModel = authenticationViewModel)
    }
}

class AppState : androidx.lifecycle.ViewModel() {
    val returnToTitle = mutableStateOf(false)
}

@Composable
fun ContentView(userSettings: UserSettings, appState: AppState, authenticationViewModel: AuthenticationViewModel) {
    // Your ContentView implementation
}
