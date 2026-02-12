package com.bandi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BandiApp(context = this)
        }
    }
}

private enum class BandiTab {
    HOME,
    PROGRESS
}

@Composable
fun BandiApp(context: Context) {
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    var selectedModule by remember { mutableStateOf<Module?>(null) }
    var currentTab by remember { mutableStateOf(BandiTab.HOME) }
    val progressMap = remember { mutableStateMapOf<String, Int>() }
    val baseModules = remember { sampleModules() }
    val modules = remember(baseModules, progressMap.toMap()) {
        applyProgress(baseModules, progressMap)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandi - English Coach") },
                navigationIcon = if (selectedLesson != null || selectedModule != null) {
                    {
                        IconButton(onClick = {
                            if (selectedLesson != null) {
                                selectedLesson = null
                            } else {
                                selectedModule = null
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Wstecz",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    null
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == BandiTab.HOME,
                    onClick = {
                        currentTab = BandiTab.HOME
                        selectedLesson = null
                        selectedModule = null
                    },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    label = { Text("Kurs") }
                )
                NavigationBarItem(
                    selected = currentTab == BandiTab.PROGRESS,
                    onClick = {
                        currentTab = BandiTab.PROGRESS
                        selectedLesson = null
                        selectedModule = null
                    },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                    label = { Text("Postęp") }
                )
            }
        }
    ) { paddingValues ->
        when {
            currentTab == BandiTab.PROGRESS -> {
                ProgressScreen(paddingValues = paddingValues, modules = modules)
            }
            selectedLesson != null -> {
                LessonScreen(
                    paddingValues = paddingValues,
                    lesson = selectedLesson!!,
                    context = context,
                    onScoreUpdated = { lessonId, score ->
                        progressMap[lessonId] = score
                    }
                )
            }
            selectedModule != null -> {
                ModuleScreen(
                    paddingValues = paddingValues,
                    module = selectedModule!!,
                    onLessonSelected = { selectedLesson = it }
                )
            }
            else -> {
                HomeScreen(
                    paddingValues = paddingValues,
                    modules = modules,
                    onModuleSelected = { selectedModule = it }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    modules: List<Module>,
    onModuleSelected: (Module) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item {
            HeroSection()
        }
        item {
            SectionHeader("Moduły kursu")
        }
        items(modules) { module ->
            ModuleCard(module, onModuleSelected)
        }
        item {
            SectionHeader("Poziomy")
        }
        item {
            LevelRow()
        }
        item {
            SectionHeader("Ćwiczenie mówienia")
        }
        item {
            SpeakingPracticeCard()
        }
    }
}

@Composable
fun ModuleScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    module: Module,
    onLessonSelected: (Lesson) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item {
            ModuleHeader(module)
        }
        items(module.lessons) { lesson ->
            LessonCard(lesson, onLessonSelected)
        }
    }
}

@Composable
fun ModuleHeader(module: Module) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5FE))
            .padding(16.dp)
    ) {
        Text(
            text = module.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = module.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF546E7A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProgressPill("${module.progressPercent}%")
    }
}

@Composable
fun ProgressScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    modules: List<Module>
) {
    val allLessons = modules.flatMap { it.lessons }
    val averageScore = if (allLessons.isNotEmpty()) {
        allLessons.map { it.progressPercent }.average().toInt()
    } else {
        0
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item {
            SectionHeader("Twój postęp")
        }
        item {
            ProgressSummaryCard(averageScore, modules.size, allLessons.size)
        }
        item {
            SectionHeader("Postęp w modułach")
        }
        items(modules) { module ->
            ProgressModuleCard(module)
        }
    }
}

@Composable
fun ProgressSummaryCard(averageScore: Int, moduleCount: Int, lessonCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Średni wynik: $averageScore%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Moduły: $moduleCount  •  Lekcje: $lessonCount")
            Spacer(modifier = Modifier.height(6.dp))
            Text("Ostatnia sesja: 12 min, 5 ćwiczeń")
        }
    }
}

