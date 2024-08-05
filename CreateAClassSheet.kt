import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*

class CreateClassActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateClassScreen(userSettings = remember { mutableStateOf(UserSettings()) })
        }
    }
}

@Composable
fun CreateClassScreen(userSettings: MutableState<UserSettings>, viewModel: ClassViewModel = viewModel()) {
    var className by remember { mutableStateOf("") }
    var classSubject by remember { mutableStateOf("") }
    var numberOfStudents by remember { mutableStateOf("") }
    var classStartTime by remember { mutableStateOf(Date()) }
    var classEndTime by remember { mutableStateOf(Date()) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showFilePicker by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var processComplete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.lilac))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        NewLogoHeaderSection()
        Divider()
        Text(
            text = "Add A Class",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Purple,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Divider()
        BasicTextField(
            value = classSubject,
            onValueChange = { classSubject = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White)
        )
        BasicTextField(
            value = numberOfStudents,
            onValueChange = { numberOfStudents = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White),
            keyboardType = KeyboardType.Number
        )
        DatePicker("Class Start Time", classStartTime) { selectedDate ->
            classStartTime = selectedDate
        }
        DatePicker("Class End Time", classEndTime) { selectedDate ->
            classEndTime = selectedDate
        }
        Button(
            onClick = { showFilePicker = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(if (selectedFileUri == null) "Upload Class Roster" else selectedFileUri!!.lastPathComponent, color = Color.White)
        }
        Button(
            onClick = {
                saveClass(
                    classSubject, numberOfStudents, classStartTime, classEndTime, selectedFileUri,
                    userSettings, viewModel, onSuccess = {
                        processComplete = true
                    },
                    onError = { error ->
                        alertMessage = error
                        showAlert = true
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Pink),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Save", color = Color.White, fontSize = 16.sp)
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Error") },
                text = { Text(alertMessage) },
                confirmButton = {
                    Button(onClick = { showAlert = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (processComplete) {
            AlertDialog(
                onDismissRequest = { processComplete = false },
                title = { Text("Success") },
                text = { Text("You successfully created a class.") },
                confirmButton = {
                    Button(onClick = { processComplete = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun DatePicker(label: String, date: Date, onDateSelected: (Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.time = date

    Column {
        Text(label)
        BasicTextField(
            value = date.toString(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White)
        )
        // Add logic to show date picker dialog
    }
}

fun saveClass(
    classSubject: String, numberOfStudents: String, classStartTime: Date, classEndTime: Date,
    selectedFileUri: Uri?, userSettings: MutableState<UserSettings>, viewModel: ClassViewModel,
    onSuccess: () -> Unit, onError: (String) -> Unit
) {
    if (classSubject.isBlank() || numberOfStudents.isBlank()) {
        onError("Please fill in all fields.")
        return
    }

    val numberOfStudentsInt = numberOfStudents.toIntOrNull()
    if (numberOfStudentsInt == null) {
        onError("Please enter a valid number of students.")
        return
    }

    viewModel.saveClass(
        classSubject = classSubject,
        numberOfStudents = numberOfStudentsInt,
        classStartTime = classStartTime,
        classEndTime = classEndTime,
        selectedFileUri = selectedFileUri,
        userSettings = userSettings.value,
        onSuccess = onSuccess,
        onError = onError
    )
}

// ViewModel and other required classes

class ClassViewModel : ViewModel() {
    fun saveClass(
        classSubject: String, numberOfStudents: Int, classStartTime: Date, classEndTime: Date,
        selectedFileUri: Uri?, userSettings: UserSettings, onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val newClassRef = db.collection("Schools").document(userSettings.assignedSchool).collection("Classes").document()
        val duration = (classEndTime.time - classStartTime.time) / 3600000.0

        val classData = mapOf(
            "className" to classSubject,
            "classSubject" to classSubject,
            "numberOfStudents" to numberOfStudents,
            "schoolUID" to userSettings.assignedSchool,
            "teacherUID" to userSettings.uid,
            "classStartTime" to classStartTime,
            "classEndTime" to classEndTime,
            "duration" to duration
        )

        newClassRef.set(classData)
            .addOnSuccessListener {
                if (selectedFileUri != null) {
                    uploadFile(newClassRef, selectedFileUri, onSuccess, onError)
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                onError("Failed to save class: ${exception.localizedMessage}")
            }
    }

    private fun uploadFile(documentRef: DocumentReference, fileUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("Schools/${documentRef.id}/Classes/${documentRef.id}/Attachments/${fileUri.lastPathSegment}")
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    documentRef.update("classRosterURL", downloadUrl.toString())
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onError("Failed to update class data: ${exception.localizedMessage}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onError("Failed to upload file: ${exception.localizedMessage}")
            }
    }
}

data class UserSettings(
    var uid: String = "",
    var assignedSchool: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var emailAddress: String = "",
    var pushToken: String = ""
)
