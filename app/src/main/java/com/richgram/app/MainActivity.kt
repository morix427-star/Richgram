package com.richgram.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF100D18)
private val Panel = Color(0xFF1C1728)
private val Purple = Color(0xFF9B6BFF)
private val Muted = Color(0xFFAAA2B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RichGramApp() }
    }
}

@Composable
fun RichGramApp() {
    var tab by remember { mutableStateOf("Чаты") }
    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Panel, primary = Purple, onBackground = Color.White, onSurface = Color.White)) {
        Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("RichGram", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Общение без границ", color = Muted, fontSize = 12.sp)
                }
                Text("⭐  250", color = Color(0xFFFFD76A), fontWeight = FontWeight.Bold, modifier = Modifier.background(Panel, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 10.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Чаты", "Подарки", "Профиль").forEach { item ->
                    FilterChip(selected = tab == item, onClick = { tab = item }, label = { Text(item) })
                }
            }
            when (tab) { "Чаты" -> Chats(); "Подарки" -> Gifts(); else -> Profile() }
            Spacer(Modifier.weight(1f))
            Text("RichGram • ранняя версия интерфейса", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(12.dp))
        }
    }
}

@Composable
private fun Chats() {
    val chats = listOf("Избранное" to "Заметки и сохранённые сообщения", "Александр" to "Привет! Как дела?", "Друзья" to "Новое фото", "RichGram Новости" to "Добро пожаловать!")
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(chats) { (name, message) ->
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(Purple.copy(alpha = .22f), CircleShape), contentAlignment = Alignment.Center) { Text(name.take(1), color = Purple, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(message, color = Muted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun Gifts() {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Подарки", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Коллекция подарков появится после подключения официальных сервисов Telegram.", color = Muted)
        listOf("🎁  Коллекционные подарки", "⭐  Telegram Stars", "✨  Улучшение подарков").forEach {
            Text(it, color = Color.White, modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(18.dp))
        }
    }
}

@Composable
private fun Profile() {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(84.dp).background(Purple.copy(alpha = .25f), CircleShape), contentAlignment = Alignment.Center) { Text("R", color = Purple, fontSize = 36.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
        Text("Мой профиль", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Настройки аккаунта и оформления", color = Muted)
    }
}
