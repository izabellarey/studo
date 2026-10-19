package com.example.ersteversion.ui.roomplan

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.Lesson
import com.example.ersteversion.data.model.Rasterslot
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.RoomCategory
import com.example.ersteversion.data.model.RoomPlan
import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.ui.common.TimetableGrid

@Composable
fun RoomPlanContent(
    uiState: RoomPlanUiState,
    onRoomClick: (Room) -> Unit,
    onBackClick: () -> Unit
) {
    if (uiState.selectedRoom != null && uiState.selectedRoomPlan != null) {
            TimetableGrid(
                stundenraster = uiState.stundenraster,
                lessonAt = { slot, day ->
                    uiState.selectedRoomPlan.lessons.find { it.day == day && it.rasterIndex == slot.index }
                },
                subtitleLines = { lesson, expanded -> if (expanded) listOf(lesson.teacher, lesson.schoolClass) else listOf(lesson.teacher)},
                modifier = Modifier.fillMaxSize()
            )

    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .testTag("categoryList")
        ) {
            itemsIndexed(uiState.categories) { index, category ->
                CategoryRow(category = category, onRoomClick = onRoomClick)

                if (index != uiState.categories.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color(0xFFCAD2C5)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(category: RoomCategory, onRoomClick: (Room) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 30.dp)) {
        Text(
            category.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(category.rooms) { room ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF354F52))
                        .clickable { onRoomClick(room) }
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(room.name, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Preview(showBackground = true, heightDp = 800, name = "RoomsOverview Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "RoomsOverview Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoomPlanContentOverviewPreview() {
    val fakeState = RoomPlanUiState(
        categories = listOf(
            RoomCategory(
                id = "1", name = "Hauptgebäude",
                rooms = listOf(Room("101", "101"), Room("102", "102"), Room("103", "103"))
            ),
            RoomCategory(
                id = "2", name = "Chemieräume",
                rooms = listOf(Room("C1", "Chemie 1"), Room("C2", "Chemie 2"))
            ),
            RoomCategory(
                id = "3", name = "Biologieräume",
                rooms = listOf(Room("B1", "Bio 1"))
            )
        )
    )

    RoomPlanContent(uiState = fakeState, onRoomClick = {}, onBackClick = {})
}

@Preview(showBackground = true, heightDp = 800, name = "RoomPlan Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "RoomPlan Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoomPlanContentDetailPreview() {
    val raum = Room("101", "101")

    val fakeState = RoomPlanUiState(
        selectedRoom = raum,
        selectedRoomPlan = RoomPlan(
            room = raum,
            lessons = listOf(
                Lesson(subject = "Mathe", teacher = "Frau Müller", schoolClass = "9a", room = raum, day = Day.MONDAY, rasterIndex = 1),
                Lesson(subject = "Chemie", teacher = "Herr Bauer", schoolClass = "10b", room = raum, day = Day.TUESDAY, rasterIndex = 2),
                Lesson(subject = "Sport", teacher = "Herr Klein", schoolClass = "9c", room = raum, day = Day.WEDNESDAY, rasterIndex = 3)
            )
        ),
        stundenraster = Stundenraster(
            slots = listOf(
                Rasterslot(index = 1, startTime = "08:00", endTime = "08:45", isPause = false),
                Rasterslot(index = 2, startTime = "08:50", endTime = "09:35", isPause = false),
                Rasterslot(index = 0, startTime = "09:35", endTime = "09:50", isPause = true),
                Rasterslot(index = 3, startTime = "09:50", endTime = "10:35", isPause = false)
            )
        )
    )

    RoomPlanContent(uiState = fakeState, onRoomClick = {}, onBackClick = {})
}