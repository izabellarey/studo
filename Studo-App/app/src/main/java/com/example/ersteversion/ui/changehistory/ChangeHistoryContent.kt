package com.example.ersteversion.ui.changehistory

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ersteversion.R
import com.example.ersteversion.data.model.Change
import com.example.ersteversion.ui.theme.Gelb
import com.example.ersteversion.ui.theme.PastelRot

@Composable
fun ChangeHistoryContent(uiState: ChangeHistoryUiState) {
    if (uiState.errorMessage != null) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text(stringResource(R.string.fehler, uiState.errorMessage))
        }
        return
    }

    if (uiState.changes.isEmpty() && !uiState.isLoading) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text(stringResource(R.string.keine_aenderungen_vorhanden))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.changes) { change ->
            ChangeCard(change = change)
        }
    }
}
@Composable
private fun ChangeCard(change: Change) {
    val backgroundColor = if (change.type == "AUSFALL") PastelRot else Gelb

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(30.dp)
    ) {
        Text(change.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
            change.description,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Preview(showBackground = true, heightDp = 800, name = "ChangeHistory Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "ChangeHistory Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChangeHistoryContentPreview() {
    val fakeState = ChangeHistoryUiState(
        changes = listOf(
            Change(1, "RAUMAENDERUNG", "Raumänderung: D jetzt in R102", "Am Montag, 1. Stunde: Raum geändert.", false),
            Change(2, "AUSFALL", "Unterrichtsausfall: Sport", "Am Mittwoch, 3. Stunde fällt Sport aus.", true),
            Change(3, "VERTRETUNG", "Vertretung: Mathe", "Am Dienstag, 2. Stunde übernimmt Herr Schmidt.", false)
        )
    )
    ChangeHistoryContent(uiState = fakeState)
}