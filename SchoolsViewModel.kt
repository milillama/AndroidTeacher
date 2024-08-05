import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SchoolsViewModel : ViewModel() {

    val schools = mutableStateOf(listOf<School>())
    val errorMessage = mutableStateOf("")
    val count = mutableStateOf(0)
    val isUserCurrentlyLoggedOut = mutableStateOf(false)
    private var firestoreListener: ListenerRegistration? = null

    init {
        CoroutineScope(Dispatchers.Main).launch {
            isUserCurrentlyLoggedOut.value = FirebaseAuth.getInstance().currentUser?.uid == null
        }

        fetchSchools()
    }

    private fun fetchSchools() {
        // Clear out the listener
        firestoreListener?.remove()
        schools.value = listOf()

        firestoreListener = Firebase.firestore.collection("Schools")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    errorMessage.value = "Error fetching schools: ${error.message}"
                    return@addSnapshotListener
                }

                val documents = querySnapshot?.documents ?: return@addSnapshotListener
                val fetchedSchools = documents.map { queryDocumentSnapshot ->
                    val data = queryDocumentSnapshot.data ?: emptyMap<String, Any>()

                    School(
                        documentId = queryDocumentSnapshot.id,
                        schoolName = data["schoolName"] as? String ?: "",
                        schoolLogo = data["schoolLogo"] as? String ?: "",
                        address = data["address"] as? String ?: "",
                        city = data["city"] as? String ?: "",
                        state = data["state"] as? String ?: "",
                        zip = data["zip"] as? String ?: "",
                        phoneNumber = data["phoneNumber"] as? String ?: "",
                        emailAddress = data["emailAddress"] as? String ?: "",
                        rating = data["rating"] as? Double ?: 0.0,
                        website = data["website"] as? String ?: "",
                        district = data["district"] as? String ?: "",
                        phoneNumber2 = data["phoneNumber2"] as? String ?: "",
                        emailAddress2 = data["emailAddress2"] as? String ?: "",
                        pointOfContact = data["pointOfContact"] as? String ?: "",
                        pointOfContact2 = data["pointOfContact2"] as? String ?: "",
                        funFact = data["funFact"] as? String ?: "",
                        domain = data["domain"] as? String ?: ""
                    )
                }
                schools.value = fetchedSchools
            }
    }

    fun createANewSchool(
        schoolName: String, schoolLogo: String, address: String, city: String,
        state: String, zip: String, phoneNumber: String, emailAddress: String,
        website: String, district: String, phoneNumber2: String, emailAddress2: String,
        pointOfContact: String, pointOfContact2: String, funFact: String, domain: String
    ) {
        val schoolData = mapOf(
            "schoolName" to schoolName,
            "schoolLogo" to schoolLogo,
            "address" to address,
            "city" to city,
            "state" to state,
            "zip" to zip,
            "phoneNumber" to phoneNumber,
            "emailAddress" to emailAddress,
            "rating" to 0.0,
            "website" to website,
            "district" to district,
            "phoneNumber2" to phoneNumber2,
            "emailAddress2" to emailAddress2,
            "pointOfContact" to pointOfContact,
            "pointOfContact2" to pointOfContact2,
            "funFact" to funFact,
            "domain" to domain
        )

        Firebase.firestore.collection("Schools").document().set(schoolData)
    }

    // MARK: Fetch school by domain
    fun fetchSchoolByDomain(domain: String, completion: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = Firebase.firestore
                    .collection("Schools")
                    .whereEqualTo("domain", domain)
                    .get()
                    .await()

                val document = querySnapshot.documents.firstOrNull()
                completion(document?.id)
            } catch (e: Exception) {
                errorMessage.value = "Error fetching school by domain: ${e.localizedMessage}"
                completion(null)
            }
        }
    }
}
