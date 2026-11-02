package com.example.shiftschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF286B58))) { ShiftApp() } }
    }
}

private enum class Shift(val label: String, val color: Color) {
    DAY("白", Color(0xFFFFE0A3)), NIGHT("夜", Color(0xFFC9C8F4)), OFF("休", Color(0xFFBFE6D2)), DUTY("值", Color(0xFFFFC8C8))
}

@Composable
private fun ShiftApp() {
    var page by remember { mutableIntStateOf(0) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    val assignments = remember { mutableStateMapOf<LocalDate, Shift>() }
    var selected by remember { mutableStateOf(setOf<LocalDate>()) }
    var batchMode by remember { mutableStateOf(false) }
    var chosenShift by remember { mutableStateOf(Shift.DAY) }

    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(selected = page == 0, onClick = { page = 0 }, icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("班表") })
            NavigationBarItem(selected = page == 1, onClick = { page = 1 }, icon = { Icon(Icons.Default.Alarm, null) }, label = { Text("闹钟") })
            NavigationBarItem(selected = page == 2, onClick = { page = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("设置") })
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("轮班", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            when (page) {
                0 -> CalendarPage(month, { month = month.minusMonths(1); selected = emptySet() }, { month = month.plusMonths(1); selected = emptySet() }, assignments, selected, { date ->
                    if (batchMode) selected = if (date in selected) selected - date else selected + date
                    else assignments[date] = chosenShift
                }, batchMode, { batchMode = !batchMode; selected = emptySet() }, chosenShift, { chosenShift = it }, {
                    selected.forEach { assignments[it] = chosenShift }; selected = emptySet(); batchMode = false
                })
                1 -> AlarmPage()
                else -> SettingsPage()
            }
        }
    }
}

@Composable
private fun CalendarPage(month: YearMonth, prev: () -> Unit, next: () -> Unit, assignments: Map<LocalDate, Shift>, selected: Set<LocalDate>, onDay: (LocalDate) -> Unit, batch: Boolean, toggleBatch: () -> Unit, chosen: Shift, choose: (Shift) -> Unit, apply: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = prev) { Text("‹ 上月") }
        Text("${month.year}年${month.monthValue}月", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = next) { Text("下月 ›") }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = toggleBatch) { Text(if (batch) "取消多选" else "批量多选") } }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { Text(it, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray) }
    }
    val firstOffset = month.atDay(1).dayOfWeek.value - 1
    val cells: List<LocalDate?> = List(firstOffset) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.weight(1f), userScrollEnabled = false) {
        items(cells) { date ->
            Box(Modifier.aspectRatio(0.9f).padding(3.dp).then(if (date != null) Modifier.background(if (date in selected) Color(0xFFDDECE7) else Color(0xFFF5F6F5), RoundedCornerShape(10.dp)).clickable { onDay(date) } else Modifier), contentAlignment = Alignment.Center) {
                if (date != null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(date.dayOfMonth.toString(), fontSize = 14.sp)
                    assignments[date]?.let { shift -> Box(Modifier.padding(top = 3.dp).background(shift.color, RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) { Text(shift.label, fontSize = 11.sp) } }
                    if (date in selected) Text("✓", color = Color(0xFF286B58), fontSize = 11.sp)
                }
            }
        }
    }
    Text("选择班别", fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Shift.entries.forEach { shift -> FilterChip(selected = chosen == shift, onClick = { choose(shift) }, label = { Text(shift.label + "班") }) }
    }
    if (batch) Button(onClick = apply, enabled = selected.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("将所选 ${selected.size} 天设为${chosen.label}班") }
    val counts = Shift.entries.associateWith { s -> assignments.values.count { it == s } }
    Text("本月统计：白班 ${counts[Shift.DAY]} 天 · 夜班 ${counts[Shift.NIGHT]} 天 · 休息 ${counts[Shift.OFF]} 天 · 值班 ${counts[Shift.DUTY]} 天", fontSize = 12.sp, color = Color.DarkGray)
}

@Composable
private fun AlarmPage() {
    Text("班别闹钟", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("按白班、夜班、值班分别设置提醒时间。")
    listOf("白班闹钟", "夜班闹钟", "值班闹钟", "独立闹钟").forEach { title ->
        var enabled by remember { mutableStateOf(false) }
        var time by remember { mutableStateOf("07:00") }
        Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text("提醒时间：$time", color = Color.Gray, fontSize = 13.sp) }
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
        }
    }
    Text("提示：当前版本是界面原型，尚未接入系统真实闹钟。", color = Color(0xFF9A5A00), fontSize = 12.sp)
}

@Composable
private fun SettingsPage() {
    Text("设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    listOf("班别管理", "每周首日", "显示中国法定节假日", "显示传统节日", "显示二十四节气", "权限说明").forEach { item ->
        var checked by remember { mutableStateOf(item.startsWith("显示").not()) }
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item, Modifier.weight(1f))
            if (item.startsWith("显示")) Switch(checked = checked, onCheckedChange = { checked = it }) else Text("›", color = Color.Gray, fontSize = 22.sp)
        }
        HorizontalDivider()
    }
    Spacer(Modifier.height(12.dp))
    Text("版本 0.1.0 · 数据优先保存在本机", color = Color.Gray, fontSize = 12.sp)
}