@Composable
fun ProgressModuleCard(module: Module) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = module.imageRes),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(module.title, fontWeight = FontWeight.SemiBold)
                Text(module.description, style = MaterialTheme.typography.bodySmall)
            }
            ProgressPill("${module.progressPercent}%")
        }
    }
}

@Composable
fun LessonScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    lesson: Lesson,
    context: Context,
    onScoreUpdated: (String, Int) -> Unit
) {
    val ttsState = rememberTextToSpeech(context)
    val speechState = rememberSpeechRecognizer(context, lesson)

    LaunchedEffect(speechState.score, speechState.lastResult) {
        if (speechState.lastResult.isNotBlank()) {
            onScoreUpdated(lesson.id, speechState.score)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        LessonHeader(lesson)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Zdanie do wymówienia:",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Text(
                text = lesson.samplePhrase,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = {
                ttsState.speak(lesson.samplePhrase)
            }) {
                Icon(Icons.Default.VolumeUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Odtwórz")
            }
            FilledTonalButton(onClick = {
                speechState.requestPermissionAndStart()
            }) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nagraj")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Twoja odpowiedź:",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = speechState.lastResult.ifEmpty { "Brak odpowiedzi" },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PronunciationScore(score = speechState.score, feedback = speechState.feedback)
        Spacer(modifier = Modifier.height(20.dp))
        LessonExercises(lesson)
    }
}

@Composable
fun LessonHeader(lesson: Lesson) {
    Column {
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = lesson.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF607D8B)
        )
    }
}

@Composable
fun LessonExercises(lesson: Lesson) {
    Text(
        text = "Ćwiczenia dla lekcji",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(8.dp))
    ExerciseCard(
        title = "Shadowing",
        description = "Powtarzaj zdanie w tempie lektora.",
        icon = Icons.Default.RecordVoiceOver
    )
    ExerciseCard(
        title = "Pytania i odpowiedzi",
        description = "Odpowiedz pełnym zdaniem na pytania.",
        icon = Icons.Default.Mic
    )
    ExerciseCard(
        title = "Płynność",
        description = "Trenuj płynne łączenie słów w zdaniu.",
        icon = Icons.Default.PlayArrow
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Wskazówka: zwróć uwagę na rytm i intonację w " + lesson.samplePhrase,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF546E7A)
    )
}

@Composable
fun ExerciseCard(title: String, description: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3949AB))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun PronunciationScore(score: Int, feedback: String) {
    val badgeColor = when {
        score >= 85 -> Color(0xFFC8E6C9)
        score >= 60 -> Color(0xFFFFF9C4)
        else -> Color(0xFFFFCDD2)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(badgeColor, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Ocena wymowy: $score%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = feedback, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun HeroSection() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(20.dp)
    ) {
        Text(
            text = "Perfekcyjna wymowa",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Trenuj mowę, słuch i płynność z lekcjami dopasowanymi do Ciebie.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun LevelRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LevelChip("Podstawy")
        LevelChip("Średniozaaw.")
        LevelChip("Zaawans.")
    }
}

@Composable
fun LevelChip(label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ModuleCard(module: Module, onModuleSelected: (Module) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onModuleSelected(module) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = module.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE8EAF6), shape = RoundedCornerShape(12.dp))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF607D8B)
                )
            }
            ProgressPill("${module.progressPercent}%")
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson, onLessonSelected: (Lesson) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onLessonSelected(lesson) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = lesson.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE8EAF6), shape = RoundedCornerShape(12.dp))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lesson.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF607D8B)
                )
            }
            ProgressPill("${lesson.progressPercent}%")
        }
    }
}

@Composable
fun ProgressPill(progress: String) {
    Text(
        text = progress,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF1B5E20),
        modifier = Modifier
            .background(Color(0xFFC8E6C9), shape = CardDefaults.shape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
fun SpeakingPracticeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Powiedz: \"I would like a cup of coffee.\"",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionIcon(Icons.Default.VolumeUp, "Odsłuch")
                ActionIcon(Icons.Default.Mic, "Nagraj")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ostatnia próba: 82% poprawności",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4A148C)
            )
        }
    }
}

