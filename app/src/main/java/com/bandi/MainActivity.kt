package com.bandi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BandiApp()
        }
    }
}

@Composable
fun BandiApp() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandi - English Coach") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                HeroSection()
            }
            item {
                SectionHeader("Poziomy")
            }
            item {
                LevelRow()
            }
            item {
                SectionHeader("Twoje lekcje")
            }
            items(sampleLessons()) { lesson ->
                LessonCard(lesson)
            }
            item {
                SectionHeader("Ćwiczenie mówienia")
            }
            item {
                SpeakingPracticeCard()
            }
        }
    }
}

@Composable
fun HeroSection() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))
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
fun LessonCard(lesson: Lesson) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE8EAF6), shape = CardDefaults.shape)
                    .padding(10.dp)
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
            ProgressPill(lesson.progress)
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
    val title: String,
    val subtitle: String,
    val progress: String
)

fun sampleLessons(): List<Lesson> = listOf(
    Lesson("Wymowa podstawowa", "Samogłoski i spółgłoski", "Nowa"),
    Lesson("Dialogi w kawiarni", "Ćwiczenie płynności", "60%"),
    Lesson("Angielski w pracy", "Słownictwo biznesowe", "40%")
)

@Preview(showBackground = true)
@Composable
fun BandiPreview() {
    BandiApp()
}
