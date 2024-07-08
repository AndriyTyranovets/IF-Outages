package com.github.andriytyranovets.ifoutages.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AccountNumberDialog(
    accountNumber: String?,
    onCancel: () -> Unit,
    onSave: (String) -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
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
                text = "${if(accountNumber.isNullOrEmpty()) "Set" else "Change"} account number",
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
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = { onSave(text) },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Save")
                }
            }

        }
    }
}