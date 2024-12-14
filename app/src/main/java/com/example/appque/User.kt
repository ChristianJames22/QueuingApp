data class User(
    val email: String? = null,
    val firebaseUid: String? = null,
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val course: String? = null,
    val year: String? = null,
    var timestamp: Long? = null,
    val uid: String? = null // Add this property for UID
)
