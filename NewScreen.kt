import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun NewScreen(userSettings: UserSettings, viewModel: AuthenticationViewModel = viewModel()) {
    var showCreateClassSheet by remember { mutableStateOf(false) }
    var showTimeOffRequestSheet by remember { mutableStateOf(false) }
    var showBulkEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colorResource(id = R.color.lilac))
    ) {
        NewLogoHeaderSection()
        NewHeaderImageSection()
        Spacer(modifier = Modifier.weight(1f))

        // Add Class Button
        Button(
            onClick = { showCreateClassSheet = true },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(150.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.dark_purple))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Add New Class",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
                Divider(
                    color = Color.White,
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Tap here to create your class schedule.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // New Time Off Requests Button
        Button(
            onClick = { showBulkEdit = true },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(150.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.pink))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "New Time Off Requests",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
                Divider(
                    color = Color.White,
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Tap here to create time off requests for multiple classes.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    if (showCreateClassSheet) {
        CreateClassSheet(userSettings = userSettings, onDismiss = { showCreateClassSheet = false })
    }
    if (showTimeOffRequestSheet) {
        TimeOffRequestSheet(userSettings = userSettings, onDismiss = { showTimeOffRequestSheet = false })
    }
    if (showBulkEdit) {
        BulkTimeOffRequestSheet(userSettings = userSettings, onDismiss = { showBulkEdit = false })
    }
}

// Logo Header Section
@Composable
fun NewLogoHeaderSection() {
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

// School Header Image Section
@Composable
fun NewHeaderImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(colorResource(id = R.color.pink).copy(alpha = 0.7f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = "Placeholder",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .alpha(0.4f)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// Placeholder composables for CreateClassSheet, TimeOffRequestSheet, and BulkTimeOffRequestSheet
@Composable
fun CreateClassSheet(userSettings: UserSettings, onDismiss: () -> Unit) {
    // Your implementation here
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Text(text = "CreateClassSheet", modifier = Modifier.align(Alignment.Center))
        Button(onClick = onDismiss, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
            Text("Close")
        }
    }
}

@Composable
fun TimeOffRequestSheet(userSettings: UserSettings, onDismiss: () -> Unit) {
    // Your implementation here
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Text(text = "TimeOffRequestSheet", modifier = Modifier.align(Alignment.Center))
        Button(onClick = onDismiss, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
            Text("Close")
        }
    }
}

@Composable
fun BulkTimeOffRequestSheet(userSettings: UserSettings, onDismiss: () -> Unit) {
    // Your implementation here
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Text(text = "BulkTimeOffRequestSheet", modifier = Modifier.align(Alignment.Center))
        Button(onClick = onDismiss, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
            Text("Close")
        }
    }
}
