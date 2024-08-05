import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*

class BulkTimeOffRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BulkTimeOffRequestScreen(userSettings = remember { mutableStateOf(UserSettings()) })
        }
    }
}

@Composable
fun BulkTimeOffRequestScreen(userSettings: MutableState<UserSettings>, viewModel: AssignmentsViewModel = viewModel()) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(Date()) }
    var additionalNotes by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showFilePicker by remember { mutableStateOf(false) }
    var subRequired by remember { mutableStateOf(true) }
    var fullDayOff by remember { mutableStateOf(false) }
    var selectedClassIDs by remember { mutableStateOf(setOf<String>()) }
    var requestType by remember { mutableStateOf(0) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var showConfirmationAlert by remember { mutableStateOf(false) }
    var processComplete by remember { mutableStateOf(false) }
    var classes by remember { mutableStateOf(emptyList<Class>()) }

    LaunchedEffect(Unit) {
        viewModel.fetchClasses(userSettings.value.assignedSchool).collect { fetchedClasses ->
            classes = fetchedClasses
        }
    }

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
            text = "New Time Off Request",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Purple,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Divider()
        DatePicker(date) { selectedDate ->
            date = selectedDate
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Additional Information:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        SelectionContainer {
            BasicTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                modifier = Modifier
                    .height(100.dp)
                    .background(Color.White)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ToggleButton(
            text = "Does the time off request require a substitute teacher?",
            isChecked = subRequired
        ) {
            subRequired = it
        }
        ToggleButton(
            text = "Are you requesting a full day off?",
            isChecked = fullDayOff
        ) {
            fullDayOff = it
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Which time allocation would you like to pull from?", fontWeight = FontWeight.SemiBold)
        Picker(
            options = listOf("Vacation", "Sick Time", "Unpaid Leave"),
            selectedOption = requestType
        ) {
            requestType = it
        }
        if (subRequired && !fullDayOff) {
            Column {
                Text("Select Classes", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                classes.forEach { classItem ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(classItem.className, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = selectedClassIDs.contains(classItem.documentId),
                            onCheckedChange = {
                                if (it) {
                                    selectedClassIDs = selectedClassIDs + classItem.documentId
                                } else {
                                    selectedClassIDs = selectedClassIDs - classItem.documentId
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showFilePicker = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(colorResource(id = R.color.dark_purple))),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Upload Attachments", color = Color.White, fontSize = 16.sp)
        }
        Button(
            onClick = { showConfirmationAlert = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(colorResource(id = R.color.pink))),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Save", color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Time Off Request") },
                text = { Text(alertMessage) },
                confirmButton = {
                    Button(onClick = { showAlert = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showConfirmationAlert) {
            AlertDialog(
                onDismissRequest = { showConfirmationAlert = false },
                title = { Text("Confirmation") },
                text = { Text("Are you sure you want to submit this time off request?") },
                confirmButton = {
                    Button(onClick = {
                        showConfirmationAlert = false
                        if (!subRequired && !fullDayOff) {
                            savePersonalTimeOffRequest(
                                date, additionalNotes, requestType, userSettings, viewModel
                            )
                        } else {
                            saveBulkTimeOffRequest(
                                date, additionalNotes, selectedClassIDs, requestType, fullDayOff,
                                subRequired, selectedFileUri, userSettings, viewModel
                            )
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showConfirmationAlert = false }) {
                        Text("No")
                    }
                }
            )
        }

        if (processComplete) {
            AlertDialog(
                onDismissRequest = { processComplete = false },
                title = { Text("Success") },
                text = { Text("The time off request was created successfully.") },
                confirmButton = {
                    Button(onClick = {
                        processComplete = false
                        dismiss()
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun ToggleButton(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(text, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun Picker(options: List<String>, selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        options.forEachIndexed { index, option ->
            Button(
                onClick = { onOptionSelected(index) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedOption == index) Color.Gray else Color.LightGray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(option, color = Color.White)
            }
        }
    }
}

@Composable
fun DatePicker(date: Date, onDateSelected: (Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.time = date

    Column {
        Text("Select Date")
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

fun savePersonalTimeOffRequest(
    date: Date, additionalNotes: String, requestType: Int,
    userSettings: MutableState<UserSettings>, viewModel: AssignmentsViewModel
) {
    // Implement logic to save personal time off request
}

fun saveBulkTimeOffRequest(
    date: Date, additionalNotes: String, selectedClassIDs: Set<String>, requestType: Int,
    fullDayOff: Boolean, subRequired: Boolean, selectedFileUri: Uri?,
    userSettings: MutableState<UserSettings>, viewModel: AssignmentsViewModel
) {
    // Implement logic to save bulk time off request
}

// ViewModel and other required classes

class AssignmentsViewModel : ViewModel() {
    private val _classes = MutableStateFlow<List<Class>>(emptyList())
    val classes: StateFlow<List<Class>> = _classes

    init {
        // Initialize ViewModel if needed
    }

    fun fetchClasses(schoolUID: String) = flow {
        FirebaseFirestore.getInstance()
            .collection("Schools")
            .document(schoolUID)
            .collection("Classes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val classes = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Class::class.java)?.apply { documentId = document.id }
                }
                emit(classes)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    fun savePersonalTimeOffRequest(date: Date, additionalNotes: String, requestType: Int, userSettings: UserSettings) {
        // Implement save logic
    }

    fun saveBulkTimeOffRequest(date: Date, additionalNotes: String, selectedClassIDs: Set<String>, requestType: Int, fullDayOff: Boolean, subRequired: Boolean, selectedFileUri: Uri?, userSettings: UserSettings) {
        // Implement save logic
    }
}

data class UserSettings(
    var assignedSchool: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var emailAddress: String = "",
    var pushToken: String = ""
)

data class Class(
    var documentId: String = "",
    val className: String = "",
    val classSubject: String = "",
    val numberOfStudents: Int = 0,
    val schoolUID: String = "",
    val teacherUID: String = "",
    val classRosterURL: String = "",
    val classStartTime: Date = Date(),
    val classEndTime: Date = Date(),
    val duration: Double = 0.0
)
