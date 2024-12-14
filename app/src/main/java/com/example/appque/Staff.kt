data class Staff(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    var firebaseUid: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
