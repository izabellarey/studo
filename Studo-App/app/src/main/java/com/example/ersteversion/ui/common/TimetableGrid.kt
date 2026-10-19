package com.example.ersteversion.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ersteversion.R
import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.Lesson
import com.example.ersteversion.data.model.Rasterslot
import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.ui.theme.DunkelGelb
import com.example.ersteversion.ui.theme.DunkelstesGrün
import com.example.ersteversion.ui.theme.Gelb
import com.example.ersteversion.ui.theme.PastelRot
import com.example.ersteversion.ui.theme.Rot
import java.time.Duration
import java.time.LocalTime

private val TIME_COLUMN_WIDTH = 72.dp

private val SUBJECT_COLORS = listOf(
    Color(0xFF52706F))

private fun colorForSubject(subject: String): Color =
    SUBJECT_COLORS[subject.hashCode().mod(SUBJECT_COLORS.size)]

private fun Rasterslot.durationInMinutes(): Float {
    val start = LocalTime.parse(startTime)
    val end = LocalTime.parse(endTime)
    return Duration.between(start, end).toMinutes().toFloat().coerceAtLeast(1f)
}

// Verwaltet Stundenraster für alle Plantypen

@Composable
fun TimetableGrid(
    stundenraster: Stundenraster,
    lessonAt: (Rasterslot, Day) -> Lesson?,
    subtitleLines: (Lesson, Boolean) -> List<String>,
    modifier: Modifier = Modifier
) {
    var selectedDay by rememberSaveable { mutableStateOf<Day?>(null) }
    Column(modifier = modifier.fillMaxSize()) {
        DayHeaderRow(
            selectedDay = selectedDay,
            onDayClick = { day ->
                selectedDay = if (selectedDay == day) null else day
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color(0xFFCAD2C5))
        ) {
            stundenraster.slots.forEach { slot ->
                GridRow(
                    slot = slot,
                    lessonAt = lessonAt,
                    subtitleLines = subtitleLines,
                    selectedDay = selectedDay,
                    modifier = Modifier.weight(slot.durationInMinutes())
                )
            }
        }
    }
}

@Composable
private fun DayHeaderRow(selectedDay: Day?, onDayClick: (Day) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFCAD2C5))
            .padding(vertical = 8.dp)
    ) {
        Box(modifier = Modifier.width(TIME_COLUMN_WIDTH))

        Day.entries.forEach { day ->
            val isSelected = day == selectedDay
            if (selectedDay == null || isSelected) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDayClick(day) }
                        .background(if (isSelected) Color(0xFFB2BEB5) else Color.Transparent)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day.shortName(),
                        color= DunkelstesGrün,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun GridRow(
    slot: Rasterslot,
    lessonAt: (Rasterslot, Day) -> Lesson?,
    subtitleLines: (Lesson, Boolean) -> List<String>,
    selectedDay: Day?,
    modifier: Modifier = Modifier
) {
    val rowBackground = if (slot.isPause) Color(0xFFFAFAFA) else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(rowBackground)
            .border(0.5.dp, Color(0xFFCAD2C5))
    ) {
        Box(
            modifier = Modifier
                .width(TIME_COLUMN_WIDTH)
                .fillMaxHeight()
                .padding(start = 4.dp, top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!slot.isPause) {
                Text(
                    text = "${slot.startTime} -\n${slot.endTime}",
                    fontSize = 10.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (slot.isPause) {
            Box(modifier = Modifier.weight(5f))
        } else {
            Day.entries.forEach { day ->
                if (selectedDay == null || day == selectedDay) {
                    val lesson = lessonAt(slot, day)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                    ) {
                        if (lesson != null) {
                            LessonCard(
                                lesson = lesson,
                                subtitleLines = subtitleLines(lesson, selectedDay != null),
                                expanded = selectedDay != null
                            )
                        }
                    }
                }
            }
        }
    }
}

// Die einzelnen Stunden

@Composable
private fun LessonCard(lesson: Lesson, subtitleLines: List<String>, expanded: Boolean) {
    val isAusfall = lesson.changeReason == "AUSFALL"
    val isAenderung = lesson.changed && !isAusfall

    val cardBackground = when {
        isAusfall -> PastelRot
        isAenderung -> Gelb
        else -> colorForSubject(lesson.subject)
    }

    val textColor = when {
        isAusfall -> Rot
        isAenderung -> DunkelGelb
        else -> Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(cardBackground)
            .padding(horizontal = if (expanded) 12.dp else 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            lesson.subject, color = textColor, fontWeight = FontWeight.Bold,
            fontSize = if (expanded) 14.sp else 11.sp, textAlign = TextAlign.Center,
            lineHeight = if (expanded) 15.sp else 12.sp
        )
        subtitleLines.forEach { line ->
            Text(
                line, color = textColor,
                fontSize = if (expanded) 11.sp else 9.sp, textAlign = TextAlign.Center,
                lineHeight = if (expanded) 12.sp else 10.sp,
                maxLines = 1
            )
        }

        if (isAusfall) {
            Text(
                stringResource(R.string.ausfall), color = textColor, fontWeight = FontWeight.Bold,
                fontSize = if (expanded) 10.sp else 8.sp, textAlign = TextAlign.Center,
                lineHeight = if (expanded) 11.sp else 9.sp
            )
        } else if (isAenderung) {
            Text(
                stringResource(R.string.nderung), color = textColor, fontWeight = FontWeight.Bold,
                fontSize = if (expanded) 10.sp else 8.sp, textAlign = TextAlign.Center,
                lineHeight = if (expanded) 11.sp else 9.sp
            )
        }
    }
}

@Composable
private fun Day.shortName(): String = when (this) {
    Day.MONDAY -> stringResource(R.string.mo)
    Day.TUESDAY -> stringResource(R.string.di)
    Day.WEDNESDAY -> stringResource(R.string.mi)
    Day.THURSDAY -> stringResource(R.string.don)
    Day.FRIDAY -> stringResource(R.string.fr)
}