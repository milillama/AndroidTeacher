import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*

class TimeOffRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeOffRequestScreen()
        }
    }
}

@Composable
fun TimeOffRequestScreen(userSettings: UserSettings, viewModel: TimeOffRequestViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf(Date()) }
    var additionalNotes by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showFilePicker by remember { mutableStateOf(false) }
    var subRequired by remember { mutableStateOf(true) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var classes by remember { mutableStateOf(listOf<Class>()) }
    var selectedClassID by remember { mutableStateOf("") }
    var fullDayOff by remember { mutableStateOf(false) }
    var processComplete by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFileUri = uri
        alertMessage = if (uri != null) "File selected successfully" else "No file selected"
        showAlert = true
    }

    LaunchedEffect(Unit) {
        viewModel.fetchClasses(userSettings.assignedSchool) { fetchedClasses ->
            classes = fetchedClasses
            if (classes.isNotEmpty()) {
                selectedClassID = classes.first().documentId
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(text = alertTitle) },
            text = { Text(text = alertMessage) },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
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
            color = colorResource(id = R.color.dark_purple),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Divider()
        DatePicker(
            date = date,
            onDateChange = { newDate -> date = newDate }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Additional Information:",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Add any additional information related to the request.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
            TextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 8.dp)
                    .background(Color.White),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        }
        Toggle(
            label = "Does the time off request require a substitute teacher?",
            checked = subRequired,
            onCheckedChange = { subRequired = it }
        )
        Toggle(
            label = "Are you requesting a full day off?",
            checked = fullDayOff,
            onCheckedChange = { fullDayOff = it }
        )
        if (subRequired && !fullDayOff) {
            DropdownMenu(
                label = "Select Class",
                options = classes.map { it.className },
                selectedOption = selectedClassID,
                onOptionSelected = { selectedClassID = it }
            )
        }
        Button(
            onClick = { filePickerLauncher.launch("*/*") },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
                .height(50.dp)
                .background(colorResource(id = R.color.dark_purple)),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.dark_purple))
        ) {
            Text(
                text = "Upload Attachments",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                scope.launch {
                    viewModel.saveTimeOffRequest(
                        userSettings = userSettings,
                        date = date,
                        additionalNotes = additionalNotes,
                        selectedClassID = selectedClassID,
                        selectedFileUri = selectedFileUri,
                        subRequired = subRequired,
                        fullDayOff = fullDayOff,
                        onSuccess = {
                            alertTitle = "Success"
                            alertMessage = "You successfully submitted a request."
                            processComplete = true
                        },
                        onError = { message ->
                            alertTitle = "Error"
                            alertMessage = message
                            showAlert = true
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colorResource(id = R.color.pink)),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.pink))
        ) {
            Text(
                text = "Save",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun NewLogoHeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Image(
            painter = rememberImagePainter(data = R.drawable.banner),
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
fun DatePicker(date: Date, onDateChange: (Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { time = date }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val datePickerDialog = remember {
        android.app.DatePickerDialog(context, { _, newYear, newMonth, newDay ->
            onDateChange(Calendar.getInstance().apply {
                set(newYear, newMonth, newDay)
            }.time)
        }, year, month, day)
    }
    TextField(
        value = date.toString(),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        readOnly = true,
        label = { Text("Select Date") },
        trailingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) }
    )
}

@Composable
fun Toggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.pink),
                uncheckedThumbColor = Color.Gray
            )
        )
    }
}

@Composable
fun DropdownMenu(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        TextField(
            value = selectedOption,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(text = option)
                }
            }
        }
    }
}

class TimeOffRequestViewModel : ViewModel() {

    fun fetchClasses(schoolUID: String, onClassesFetched: (List<Class>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("Schools")
            .document(schoolUID)
            .collection("Classes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val classes = querySnapshot.documents.map { document ->
                    val className = document.getString("className") ?: ""
                    val classSubject = document.getString("classSubject") ?: ""
                    val numberOfStudents = document.getLong("numberOfStudents")?.toInt() ?: 0
                    val schoolUID = document.getString("schoolUID") ?: ""
                    val teacherUID = document.getString("teacherUID") ?: ""
                    val classRosterURL = document.getString("classRosterURL") ?: ""
                    val classStartTime = (document.getTimestamp("classStartTime")?.toDate() ?: Date())
                    val classEndTime = (document.getTimestamp("classEndTime")?.toDate() ?: Date())
                    val duration = document.getDouble("duration") ?: 0.0
                    Class(
                        documentId = document.id,
                        data = mapOf(
                            "className" to className,
                            "classSubject" to classSubject,
                            "numberOfStudents" to numberOfStudents,
                            "schoolUID" to schoolUID,
                            "teacherUID" to teacherUID,
                            "classRosterURL" to classRosterURL,
                            "classStartTime" to classStartTime,
                            "classEndTime" to classEndTime,
                            "duration" to duration
                        )
                    )
                }
                onClassesFetched(classes)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    fun saveTimeOffRequest(
        userSettings: UserSettings,
        date: Date,
        additionalNotes: String,
        selectedClassID: String,
        selectedFileUri: Uri?,
        subRequired: Boolean,
        fullDayOff: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val schoolUID = userSettings.assignedSchool
        val db = FirebaseFirestore.getInstance()
        val newAssignmentRef = db.collection("Assignments").document()
        val assignmentData = hashMapOf(
            "date" to Timestamp(date),
            "classID" to selectedClassID,
            "additionalNotes" to additionalNotes,
            "assignedTo" to "",
            "assignedToUID" to "",
            "isAvailable" to false,
            "inProgress" to false,
            "completed" to false,
            "reviewScore" to 0.0,
            "cancelled" to false,
            "cancelledBy" to "",
            "paymentProcessed" to false,
            "approved" to false
        )
        newAssignmentRef.set(assignmentData)
            .addOnSuccessListener {
                selectedFileUri?.let { uri ->
                    uploadFile(newAssignmentRef.id, uri, onSuccess, onError)
                } ?: onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Failed to save time off request")
            }
    }

    private fun uploadFile(documentId: String, fileUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("Schools/${userSettings.assignedSchool}/Classes/$selectedClassID/Assignments/$documentId/${fileUri.lastPathSegment}")
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    FirebaseFirestore.getInstance()
                        .collection("Assignments")
                        .document(documentId)
                        .update("attachments", listOf(downloadUrl.toString()))
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onError(exception.localizedMessage ?: "Failed to update assignment data")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Failed to upload file")
            }
    }
}
