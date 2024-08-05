import com.google.firebase.firestore.DocumentId

data class School(
    @DocumentId val id: String? = null,
    val documentId: String,
    val schoolName: String = "",
    val schoolLogo: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val phoneNumber: String = "",
    val emailAddress: String = "",
    val rating: Double = 0.00,
    val website: String = "",
    val district: String = "",
    val pointOfContact: String = "",
    val emailAddress2: String = "",
    val phoneNumber2: String = "",
    val pointOfContact2: String = "",
    val funFact: String = "",
    val domain: String = ""
) {
    companion object {
        fun fromData(documentId: String, data: Map<String, Any>): School {
            return School(
                documentId = documentId,
                schoolName = data["schoolName"] as? String ?: "",
                schoolLogo = data["schoolLogo"] as? String ?: "",
                address = data["address"] as? String ?: "",
                city = data["city"] as? String ?: "",
                state = data["state"] as? String ?: "",
                zip = data["zip"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                emailAddress = data["emailAddress"] as? String ?: "",
                rating = data["rating"] as? Double ?: 0.00,
                website = data["website"] as? String ?: "",
                district = data["district"] as? String ?: "",
                pointOfContact = data["pointOfContact"] as? String ?: "",
                emailAddress2 = data["emailAddress2"] as? String ?: "",
                phoneNumber2 = data["phoneNumber2"] as? String ?: "",
                pointOfContact2 = data["pointOfContact2"] as? String ?: "",
                funFact = data["funFact"] as? String ?: "",
                domain = data["domain"] as? String ?: ""
            )
        }
    }
}
