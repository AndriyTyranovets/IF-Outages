package com.github.andriytyranovets.ifoutages

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.andriytyranovets.ifoutages.config.Formatter
import com.github.andriytyranovets.ifoutages.datastore.DataStoreRepository
import com.github.andriytyranovets.ifoutages.models.OutageDuration
import com.github.andriytyranovets.ifoutages.ui.theme.IFOutagesTheme
import com.github.andriytyranovets.ifoutages.viewmodels.MainViewModel
import java.time.LocalDateTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "appdata"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    lateinit var store: DataStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = DataStoreRepository(dataStore)

        enableEdgeToEdge()
        setContent {
            var accountNumber: String? by rememberSaveable { mutableStateOf(null) }

            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val outages by viewModel.outages.observeAsState(emptyMap())
            val queue by viewModel.queue.observeAsState("")

            var showDialog by remember { mutableStateOf(false) }

            IFOutagesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
//                            title = { Text("Queue ${q}.${s}") },
                            title = { Text("Outages${if(queue.isBlank()) "" else ": Queue ${queue}"}") },
                            actions = {
                                IconButton(onClick = { showDialog = true }) {
                                    Icon(imageVector = Icons.Default.Build, contentDescription = "Change account number")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    LaunchedEffect(Unit) {
                        viewModel.getOutages(accountNumber!!)
                        Log.i("Main Activity", outages.toString())
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(innerPadding),

                        ) {
                        outages.forEach { (day, list) ->
                            stickyHeader {
                                Column(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .shadow(1.dp)
                                ) {
                                    Text(
                                        text = day,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            items(list) {
                                OutageItem(
                                    duration = it,
                                    modifier = Modifier.padding(
                                        paddingValues = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
                                    )
                                )
                            }
                        }
                    }

                    if(showDialog) {
                        Dialog(onDismissRequest = { showDialog = false }) {
                            var text by remember { mutableStateOf(accountNumber ?: "") }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.35f)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),

                            ) {
                                Text(
                                    modifier = Modifier.padding(16.dp),
                                    text = "${if(accountNumber == null) "Set" else "Change"} account number",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                OutlinedTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .focusable(),
                                    value = text,
                                    onValueChange = { text = it },
                                    label = { Text("Account number") }
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(
                                        onClick = { showDialog = false },
                                        modifier = Modifier.padding(8.dp),
                                    ) {
                                        Text("Cancel")
                                    }
                                    TextButton(
                                        onClick = {
                                            accountNumber = text
                                            showDialog = false
                                            viewModel.getOutages(accountNumber!!)
                                        },
                                        modifier = Modifier.padding(8.dp),
                                    ) {
                                        Text("Save")
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

private val bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IFOutagesTheme {
        Greeting("Android")
    }
}

@Composable
fun OutageItem(duration: OutageDuration, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (duration.unknownType != null) Color(0xfff9ea89) else Color.Unspecified)
    ) {
        Column(modifier = modifier) {
            Text(
                text = "${duration.begin.toLocalTime().format(Formatter.Time)} - ${duration.end.toLocalTime().format(Formatter.Time)}",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(text = "${duration.duration()} hour${if (duration.duration() > 1) "s" else ""}")
        }
    }
}