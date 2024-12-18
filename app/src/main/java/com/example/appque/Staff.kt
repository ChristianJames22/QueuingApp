data class Staff(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    var firebaseUid: String? = null,
    val timestamp: Long? = System.currentTimeMillis()
)