@Composable
fun ActionIcon(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White, shape = CardDefaults.shape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF512DA8))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

data class Lesson(
    val id: String,
    val title: String,
    val subtitle: String,
    val progressPercent: Int,
    val samplePhrase: String,
    val level: LessonLevel,
    val targetDurationSec: Double,
    val focusWords: List<String>,
    @DrawableRes val imageRes: Int
)

data class Module(
    val id: String,
    val title: String,
    val description: String,
    val level: LessonLevel,
    val lessons: List<Lesson>,
    val progressPercent: Int,
    @DrawableRes val imageRes: Int
)

enum class LessonLevel {
    BASIC,
    INTERMEDIATE,
    ADVANCED
}

fun sampleModules(): List<Module> {
    val basics = listOf(
        Lesson(
            id = "basics_pronunciation",
            title = "Podstawy wymowy",
            subtitle = "Samogłoski i spółgłoski",
            progressPercent = 15,
            samplePhrase = "Good morning. How are you today?",
            level = LessonLevel.BASIC,
            targetDurationSec = 3.8,
            focusWords = listOf("good", "morning", "today"),
            imageRes = R.drawable.ic_vocab
        ),
        Lesson(
            id = "basics_intro",
            title = "Przedstawianie się",
            subtitle = "Pierwsze rozmowy",
            progressPercent = 20,
            samplePhrase = "My name is Anna and I live in Warsaw.",
            level = LessonLevel.BASIC,
            targetDurationSec = 4.2,
            focusWords = listOf("name", "live", "warsaw"),
            imageRes = R.drawable.ic_dialog
        ),
        Lesson(
            id = "basics_spelling",
            title = "Alfabet i literowanie",
            subtitle = "Spell it out",
            progressPercent = 12,
            samplePhrase = "Could you spell your last name for me?",
            level = LessonLevel.BASIC,
            targetDurationSec = 4.4,
            focusWords = listOf("spell", "last", "name"),
            imageRes = R.drawable.ic_vocab
        ),
        Lesson(
            id = "basics_questions",
            title = "Pytania podstawowe",
            subtitle = "Where, what, how",
            progressPercent = 18,
            samplePhrase = "Where is the nearest bus stop?",
            level = LessonLevel.BASIC,
            targetDurationSec = 3.6,
            focusWords = listOf("where", "nearest", "stop"),
            imageRes = R.drawable.ic_dialog
        ),
        Lesson(
            id = "basics_family",
            title = "Rodzina i relacje",
            subtitle = "Opisywanie bliskich",
            progressPercent = 10,
            samplePhrase = "My sister lives in Krakow and works as a nurse.",
            level = LessonLevel.BASIC,
            targetDurationSec = 5.1,
            focusWords = listOf("sister", "works", "nurse"),
            imageRes = R.drawable.ic_vocab
        )
    )

    val travel = listOf(
        Lesson(
            id = "travel_shopping",
            title = "Zakupy w sklepie",
            subtitle = "Zwroty praktyczne",
            progressPercent = 45,
            samplePhrase = "Could you help me find this size?",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.0,
            focusWords = listOf("help", "find", "size"),
            imageRes = R.drawable.ic_vocab
        ),
        Lesson(
            id = "travel_airport",
            title = "Na lotnisku",
            subtitle = "Odprawa i pytania",
            progressPercent = 35,
            samplePhrase = "I have a connecting flight to London.",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.3,
            focusWords = listOf("connecting", "flight", "london"),
            imageRes = R.drawable.ic_travel
        ),
        Lesson(
            id = "travel_hotel",
            title = "Hotel i rezerwacja",
            subtitle = "Check-in",
            progressPercent = 28,
            samplePhrase = "I have a reservation under the name Kowalski.",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.8,
            focusWords = listOf("reservation", "under", "name"),
            imageRes = R.drawable.ic_travel
        ),
        Lesson(
            id = "travel_restaurant",
            title = "Restauracja",
            subtitle = "Zamawianie jedzenia",
            progressPercent = 32,
            samplePhrase = "Could I have the chicken salad, please?",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.0,
            focusWords = listOf("chicken", "salad", "please"),
            imageRes = R.drawable.ic_travel
        ),
        Lesson(
            id = "travel_transport",
            title = "Transport miejski",
            subtitle = "Bilety i kierunki",
            progressPercent = 22,
            samplePhrase = "Does this bus go to the city center?",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.1,
            focusWords = listOf("bus", "city", "center"),
            imageRes = R.drawable.ic_travel
        )
    )

    val business = listOf(
        Lesson(
            id = "business_meeting",
            title = "Dialog w pracy",
            subtitle = "Spotkanie zespołu",
            progressPercent = 60,
            samplePhrase = "Let us review the project timeline together.",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.6,
            focusWords = listOf("review", "project", "timeline"),
            imageRes = R.drawable.ic_dialog
        ),
        Lesson(
            id = "business_reporting",
            title = "Raportowanie",
            subtitle = "Statusy i wyniki",
            progressPercent = 40,
            samplePhrase = "The campaign exceeded our targets this quarter.",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.5,
            focusWords = listOf("campaign", "targets", "quarter"),
            imageRes = R.drawable.ic_business
        ),
        Lesson(
            id = "business_presentation",
            title = "Prezentacja",
            subtitle = "Mowa formalna",
            progressPercent = 10,
            samplePhrase = "Today I will present the quarterly results.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 4.7,
            focusWords = listOf("present", "quarterly", "results"),
            imageRes = R.drawable.ic_presentation
        ),
        Lesson(
            id = "business_client",
            title = "Rozmowa z klientem",
            subtitle = "Budowanie relacji",
            progressPercent = 25,
            samplePhrase = "We appreciate your feedback and will follow up shortly.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 5.1,
            focusWords = listOf("appreciate", "feedback", "follow"),
            imageRes = R.drawable.ic_business
        ),
        Lesson(
            id = "business_online",
            title = "Spotkanie online",
            subtitle = "Zasady i podsumowanie",
            progressPercent = 18,
            samplePhrase = "Let us wrap up and outline the next steps.",
            level = LessonLevel.INTERMEDIATE,
            targetDurationSec = 4.2,
            focusWords = listOf("wrap", "outline", "steps"),
            imageRes = R.drawable.ic_business
        )
    )

    val advanced = listOf(
        Lesson(
            id = "advanced_speaking",
            title = "Wystąpienie publiczne",
            subtitle = "Pewność i płynność",
            progressPercent = 5,
            samplePhrase = "Thank you for your attention and thoughtful questions.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 4.9,
            focusWords = listOf("attention", "thoughtful", "questions"),
            imageRes = R.drawable.ic_presentation
        ),
        Lesson(
            id = "advanced_negotiation",
            title = "Negocjacje",
            subtitle = "Ton i perswazja",
            progressPercent = 12,
            samplePhrase = "We can reach an agreement that benefits both sides.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 5.2,
            focusWords = listOf("agreement", "benefits", "sides"),
            imageRes = R.drawable.ic_fluency
        ),
        Lesson(
            id = "advanced_debate",
            title = "Debata",
            subtitle = "Argumentacja",
            progressPercent = 8,
            samplePhrase = "I respect your view, yet I see it differently.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 5.0,
            focusWords = listOf("respect", "view", "differently"),
            imageRes = R.drawable.ic_fluency
        ),
        Lesson(
            id = "advanced_storytelling",
            title = "Storytelling",
            subtitle = "Narracja i emocje",
            progressPercent = 6,
            samplePhrase = "The turning point came when we chose to act.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 5.1,
            focusWords = listOf("turning", "point", "act"),
            imageRes = R.drawable.ic_fluency
        ),
        Lesson(
            id = "advanced_expert",
            title = "Wystąpienie eksperckie",
            subtitle = "Precyzja i pewność",
            progressPercent = 4,
            samplePhrase = "Our findings highlight a significant shift in behavior.",
            level = LessonLevel.ADVANCED,
            targetDurationSec = 5.4,
            focusWords = listOf("findings", "significant", "behavior"),
            imageRes = R.drawable.ic_presentation
        )
    )

    return listOf(
        Module(
            id = "module_basics",
            title = "Start z wymową",
            description = "Podstawy dykcji i intonacji.",
            level = LessonLevel.BASIC,
            lessons = basics,
            progressPercent = 18,
            imageRes = R.drawable.ic_vocab
        ),
        Module(
            id = "module_travel",
            title = "Podróże i zakupy",
            description = "Praktyczne dialogi w ruchu.",
            level = LessonLevel.INTERMEDIATE,
            lessons = travel,
            progressPercent = 40,
            imageRes = R.drawable.ic_travel
        ),
        Module(
            id = "module_business",
            title = "Business English",
            description = "Spotkania, prezentacje, formalny styl.",
            level = LessonLevel.INTERMEDIATE,
            lessons = business,
            progressPercent = 35,
            imageRes = R.drawable.ic_business
        ),
        Module(
            id = "module_advanced",
            title = "Zaawansowana płynność",
            description = "Wystąpienia i negocjacje.",
            level = LessonLevel.ADVANCED,
            lessons = advanced,
            progressPercent = 9,
            imageRes = R.drawable.ic_fluency
        )
    )
}

