import android.app.Application
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {

    val signInWithGoogleSuccess = mutableStateOf(false)
    val state = mutableStateOf(SignInState.SignedOut)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(application, options)

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                state.value = SignInState.SignedIn
            } else {
                state.value = SignInState.SignedOut
            }
        }
    }

    enum class SignInState {
        SignedIn,
        SignedOut
    }

    fun authenticateUser(account: GoogleSignInAccount?, error: Exception?) {
        if (error != null) {
            println(error.localizedMessage)
            return
        }

        val idToken = account?.idToken ?: return
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                state.value = SignInState.SignedIn
                signInWithGoogleSuccess.value = true
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        }
    }

    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                auth.signOut()
                state.value = SignInState.SignedOut
            } else {
                println("Failed to sign out from Google")
            }
        }
    }

    fun signIn(activity: MainActivity) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN)
    }

    fun handleSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                authenticateUser(account, null)
            } catch (e: ApiException) {
                authenticateUser(null, e)
            }
        }
    }
}
