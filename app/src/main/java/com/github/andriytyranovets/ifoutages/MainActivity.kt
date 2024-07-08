package com.github.andriytyranovets.ifoutages

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.github.andriytyranovets.ifoutages.ui.AccountNumberDialog
import com.github.andriytyranovets.ifoutages.ui.OutageList
import com.github.andriytyranovets.ifoutages.ui.theme.IFOutagesTheme
import com.github.andriytyranovets.ifoutages.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels(factoryProducer = { MainViewModel.factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        enableEdgeToEdge()
        setContent {
            val accountNumber by viewModel.accountNumber.collectAsState()
            var showDialog by remember { mutableStateOf(false) }

            val snackbarHostState = remember { SnackbarHostState() }
            val queue by viewModel.queue.observeAsState("")

            when(accountNumber) {
                null -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
                "" -> showDialog = true
                else -> IFOutagesTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            TheTopBar(
                                queue = queue,
                                showAccountNumberDialog = { showDialog = true }
                            )
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            OutageList(
                                viewModel = viewModel,
                                accountNumber = accountNumber,
                                onEditAccountNumber = { showDialog = true }
                            )
                        }
                    }
                }
            }

            if(showDialog) {
                AccountNumberDialog(
                    accountNumber = accountNumber,
                    onSave = { text ->
                        lifecycleScope.launch {
                            viewModel.updateAccountNumber(text)
                        }.invokeOnCompletion {
                            showDialog = false
                        }
                    },
                    onCancel = {
                        if(accountNumber.isNullOrEmpty()) {
                            this.finish()
                            System.exit(0)
                        } else
                            showDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheTopBar(queue: String, showAccountNumberDialog: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = { Text("Outages${if(queue.isBlank()) "" else ": Queue ${queue}"}") },
        actions = {
            IconButton(onClick = showAccountNumberDialog) {
                Icon(imageVector = Icons.Default.Build, contentDescription = "Change account number")
            }
        }
    )
}