private fun applyProgress(
    modules: List<Module>,
    progressMap: Map<String, Int>
): List<Module> {
    return modules.map { module ->
        val updatedLessons = module.lessons.map { lesson ->
            val updatedProgress = progressMap[lesson.id] ?: lesson.progressPercent
            lesson.copy(progressPercent = updatedProgress)
        }
        val avgProgress = if (updatedLessons.isNotEmpty()) {
            updatedLessons.map { it.progressPercent }.average().toInt()
        } else {
            module.progressPercent
        }
        module.copy(lessons = updatedLessons, progressPercent = avgProgress)
    }
}

@Preview(showBackground = true)
@Composable
fun BandiPreview() {
    BandiApp(context = androidx.compose.ui.platform.LocalContext.current)
}

class TextToSpeechState(
    private val textToSpeech: TextToSpeech?
) {
    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts")
    }

    fun shutdown() {
        textToSpeech?.shutdown()
    }
}

@Composable
fun rememberTextToSpeech(context: Context): TextToSpeechState {
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
        }
    }

    return remember(tts) { TextToSpeechState(tts) }
}

class SpeechState(
    private val context: Context,
    private val lesson: Lesson
) {
    var lastResult by mutableStateOf("")
        private set
    var score by mutableStateOf(0)
        private set
    var feedback by mutableStateOf("Nagraj wypowiedź, aby zobaczyć wynik.")
        private set

    private var recognizer: SpeechRecognizer? = null
    private var requestPermission: (() -> Unit)? = null
    private var speechStartMs: Long? = null
    private var speechEndMs: Long? = null

    fun setRequestPermission(handler: () -> Unit) {
        requestPermission = handler
    }

    fun requestPermissionAndStart() {
        requestPermission?.invoke()
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            feedback = "Rozpoznawanie mowy nie jest dostępne na tym urządzeniu."
            return
        }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                feedback = "Słucham..."
                speechStartMs = null
                speechEndMs = null
            }

            override fun onBeginningOfSpeech() {
                feedback = "Mów teraz..."
                speechStartMs = System.currentTimeMillis()
            }

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                speechEndMs = System.currentTimeMillis()
            }

            override fun onError(error: Int) {
                feedback = "Nie udało się rozpoznać mowy. Spróbuj ponownie."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull().orEmpty()
                if (speechEndMs == null) {
                    speechEndMs = System.currentTimeMillis()
                }
                updateScore(recognized)
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        recognizer?.startListening(intent)
    }

    private fun updateScore(recognized: String) {
        lastResult = recognized
        if (recognized.isBlank()) {
            score = 0
            feedback = "Nie rozpoznano wypowiedzi."
            return
        }
        val targetWords = lesson.samplePhrase.normalizeForScoring()
        val userWords = recognized.normalizeForScoring()
        if (targetWords.isEmpty()) {
            score = 0
            feedback = "Brak frazy wzorcowej."
            return
        }
        val matched = targetWords.count { userWords.contains(it) }
        val wordScore = matched.toDouble() / targetWords.size.toDouble()
        val targetPhonetic = lesson.samplePhrase.phoneticKey()
        val userPhonetic = recognized.phoneticKey()
        val phoneticScore = similarityRatio(targetPhonetic, userPhonetic)
        val tempoScore = tempoScore()
        val accentScore = focusWordsScore(userWords)
        val combined = (wordScore * 0.3) + (phoneticScore * 0.4) + (tempoScore * 0.2) + (accentScore * 0.1)
        score = (combined * 100).toInt().coerceIn(0, 100)
        val tempoHint = when {
            tempoScore >= 0.9 -> "Tempo bardzo dobre."
            tempoScore >= 0.7 -> "Tempo OK, ale postaraj się mówić równiej."
            else -> "Tempo wymaga poprawy – mów pewniej i płynniej."
        }
        feedback = when {
            score >= 85 -> "Świetnie! Wymowa bardzo bliska wzorcowej. $tempoHint"
            score >= 60 -> "Dobrze, ale spróbuj wyraźniej wymówić brakujące słowa. $tempoHint"
            else -> "Potrzebujesz więcej ćwiczeń. Skup się na akcentowaniu słów. $tempoHint"
        }
    }

    private fun tempoScore(): Double {
        val start = speechStartMs
        val end = speechEndMs
        if (start == null || end == null) return 0.7
        val durationSec = max(0.6, (end - start).toDouble() / 1000.0)
        val expected = lesson.targetDurationSec
        val diffRatio = abs(durationSec - expected) / expected
        return (1.0 - diffRatio).coerceIn(0.0, 1.0)
    }

    private fun focusWordsScore(userWords: List<String>): Double {
        if (lesson.focusWords.isEmpty()) return 0.8
        val matched = lesson.focusWords.count { userWords.contains(it.lowercase(Locale.US)) }
        return matched.toDouble() / lesson.focusWords.size.toDouble()
    }

    fun dispose() {
        recognizer?.destroy()
    }

    fun setFeedback(message: String) {
        feedback = message
    }
}

