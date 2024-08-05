import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Class(
    @DocumentId val id: String? = null,
    val documentId: String,
    val className: String = "",
    val classSubject: String = "",
    val numberOfStudents: Int = 0,
    val schoolUID: String = "",
    val teacherUID: String = "",
    val classRosterURL: String = "",
    val classStartTime: Date = Date(),
    val classEndTime: Date = Date(),
    val duration: Double = 0.0
) {
    companion object {
        fun fromData(documentId: String, data: Map<String, Any>): Class {
            return Class(
                documentId = documentId,
                className = data["className"] as? String ?: "",
                classSubject = data["classSubject"] as? String ?: "",
                numberOfStudents = data["numberOfStudents"] as? Int ?: 0,
                schoolUID = data["schoolUID"] as? String ?: "",
                teacherUID = data["teacherUID"] as? String ?: "",
                classRosterURL = data["classRosterURL"] as? String ?: "",
                classStartTime = data["classStartTime"] as? Date ?: Date(),
                classEndTime = data["classEndTime"] as? Date ?: Date(),
                duration = data["duration"] as? Double ?: 0.0
            )
        }
    }
}
