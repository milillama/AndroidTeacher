import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Assignment(
    @DocumentId val id: String? = null,
    val date: Date = Date(),
    val documentId: String,
    val classID: String = "",
    val rate: Double = 0.00,
    val additionalNotes: String = "",
    val assignedTo: String = "",
    val assignedToUID: String = "",
    val isAvailable: Boolean = true,
    val inProgress: Boolean = false,
    val completed: Boolean = false,
    val attachments: String = "",
    val reviewScore: Double = 0.00,
    val cancelled: Boolean = false,
    val cancelledBy: String = "",
    val createdBy: String = "",
    val paymentProcessed: Boolean = false,
    val approved: Boolean = false,
    val requestType: Double = 0.00,
    val adminApproved: Boolean = false,
    val adminRejected: Boolean = false,
    val subRequired: Boolean = false,
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val pushToken: String = ""
) {
    companion object {
        fun fromData(documentId: String, data: Map<String, Any>): Assignment {
            return Assignment(
                documentId = documentId,
                date = data["date"] as? Date ?: Date(),
                classID = data["classID"] as? String ?: "",
                rate = data["rate"] as? Double ?: 0.00,
                additionalNotes = data["additionalNotes"] as? String ?: "",
                assignedTo = data["assignedTo"] as? String ?: "",
                assignedToUID = data["assignedToUID"] as? String ?: "",
                isAvailable = data["isAvailable"] as? Boolean ?: true,
                inProgress = data["inProgress"] as? Boolean ?: false,
                completed = data["completed"] as? Boolean ?: false,
                attachments = data["attachments"] as? String ?: "",
                reviewScore = data["reviewScore"] as? Double ?: 0.00,
                cancelled = data["cancelled"] as? Boolean ?: false,
                cancelledBy = data["cancelledBy"] as? String ?: "",
                createdBy = data["createdBy"] as? String ?: "",
                paymentProcessed = data["paymentProcessed"] as? Boolean ?: false,
                approved = data["approved"] as? Boolean ?: false,
                requestType = data["requestType"] as? Double ?: 0.00,
                adminApproved = data["adminApproved"] as? Boolean ?: false,
                adminRejected = data["adminRejected"] as? Boolean ?: false,
                subRequired = data["subRequired"] as? Boolean ?: false,
                startTime = data["startTime"] as? Date ?: Date(),
                endTime = data["endTime"] as? Date ?: Date(),
                pushToken = data["pushToken"] as? String ?: ""
            )
        }
    }
}
