import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Job(
    @DocumentId val id: String? = null,
    val date: Date = Date(),
    val documentId: String,
    val schoolName: String = "",
    val schoolUid: String = "",
    val category: String = "",
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val rate: Double = 0.00,
    val additionalNotes: String = "",
    val createdBy: String = "",
    val createdByUid: String = "",
    val assignedTo: String = "",
    val assignedToUID: String = "",
    val isAvailable: Boolean = true,
    val inProgress: Boolean = false,
    val completed: Boolean = false,
    val attachments: String = "",
    val reviewScore: Double = 0.00,
    val cancelled: Boolean = false,
    val cancelledBy: String = "",
    val paymentProcessed: Boolean = false
) {
    companion object {
        fun fromData(documentId: String, data: Map<String, Any>): Job {
            return Job(
                documentId = documentId,
                date = data["date"] as? Date ?: Date(),
                schoolName = data["schoolName"] as? String ?: "",
                schoolUid = data["schoolUid"] as? String ?: "",
                category = data["category"] as? String ?: "",
                startTime = data["startTime"] as? Date ?: Date(),
                endTime = data["endTime"] as? Date ?: Date(),
                rate = data["rate"] as? Double ?: 0.00,
                additionalNotes = data["additionalNotes"] as? String ?: "",
                createdBy = data["createdBy"] as? String ?: "",
                createdByUid = data["createdByUid"] as? String ?: "",
                assignedTo = data["assignedTo"] as? String ?: "",
                assignedToUID = data["assignedToUID"] as? String ?: "",
                isAvailable = data["isAvailable"] as? Boolean ?: true,
                inProgress = data["inProgress"] as? Boolean ?: false,
                completed = data["completed"] as? Boolean ?: false,
                attachments = data["attachments"] as? String ?: "",
                reviewScore = data["reviewScore"] as? Double ?: 0.00,
                cancelled = data["cancelled"] as? Boolean ?: false,
                cancelledBy = data["cancelledBy"] as? String ?: "",
                paymentProcessed = data["paymentProcessed"] as? Boolean ?: false
            )
        }
    }
}
