package com.example.antiprocrastination.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.antiprocrastination.domain.model.Task
import com.example.antiprocrastination.ui.navigation.Routes
import com.example.antiprocrastination.ui.theme.*
import com.example.antiprocrastination.ui.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private enum class TaskView { LIST, CALENDAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: AppViewModel, navController: NavController) {
    val tasks by viewModel.tasks.collectAsState()
    var currentView by remember { mutableStateOf(TaskView.LIST) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tasks", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                },
                actions = {
                    // List / Calendar toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVar)
                            .padding(2.dp)
                    ) {
                        ViewToggleButton(
                            label    = "List",
                            selected = currentView == TaskView.LIST,
                            onClick  = { currentView = TaskView.LIST }
                        )
                        ViewToggleButton(
                            label    = "Calendar",
                            selected = currentView == TaskView.CALENDAR,
                            onClick  = { currentView = TaskView.CALENDAR }
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = { navController.navigate(Routes.NEW_TASK) },
                containerColor   = Accent,
                contentColor     = Color.White,
                shape            = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Task")
            }
        },
        containerColor = Background
    ) { padding ->
        if (currentView == TaskView.LIST) {
            TaskListView(
                tasks       = tasks,
                onToggle    = viewModel::toggleTask,
                onDelete    = viewModel::deleteTask,
                modifier    = Modifier.padding(padding)
            )
        } else {
            TaskCalendarView(
                tasks    = tasks,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ── Small toggle button ────────────────────────────────────────────────────────
@Composable
private fun ViewToggleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier            = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment    = Alignment.Center
    ) {
        Text(
            label,
            style  = MaterialTheme.typography.labelMedium,
            color  = if (selected) Color.White else Muted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── LIST VIEW ─────────────────────────────────────────────────────────────────
@Composable
fun TaskListView(
    tasks: List<Task>,
    onToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null,
                    tint = Muted, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(8.dp))
                Text("No tasks yet!", color = Muted,
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header row
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Task", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                    color = Muted, fontWeight = FontWeight.SemiBold)
                Text("Due date", Modifier.width(110.dp), style = MaterialTheme.typography.labelMedium,
                    color = Muted, fontWeight = FontWeight.SemiBold)
                Text("Done", Modifier.width(48.dp), style = MaterialTheme.typography.labelMedium,
                    color = Muted, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = SurfaceVar)
            Spacer(Modifier.height(4.dp))
        }

        items(tasks, key = { it.id }) { task ->
            TaskRowItem(task = task, onToggle = onToggle, onDelete = onDelete)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRowItem(task: Task, onToggle: (Int) -> Unit, onDelete: (Int) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy")
    val isOverdue = !task.completed && task.dueDate.isBefore(LocalDate.now())

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (task.completed) SurfaceVar else CardBg
        ),
        elevation = CardDefaults.cardElevation(if (task.completed) 0.dp else 2.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.name,
                    style          = MaterialTheme.typography.bodyMedium,
                    fontWeight     = FontWeight.Medium,
                    color          = if (task.completed) Muted else OnSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }

            // Due date + days remaining
            Column(
                modifier            = Modifier.width(110.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    task.dueDate.format(formatter),
                    style  = MaterialTheme.typography.bodySmall,
                    color  = when {
                        task.completed -> Muted
                        isOverdue      -> Warning
                        else           -> OnSurface
                    }
                )
                if (!task.completed) {
                    val label = when {
                        isOverdue              -> "Overdue"
                        task.daysRemaining == 0L -> "Today"
                        else                   -> "${task.daysRemaining}d left"
                    }
                    Text(label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Warning else Accent,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            // Checkbox
            Checkbox(
                checked         = task.completed,
                onCheckedChange = { onToggle(task.id) },
                colors          = CheckboxDefaults.colors(checkedColor = Accent)
            )

            // Delete
            IconButton(onClick = { onDelete(task.id) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = Muted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── CALENDAR VIEW ─────────────────────────────────────────────────────────────
@Composable
fun TaskCalendarView(tasks: List<Task>, modifier: Modifier = Modifier) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val taskDates = tasks.map { it.dueDate }.toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month navigation header
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month",
                    tint = Primary)
            }
            Text(
                displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface
            )
            IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month",
                    tint = Primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Day-of-week labels
        val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Box(
                    modifier         = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium,
                        color = Muted, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Calendar grid
        val firstDayOfMonth = displayedMonth.atDay(1)
        val startOffset     = firstDayOfMonth.dayOfWeek.value % 7   // Sunday = 0
        val daysInMonth     = displayedMonth.lengthOfMonth()
        val totalCells      = startOffset + daysInMonth
        val rows            = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayNum = row * 7 + col - startOffset + 1
                    Box(
                        modifier         = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date     = displayedMonth.atDay(dayNum)
                            val isToday  = date == LocalDate.now()
                            val hasTasks = date in taskDates
                            val isOverdue = hasTasks && date.isBefore(LocalDate.now())

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isToday -> Primary
                                            else    -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$dayNum",
                                        style  = MaterialTheme.typography.bodySmall,
                                        color  = if (isToday) Color.White else OnSurface,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasTasks) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isOverdue) Warning else Accent)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tasks for month
        val monthTasks = tasks.filter {
            it.dueDate.month == displayedMonth.month &&
            it.dueDate.year  == displayedMonth.year
        }
        if (monthTasks.isNotEmpty()) {
            Text("This month", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = OnSurface)
            Spacer(Modifier.height(8.dp))
            monthTasks.sortedBy { it.dueDate }.forEach { task ->
                val fmt = DateTimeFormatter.ofPattern("d MMM")
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(8.dp).clip(CircleShape)
                            .background(if (task.completed) Muted else Accent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(task.name, Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Text(task.dueDate.format(fmt),
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        }
    }
}
