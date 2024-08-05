import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(userSettings: UserSettings, viewModel: AuthenticationViewModel = viewModel()) {
    var teacher by remember { mutableStateOf<Teacher?>(null) }
    var school by remember { mutableStateOf<School?>(null) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loadSchoolData(context, userSettings) { loadedSchool ->
            school = loadedSchool
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(colorResource(id = R.color.lilac))
    ) {
        HomeLogoHeaderSection()

        school?.let { loadedSchool ->
            HomeSchoolHeaderImageSection(userSettings, loadedSchool)
        }

        teacher?.let { loadedTeacher ->
            HomeTeacherInfoSection(userSettings, loadedTeacher)
        }
    }
}

@Composable
fun HomeLogoHeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Banner",
            modifier = Modifier
                .height(100.dp)
                .width(200.dp)
        )
        Text(
            text = "F  O  R     T  E  A  C  H  E  R  S",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.dark_purple)
        )
    }
}

@Composable
fun HomeSchoolHeaderImageSection(userSettings: UserSettings, school: School) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(colorResource(id = R.color.pink).copy(alpha = 0.7f))
    ) {
        if (school.schoolLogo.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = "Placeholder",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .alpha(0.4f)
            )
        } else {
            Image(
                painter = rememberImagePainter(school.schoolLogo),
                contentDescription = "School Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .alpha(0.4f)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = school.schoolName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun HomeTeacherInfoSection(userSettings: UserSettings, teacher: Teacher) {
    val context = LocalContext.current
    var classNames by remember { mutableStateOf(listOf<String>()) }
    var daysOff by remember { mutableStateOf(listOf<Date>()) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showTimeOffRequest by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showCreateRequestSheet by remember { mutableStateOf(false) }
    var showEditClassSheet by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<String?>(null) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fetchClassNames(context, userSettings) { loadedClassNames ->
            classNames = loadedClassNames
        }
        fetchDaysOff(context, teacher) { loadedDaysOff ->
            daysOff = loadedDaysOff
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Upcoming Days Off",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.dark_purple),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DatePicker(
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it }
        )
        if (daysOff.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                daysOff.forEach { dayOff ->
                    CalendarDateIcon(
                        month = formatDate(dayOff, "MMM"),
                        day = formatDate(dayOff, "dd")
                    )
                }
            }
        }

        Text(
            text = "Your Classes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.dark_purple),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (classNames.isEmpty()) {
            Text(
                text = "Hmmm... it looks like you still need to add your classes to the platform. Tap on the New icon below to add a course. Once complete, you should see it right here.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                classNames.forEach { className ->
                    ClassBadgeIcon(
                        className = className,
                        onClick = {
                            selectedClass = className
                            showActionSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showActionSheet) {
        AlertDialog(
            onDismissRequest = { showActionSheet = false },
            title = { Text("Select Action") },
            text = { Text("Choose an action for $selectedClass") },
            confirmButton = {
                TextButton(onClick = { showTimeOffRequest = true }) {
                    Text("Submit a Request")
                }
                TextButton(onClick = { showEditClassSheet = true }) {
                    Text("Edit Class")
                }
                TextButton(onClick = {
                    deleteClass(context, userSettings, selectedClass.orEmpty()) {
                        alertMessage = "The class was deleted successfully."
                        showAlert = true
                    }
                }) {
                    Text("Delete Class")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Class Deleted") },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DatePicker(selectedDate: Date, onDateChange: (Date) -> Unit) {
    // Use a library or implement a custom DatePicker component here
    // Placeholder implementation
    Text(text = selectedDate.toString())
}

@Composable
fun ClassBadgeIcon(className: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .background(colorResource(id = R.color.pink))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = "Class Placeholder",
            modifier = Modifier
                .size(125.dp)
        )
        Text(
            text = className,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun CalendarDateIcon(month: String, day: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray)
    ) {
        Text(
            text = month,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(4.dp)
        )
        Text(
            text = day,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(4.dp)
        )
    }
}

fun loadSchoolData(context: Context, userSettings: UserSettings, onSchoolLoaded: (School?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val schoolRef = db.collection("Schools").document(userSettings.assignedSchool)

    schoolRef.get().addOnSuccessListener { document ->
        if (document != null && document.exists()) {
            val school = document.toObject(School::class.java)
            onSchoolLoaded(school)
        } else {
            Toast.makeText(context, "School document not found", Toast.LENGTH_SHORT).show()
            onSchoolLoaded(null)
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to load school: ${it.message}", Toast.LENGTH_SHORT).show()
        onSchoolLoaded(null)
    }
}

fun fetchClassNames(context: Context, userSettings: UserSettings, onClassNamesLoaded: (List<String>) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val classesRef = FirebaseFirestore.getInstance()
        .collection("Schools")
        .document(userSettings.assignedSchool)
        .collection("Classes")

    classesRef.whereEqualTo("teacherUID", uid).get().addOnSuccessListener { documents ->
        val classNames = documents.mapNotNull { it.getString("className") }
        onClassNamesLoaded(classNames)
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to fetch classes: ${it.message}", Toast.LENGTH_SHORT).show()
        onClassNamesLoaded(emptyList())
    }
}

fun fetchDaysOff(context: Context, teacher: Teacher, onDaysOffLoaded: (List<Date>) -> Unit) {
    val daysOff = teacher.upcomingDaysOff
    onDaysOffLoaded(daysOff)
}

fun deleteClass(context: Context, userSettings: UserSettings, className: String, onClassDeleted: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val classesRef = db.collection("Schools").document(userSettings.assignedSchool).collection("Classes")

    classesRef.whereEqualTo("className", className).get().addOnSuccessListener { snapshot ->
        if (!snapshot.isEmpty) {
            snapshot.documents.first().reference.delete().addOnSuccessListener {
                Toast.makeText(context, "Class deleted successfully", Toast.LENGTH_SHORT).show()
                onClassDeleted()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete class: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Class not found", Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to delete class: ${it.message}", Toast.LENGTH_SHORT).show()
    }
}

fun formatDate(date: Date, format: String): String {
    val sdf = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
    return sdf.format(date).uppercase()
}
