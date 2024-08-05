import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirebaseManager private constructor(application: Application) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var currentUser: Teacher? = null

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(application: Application): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager(application).also { INSTANCE = it }
            }
        }
    }
}

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase Manager
        FirebaseManager.getInstance(this)
    }
}
