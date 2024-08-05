import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Teacher(
    @DocumentId val id: String? = null,
    val documentId: String,
    val firstName: String = "",
    val lastName: String = "",
    val emailAddress: String = "",
    val phoneNumber: String = "",
    val schoolUid: String = "",
    val assignedClasses: List<String> = emptyList(),
    val totalSickTimeAvailable: Double = 0.0,
    val totalPTOAvailable: Double = 0.0,
    val profilePictureUrl: String = "",
    val verified: Boolean = false,
    val joinDate: Date = Date(),
    val uid: String = "",
    val pushToken: String = "",
    val upcomingDaysOff: List<Date> = emptyList(),
    val totalUnpaidLeave: Double = 0.0
) {
    companion object {
        fun fromData(documentId: String, data: Map<String, Any>): Teacher {
            return Teacher(
                documentId = documentId,
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                emailAddress = data["emailAddress"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                schoolUid = data["schoolUid"] as? String ?: "",
                assignedClasses = data["assignedClasses"] as? List<String> ?: emptyList(),
                totalSickTimeAvailable = data["totalSickTimeAvailable"] as? Double ?: 0.0,
                totalPTOAvailable = data["totalPTOAvailable"] as? Double ?: 0.0,
                profilePictureUrl = data["profilePictureUrl"] as? String ?: "",
                verified = data["verified"] as? Boolean ?: false,
                joinDate = data["joinDate"] as? Date ?: Date(),
                uid = data["uid"] as? String ?: "",
                pushToken = data["pushToken"] as? String ?: "",
                upcomingDaysOff = data["upcomingDaysOff"] as? List<Date> ?: emptyList(),
                totalUnpaidLeave = data["totalUnpaidLeaveUsed"] as? Double ?: 0.0
            )
        }
    }
}
