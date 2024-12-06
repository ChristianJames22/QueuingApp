data class Student(
    var uid: String = "",  // Firebase Authentication UID
    val id: String = "",   // Student ID
    val name: String = "",
    val email: String = "",
    val course: String = "",
    val year: String = "",
    val role: String = "student"
)
