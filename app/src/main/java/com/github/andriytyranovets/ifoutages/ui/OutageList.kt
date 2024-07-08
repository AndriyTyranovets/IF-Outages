package com.github.andriytyranovets.ifoutages.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.github.andriytyranovets.ifoutages.config.Formatter
import com.github.andriytyranovets.ifoutages.models.OutageDuration
import com.github.andriytyranovets.ifoutages.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OutageList(
    viewModel: MainViewModel,
    accountNumber: String?,
    onEditAccountNumber: () -> Unit,

) {
    val outages by viewModel.outages.observeAsState(emptyMap())
    val lastUpdate by viewModel.lastUpdate.collectAsState()

    LaunchedEffect(Unit) {
        if(accountNumber != null) {
            viewModel.fetchOutages(accountNumber)
        }
    }
    Column(modifier = Modifier.fillMaxHeight()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),

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
        if (lastUpdate != null) {
            Text(
                text = "Last update: ${Formatter.DateTime.format(lastUpdate)}",
                style = TextStyle.Default.copy(
                    color = Color.LightGray,
                    fontStyle = FontStyle.Italic
                )
            )
        }
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
                text = "${duration.begin.toLocalTime().format(Formatter.Time)} - ${duration.end.toLocalTime().format(
                    Formatter.Time)}",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(text = "${duration.duration()} hour${if (duration.duration() > 1) "s" else ""}")
        }
    }
}