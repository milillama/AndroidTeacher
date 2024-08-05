import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

class RequestsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RequestsScreen(userSettings = remember { mutableStateOf(UserSettings()) })
        }
    }
}

@Composable
fun RequestsScreen(userSettings: MutableState<UserSettings>, viewModel: AssignmentsViewModel = viewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val classNames by viewModel.classNames.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAssignments(userSettings.value.assignedSchool)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.lilac))
    ) {
        NewLogoHeaderSection()
        RequestsHeaderImageSection()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (assignments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("There are currently no requests.", fontSize = 20.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                assignments.forEach { assignment ->
                    val className = classNames[assignment.classID] ?: "Personal"
                    AssignmentCard(
                        assignment = assignment,
                        className = className,
                        viewModel = viewModel,
                        userSettings = userSettings.value
                    )
                }
            }
        }
    }
}

@Composable
fun RequestsHeaderImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(Color.Gray)
    ) {
        Image(
            painter = rememberImagePainter(data = R.drawable.placeholder),
            contentDescription = "Placeholder",
            modifier = Modifier
                .fillMaxSize()
                .opacity(0.4f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Time Off Requests",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: Assignment,
    className: String,
    viewModel: AssignmentsViewModel,
    userSettings: UserSettings
) {
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var editSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White)
                .clickable { }
        ) {
            CalendarDateIcon(month = formatDate(assignment.date, "MMM"), day = formatDate(assignment.date, "dd"))

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = when {
                        assignment.adminApproved && !assignment.adminRejected -> "Status: Approved"
                        !assignment.adminApproved && assignment.adminRejected -> "Status: Rejected"
                        else -> "Status: Pending"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        assignment.adminApproved && !assignment.adminRejected -> colorResource(id = R.color.pink)
                        !assignment.adminApproved && assignment.adminRejected -> Color.Red
                        else -> Color.Black
                    }
                )
                Text(
                    text = className,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    Button(onClick = { showDeleteConfirmation = true }) {
                        Text("Delete")
                    }
                    if (!assignment.adminApproved && !assignment.adminRejected) {
                        Button(onClick = { editSheet = true }) {
                            Text("Edit")
                        }
                    }
                }
            }
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Are you sure you want to delete the time off request?") },
                confirmButton = {
                    Button(onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAssignment(assignment, userSettings)
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (editSheet) {
            EditTimeOffRequestScreen()
        }
    }
}

fun formatDate(date: Date, format: String): String {
    val dateFormat = java.text.SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(date).uppercase(Locale.getDefault())
}

// ViewModel and other required classes

class AssignmentsViewModel : ViewModel() {
    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments

    private val _classNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val classNames: StateFlow<Map<String, String>> = _classNames

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Initialize ViewModel if needed
    }

    fun fetchAssignments(schoolUID: String) {
        _isLoading.value = true
        FirebaseFirestore.getInstance()
            .collection("Assignments")
            .whereEqualTo("schoolUID", schoolUID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val assignments = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Assignment::class.java)?.apply { documentId = document.id }
                }
                _assignments.value = assignments
                prefetchClassNames(assignments, schoolUID)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun prefetchClassNames(assignments: List<Assignment>, schoolUID: String) {
        val classNames = mutableMapOf<String, String>()
        val db = FirebaseFirestore.getInstance()
        val tasks = assignments.map { assignment ->
            db.collection("Schools").document(schoolUID).collection("Classes").document(assignment.classID)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        classNames[assignment.classID] = document.getString("className") ?: "Unknown"
                    }
                }
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            _classNames.value = classNames
            _isLoading.value = false
        }
    }

    fun deleteAssignment(assignment: Assignment, userSettings: UserSettings) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Assignments").document(assignment.documentId)
            .delete()
            .addOnSuccessListener {
                // Delete related data in other collections and update UI
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }
}

data class Assignment(
    var documentId: String = "",
    val classID: String = "",
    val date: Date = Date(),
    val adminApproved: Boolean = false,
    val adminRejected: Boolean = false,
    // Add other fields as necessary
)

data class UserSettings(
    var assignedSchool: String = "",
    // Add other fields as necessary
)
