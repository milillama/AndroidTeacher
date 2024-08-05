import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class AssignmentsViewModel : ViewModel() {

    val assignments = mutableStateOf(listOf<Assignment>())
    val errorMessage = mutableStateOf("")
    val count = mutableStateOf(0)
    val isUserCurrentlyLoggedOut = mutableStateOf(false)
    private var firestoreListener: ListenerRegistration? = null

    init {
        viewModelScope.launch {
            isUserCurrentlyLoggedOut.value = FirebaseAuth.getInstance().currentUser?.uid == null
        }

        fetchAssignments()
    }

    private fun fetchAssignments() {
        // Clear out the listener
        firestoreListener?.remove()
        assignments.value = listOf()

        firestoreListener = Firebase.firestore.collection("Assignments")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    errorMessage.value = "Error fetching assignments: ${error.message}"
                    return@addSnapshotListener
                }

                val documents = querySnapshot?.documents ?: return@addSnapshotListener
                val fetchedAssignments = documents.mapNotNull { queryDocumentSnapshot ->
                    val data = queryDocumentSnapshot.data ?: return@mapNotNull null

                    val documentId = queryDocumentSnapshot.id
                    val date = (data["date"] as? Timestamp)?.toDate() ?: Date()
                    val classID = data["classID"] as? String ?: ""
                    val rate = data["rate"] as? Double ?: 0.0
                    val additionalNotes = data["additionalNotes"] as? String ?: ""
                    val assignedTo = data["assignedTo"] as? String ?: ""
                    val assignedToUID = data["assignedToUID"] as? String ?: ""
                    val isAvailable = data["isAvailable"] as? Boolean ?: false
                    val inProgress = data["inProgress"] as? Boolean ?: false
                    val completed = data["completed"] as? Boolean ?: false
                    val attachments = data["attachments"] as? String ?: ""
                    val reviewScore = data["reviewScore"] as? Double ?: 0.0
                    val cancelled = data["cancelled"] as? Boolean ?: false
                    val cancelledBy = data["cancelledBy"] as? String ?: ""
                    val createdBy = data["createdBy"] as? String ?: ""
                    val paymentProcessed = data["paymentProcessed"] as? Boolean ?: false
                    val approved = data["approved"] as? Boolean ?: false
                    val adminApproved = data["adminApproved"] as? Boolean ?: false
                    val adminRejected = data["adminRejected"] as? Boolean ?: false

                    Assignment(
                        documentId = documentId,
                        date = date,
                        classID = classID,
                        rate = rate,
                        additionalNotes = additionalNotes,
                        assignedTo = assignedTo,
                        assignedToUID = assignedToUID,
                        isAvailable = isAvailable,
                        inProgress = inProgress,
                        completed = completed,
                        attachments = attachments,
                        reviewScore = reviewScore,
                        cancelled = cancelled,
                        cancelledBy = cancelledBy,
                        createdBy = createdBy,
                        paymentProcessed = paymentProcessed,
                        approved = approved,
                        adminApproved = adminApproved,
                        adminRejected = adminRejected
                    )
                }
                assignments.value = fetchedAssignments
            }
    }

    fun createAssignment(date: Date, classID: String, additionalNotes: String, uid: String, completion: (Boolean, String?) -> Unit) {
        val newAssignmentRef = Firebase.firestore.collection("Assignments").document()

        val assignmentData = mapOf(
            "date" to date,
            "classID" to classID,
            "rate" to 0.0,
            "additionalNotes" to additionalNotes,
            "assignedTo" to "",
            "assignedToUID" to "",
            "isAvailable" to false,
            "inProgress" to false,
            "completed" to false,
            "attachments" to "",
            "reviewScore" to 0.0,
            "cancelled" to false,
            "cancelledBy" to "",
            "createdBy" to uid,
            "paymentProcessed" to false,
            "approved" to false,
            "adminApproved" to false,
            "adminRejected" to false
        )

        newAssignmentRef.set(assignmentData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                completion(true, newAssignmentRef.id)
            } else {
                completion(false, task.exception?.message)
            }
        }
    }

    fun updateAssignmentDocumentWithAttachments(assignmentId: String, attachments: List<String>, completion: (Boolean, String?) -> Unit) {
        val assignmentRef = Firebase.firestore.collection("Assignments").document(assignmentId)

        assignmentRef.update("attachments", attachments).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                completion(true, null)
            } else {
                completion(false, task.exception?.message)
            }
        }
    }
}
