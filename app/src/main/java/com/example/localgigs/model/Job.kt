// Job.kt (or place this at the top of your ClientHomePage.kt file)
data class Job(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val budget: Double = 0.0,
    val location: String = "",
    val jobType: String = "",
    val skills: String = ""
)
