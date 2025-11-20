package com.example.fitgenius.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitgenius.ui.theme.MutedGreen

@Composable
fun ProgressScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MutedGreen)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressSummarySection()
        WeightEvolutionCard()
        PersonalRecordsCard()
    }
}

@Composable
fun ProgressSummarySection() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgressSummaryChip("Racha", "7 días", "Máximo: 14", Icons.Default.Whatshot, Color(0xFFFFA000), Modifier.weight(1f))
        ProgressSummaryChip("Entrenos", "12", "Este mes", Icons.Default.FitnessCenter, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        ProgressSummaryChip("Récords", "3", "Nuevos", Icons.Default.EmojiEvents, Color.Magenta, Modifier.weight(1f))
    }
}

@Composable
fun ProgressSummaryChip(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = title, tint = iconColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun WeightEvolutionCard() {
    var weightInput by remember { mutableStateOf("") }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TrendingUp, contentDescription = "Evolución del Peso", tint = MaterialTheme.colorScheme.primary)
                Text("Evolución del Peso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Tu progreso en las últimas semanas", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(MutedGreen, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text("[Gráfico de evolución del peso]", color = Color.Gray)
            }
            Text("Registrar nuevo peso (kg)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Ej: 73.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { /* TODO: Guardar peso */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Guardar")
                    Spacer(Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun PersonalRecordsCard() {
    var exerciseInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Récords Personales", tint = Color(0xFFFFA000))
                Text("Récords Personales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Tus mejores marcas en cada ejercicio", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            RecordItem(exercise = "Sentadilla", date = "25/05/2024", weight = "100 kg")
            RecordItem(exercise = "Press de Banca", date = "23/05/2024", weight = "80 kg")
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Añadir nuevo récord", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = exerciseInput, onValueChange = { exerciseInput = it }, label = { Text("Ejercicio") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Button(onClick = { /* TODO: Guardar record */ }, colors=ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                    Text("Añadir")
                }
            }
        }
    }
}

@Composable
fun RecordItem(exercise: String, date: String, weight: String) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MutedGreen.copy(alpha=0.5f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)){
                 Icon(Icons.Default.EmojiEvents, contentDescription = null, tint=Color(0xFFFFA000), modifier = Modifier.background(Color.White, CircleShape).padding(6.dp))
                 Column {
                    Text(exercise, fontWeight = FontWeight.Bold)
                    Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Text(weight, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }
}