@Composable
fun rememberSpeechRecognizer(context: Context, lesson: Lesson): SpeechState {
    val speechState = remember { SpeechState(context, lesson) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechState.startListening()
        } else {
            speechState.setFeedback("Brak zgody na mikrofon.")
        }
    }

    speechState.setRequestPermission {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechState.dispose()
        }
    }

    return speechState
}

private fun String.normalizeForScoring(): List<String> {
    return lowercase(Locale.US)
        .replace(Regex("[^a-z\\s]"), "")
        .split(" ")
        .filter { it.isNotBlank() }
}

private fun String.phoneticKey(): String {
    return lowercase(Locale.US)
        .replace("th", "θ")
        .replace("sh", "ʃ")
        .replace("ch", "tʃ")
        .replace("ph", "f")
        .replace("ck", "k")
        .replace("qu", "kw")
        .replace(Regex("[^a-zθʃtʃkw]"), " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replace(Regex("[aeiouy]"), "")
        }
        .trim()
}

private fun similarityRatio(a: String, b: String): Double {
    if (a.isBlank() || b.isBlank()) return 0.0
    val distance = levenshteinDistance(a, b)
    val maxLen = max(a.length, b.length).coerceAtLeast(1)
    return 1.0 - (distance.toDouble() / maxLen.toDouble())
}

private fun levenshteinDistance(a: String, b: String): Int {
    val dp = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 0..a.length) {
        dp[i][0] = i
    }
    for (j in 0..b.length) {
        dp[0][j] = j
    }
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[i][j] = min(
                min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                dp[i - 1][j - 1] + cost
            )
        }
    }
    return dp[a.length][b.length]
}
