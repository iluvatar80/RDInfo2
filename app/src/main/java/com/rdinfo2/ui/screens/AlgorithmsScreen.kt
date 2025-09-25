// app/src/main/java/com/rdinfo2/ui/screens/AlgorithmsScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rdinfo2.data.model.Algorithm
import com.rdinfo2.data.model.AlgorithmCategory
import com.rdinfo2.data.model.AlgorithmStep

@Composable
fun AlgorithmsScreen() {
    var selectedCategory by remember { mutableStateOf<AlgorithmCategory?>(null) }
    var selectedAlgorithm by remember { mutableStateOf<Algorithm?>(null) }

    when {
        selectedAlgorithm != null -> {
            // Detailed algorithm flowchart view
            AlgorithmDetailView(
                algorithm = selectedAlgorithm!!,
                onBack = { selectedAlgorithm = null }
            )
        }
        selectedCategory != null -> {
            // Algorithm list for selected category
            AlgorithmListView(
                category = selectedCategory!!,
                onBack = { selectedCategory = null },
                onAlgorithmSelected = { selectedAlgorithm = it }
            )
        }
        else -> {
            // Main xABCDE category overview
            AlgorithmCategoryView(
                onCategorySelected = { selectedCategory = it }
            )
        }
    }
}

@Composable
private fun AlgorithmCategoryView(
    onCategorySelected: (AlgorithmCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "xABCDE Algorithmen",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Text(
                text = "Systematische Patientenversorgung nach Hamburger Rettungsdienst-Handbuch",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(getAlgorithmCategories()) { category ->
            AlgorithmCategoryCard(
                category = category,
                onClick = { onCategorySelected(category) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AdditionalReferencesCard()
        }
    }
}

@Composable
private fun AlgorithmCategoryCard(
    category: AlgorithmCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = category.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(category.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.shortName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Öffnen",
                    tint = category.color
                )
            }

            // Algorithm count and examples
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${category.algorithms.size} Algorithmen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (category.algorithms.isNotEmpty()) {
                    Text(
                        text = category.algorithms.take(2).joinToString(", ") { it.shortName },
                        style = MaterialTheme.typography.labelMedium,
                        color = category.color
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlgorithmListView(
    category: AlgorithmCategory,
    onBack: () -> Unit,
    onAlgorithmSelected: (Algorithm) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Column {
                    Text(category.shortName)
                    Text(
                        text = category.fullName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Zurück")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = category.color,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(category.algorithms) { algorithm ->
                AlgorithmCard(
                    algorithm = algorithm,
                    categoryColor = category.color,
                    onClick = { onAlgorithmSelected(algorithm) }
                )
            }
        }
    }
}

@Composable
private fun AlgorithmCard(
    algorithm: Algorithm,
    categoryColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = algorithm.shortName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                        Text(
                            text = algorithm.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    algorithm.indication?.let { indication ->
                        Text(
                            text = indication,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Starten",
                    tint = categoryColor
                )
            }

            // Key steps preview
            if (algorithm.steps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Erste Schritte:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    algorithm.steps.take(2).forEach { step ->
                        Text(
                            text = "• ${step.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (algorithm.steps.size > 2) {
                        Text(
                            text = "... und ${algorithm.steps.size - 2} weitere Schritte",
                            style = MaterialTheme.typography.bodySmall,
                            color = categoryColor
                        )
                    }
                }
            }

            // Tags for special features
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (algorithm.hasTimer) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Timer", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = categoryColor.copy(alpha = 0.2f),
                            labelColor = categoryColor
                        )
                    )
                }
                if (algorithm.hasMedications) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Medikamente", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = categoryColor.copy(alpha = 0.2f),
                            labelColor = categoryColor
                        )
                    )
                }
                if (algorithm.requiresEquipment.isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Equipment", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = categoryColor.copy(alpha = 0.2f),
                            labelColor = categoryColor
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlgorithmDetailView(
    algorithm: Algorithm,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Column {
                    Text(algorithm.shortName)
                    Text(
                        text = algorithm.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Zurück")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Algorithm info
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Algorithmus-Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        algorithm.indication?.let {
                            Text("Indikation: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                        algorithm.targetGroup?.let {
                            Text("Zielgruppe: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (algorithm.requiresEquipment.isNotEmpty()) {
                            Text(
                                "Equipment: ${algorithm.requiresEquipment.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Algorithm steps
            items(algorithm.steps) { step ->
                AlgorithmStepCard(step = step)
            }

            // Placeholder for interactive flowchart
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🚧 Interaktiver Flowchart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Die interaktive Flowchart-Darstellung wird hier implementiert. " +
                                    "Mit Ja/Nein-Buttons und dynamischer Navigation durch die Behandlungsschritte.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Button(
                            onClick = { /* TODO: Start interactive flowchart */ }
                        ) {
                            Text("Flowchart starten")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlgorithmStepCard(step: AlgorithmStep) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (step.type) {
                "decision" -> MaterialTheme.colorScheme.tertiaryContainer
                "action" -> MaterialTheme.colorScheme.primaryContainer
                "medication" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schritt ${step.stepNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                step.timeLimit?.let { time ->
                    Text(
                        text = "${time}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge
            )

            step.details?.let { details ->
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Decision options
            if (step.type == "decision" && step.options.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    step.options.forEach { option ->
                        Text(
                            text = "→ ${option.text}: ${option.nextStep}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Medications
            if (step.medications.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Medikamente:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    step.medications.forEach { med ->
                        Text(
                            text = "• $med",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdditionalReferencesCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Zusätzliche Referenzen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            listOf(
                "SEPSIS-Score" to "Sepsis erkennen und behandeln",
                "ISOBAR-Schema" to "Strukturierte Übergabe",
                "GP-START" to "Sichtung und Triage",
                "Toxidrome" to "Vergiftungsmerkmale",
                "EKG-Interpretation" to "Hochrisiko-EKG erkennen"
            ).forEach { (title, description) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { /* TODO: Navigate to reference */ }) {
                        Text("Öffnen")
                    }
                }
            }
        }
    }
}

// Sample data - will be replaced with real data from AlgorithmModels.kt
private fun getAlgorithmCategories(): List<AlgorithmCategory> {
    return listOf(
        AlgorithmCategory(
            id = "x",
            shortName = "(x)",
            fullName = "Extreme Bleeding / Critical",
            description = "Lebensbedrohliche Zustände",
            color = Color(0xFFD32F2F),
            algorithms = listOf(
                Algorithm(
                    id = "x1.1",
                    shortName = "x1.1",
                    name = "Kreislaufstillstand Erwachsene",
                    indication = "Herz-Kreislauf-Stillstand bei Erwachsenen",
                    targetGroup = "Erwachsene",
                    hasTimer = true,
                    hasMedications = true,
                    requiresEquipment = listOf("Defibrillator", "Beatmungsbeutel"),
                    steps = listOf(
                        AlgorithmStep(
                            stepNumber = 1,
                            type = "decision",
                            description = "Bewusstseinsprüfung und Pulskontrolle",
                            details = "Patient ansprechen und schütteln. Puls 10s tasten.",
                            timeLimit = 10
                        ),
                        AlgorithmStep(
                            stepNumber = 2,
                            type = "action",
                            description = "Herzdruckmassage beginnen",
                            details = "30:2 Verhältnis, 100-120/min, 5-6cm tief"
                        )
                    )
                ),
                Algorithm(
                    id = "x4",
                    shortName = "x4",
                    name = "Lebensbedrohliche Blutungen",
                    indication = "Massive externe Blutung",
                    targetGroup = "Alle Altersgruppen",
                    hasMedications = false,
                    requiresEquipment = listOf("Tourniquet", "Druckverband"),
                    steps = listOf()
                )
            )
        ),
        AlgorithmCategory(
            id = "a",
            shortName = "A",
            fullName = "Airway",
            description = "Atemweg freimachen und sichern",
            color = Color(0xFF1976D2),
            algorithms = listOf(
                Algorithm(
                    id = "a1",
                    shortName = "A1",
                    name = "Freimachen der Atemwege",
                    indication = "Atemwegsverlegung",
                    targetGroup = "Alle Altersgruppen",
                    steps = listOf()
                ),
                Algorithm(
                    id = "a2",
                    shortName = "A2",
                    name = "Schwellung obere Atemwege",
                    indication = "Anaphylaxie, thermische/toxische Schädigung",
                    targetGroup = "Alle Altersgruppen",
                    hasMedications = true,
                    steps = listOf()
                )
            )
        ),
        AlgorithmCategory(
            id = "b",
            shortName = "B",
            fullName = "Breathing",
            description = "Atmung und Beatmung",
            color = Color(0xFF388E3C),
            algorithms = listOf(
                Algorithm(
                    id = "b1",
                    shortName = "B1",
                    name = "Spannungspneumothorax",
                    indication = "Verdacht auf Spannungspneumothorax",
                    targetGroup = "Alle Altersgruppen",
                    steps = listOf()
                ),
                Algorithm(
                    id = "b2",
                    shortName = "B2",
                    name = "Akute Linksherzinsuffizienz",
                    indication = "Lungenödem, akute Herzinsuffizienz",
                    targetGroup = "Erwachsene",
                    hasMedications = true,
                    steps = listOf()
                )
            )
        ),
        AlgorithmCategory(
            id = "c",
            shortName = "C",
            fullName = "Circulation",
            description = "Kreislauf und Perfusion",
            color = Color(0xFFF57C00),
            algorithms = listOf(
                Algorithm(
                    id = "c7",
                    shortName = "C7",
                    name = "Akutes Koronarsyndrom",
                    indication = "Verdacht auf Herzinfarkt",
                    targetGroup = "Erwachsene",
                    hasMedications = true,
                    steps = listOf()
                )
            )
        ),
        AlgorithmCategory(
            id = "d",
            shortName = "D",
            fullName = "Disability",
            description = "Neurologie und Bewusstsein",
            color = Color(0xFF7B1FA2),
            algorithms = listOf()
        ),
        AlgorithmCategory(
            id = "e",
            shortName = "E",
            fullName = "Exposure/Environment",
            description = "Erweiterte Versorgung",
            color = Color(0xFF5D4037),
            algorithms = listOf()
        )
    )
}