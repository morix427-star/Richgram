package com.richgram.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TelegramBlue = Color(0xFF3390EC)
private val Screen = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF8A929A)
private val DividerColor = Color(0xFFE9EDF0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RichGramApp() }
    }
}

private data class ChatItem(
    val name: String,
    val message: String,
    val time: String,
    val initials: String,
    val color: Color,
    val unread: Int = 0,
    val verified: Boolean = false,
    val muted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichGramApp() {
    var selectedFolder by remember { mutableStateOf("Все чаты") }
    var selectedTab by remember { mutableStateOf("Чаты") }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val chats = listOf(
        ChatItem("Избранное", "Сохранённые сообщения", "15:42", "★", Color(0xFF6B9CEB)),
        ChatItem("Telegram", "Добро пожаловать в RichGram!", "15:20", "T", Color(0xFF55A6E8), verified = true),
        ChatItem("Александр", "Привет! Как ты?", "14:58", "А", Color(0xFFE6A05C), 2),
        ChatItem("Друзья", "Мария: Отправила фотографии", "13:41", "Д", Color(0xFF8D78D8), 5, muted = true),
        ChatItem("Новости технологий", "Новые сообщения", "Вчера", "Н", Color(0xFF58B99A), 1, true),
        ChatItem("Мама ❤️", "Хорошо, до вечера!", "Вчера", "М", Color(0xFFD987A5)),
        ChatItem("Работа", "Документы отправлены", "Пн", "Р", Color(0xFF6C9CB8), muted = true)
    )

    MaterialTheme(colorScheme = lightColorScheme(primary = TelegramBlue, background = Screen, surface = Screen)) {
        Scaffold(
            containerColor = Screen,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            if (searchOpen) {
                                OutlinedTextField(
                                    value = query, onValueChange = { query = it },
                                    placeholder = { Text("Поиск") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            } else {
                                Text("RichGram", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { if (searchOpen) { searchOpen = false; query = "" } }) {
                                Icon(if (searchOpen) Icons.Default.ArrowBack else Icons.Default.Menu, contentDescription = "Меню")
                            }
                        },
                        actions = {
                            IconButton(onClick = { searchOpen = !searchOpen; query = "" }) {
                                Icon(if (searchOpen) Icons.Default.Close else Icons.Default.Search, contentDescription = "Поиск")
                            }
                            if (!searchOpen) {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Дополнительно")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Screen)
                    )
                    ScrollableTabRow(
                        selectedTabIndex = listOf("Все чаты", "Личные", "Группы", "Каналы").indexOf(selectedFolder),
                        edgePadding = 8.dp,
                        containerColor = Screen,
                        contentColor = TelegramBlue,
                        divider = { HorizontalDivider(color = DividerColor) }
                    ) {
                        listOf("Все чаты", "Личные", "Группы", "Каналы").forEachIndexed { index, label ->
                            Tab(
                                selected = selectedFolder == label,
                                onClick = { selectedFolder = label },
                                text = { Text(label, fontWeight = if (selectedFolder == label) FontWeight.SemiBold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}, containerColor = TelegramBlue, contentColor = Color.White) {
                    Icon(Icons.Default.Edit, contentDescription = "Новое сообщение")
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Screen, tonalElevation = 4.dp) {
                    listOf(
                        Triple("Контакты", Icons.Default.Contacts, "Контакты"),
                        Triple("Звонки", Icons.Default.Call, "Звонки"),
                        Triple("Чаты", Icons.Default.Chat, "Чаты"),
                        Triple("Настройки", Icons.Default.Settings, "Настройки"),
                        Triple("Админ", Icons.Default.AdminPanelSettings, "Админ")
                    ).forEach { (label, icon, key) ->
                        NavigationBarItem(
                            selected = selectedTab == key,
                            onClick = { selectedTab = key },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = TelegramBlue, indicatorColor = TelegramBlue.copy(alpha = .12f), selectedTextColor = TelegramBlue)
                        )
                    }
                }
            }
        ) { padding ->
            if (selectedTab == "Админ") {
                AdminPanel(Modifier.fillMaxSize().padding(padding))
            } else if (selectedTab == "Чаты") {
                val visibleChats = chats.filter {
                    (query.isBlank() || it.name.contains(query, true) || it.message.contains(query, true)) &&
                    (selectedFolder == "Все чаты" ||
                        (selectedFolder == "Личные" && it.name !in listOf("Друзья", "Новости технологий", "Работа")) ||
                        (selectedFolder == "Группы" && it.name in listOf("Друзья", "Работа")) ||
                        (selectedFolder == "Каналы" && it.name == "Новости технологий"))
                }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(visibleChats) { chat ->
                        ChatRow(chat)
                        HorizontalDivider(color = DividerColor, thickness = .6.dp, modifier = Modifier.padding(start = 82.dp))
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        when (selectedTab) {
                            "Контакты" -> Icons.Default.Contacts
                            "Звонки" -> Icons.Default.Call
                            "Админ" -> Icons.Default.AdminPanelSettings
                            else -> Icons.Default.Settings
                        },
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(selectedTab, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("Раздел будет подключён на следующем этапе", color = SecondaryText)
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatItem) {
    Row(
        Modifier.fillMaxWidth().height(76.dp).clickable { }.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(56.dp).background(chat.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(chat.initials, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(chat.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (chat.verified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = "Подтверждён", tint = TelegramBlue, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(chat.time, fontSize = 12.sp, color = SecondaryText)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(chat.message, fontSize = 14.sp, color = SecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (chat.muted) {
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Default.NotificationsOff, contentDescription = "Без звука", tint = SecondaryText, modifier = Modifier.size(14.dp))
                }
                if (chat.unread > 0) {
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.defaultMinSize(minWidth = 21.dp, minHeight = 21.dp).background(TelegramBlue, CircleShape).padding(horizontal = 5.dp), contentAlignment = Alignment.Center) {
                        Text(chat.unread.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}


@Composable
private fun AdminPanel(modifier: Modifier = Modifier) {
    var userId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("100") }
    var gift by remember { mutableStateOf("🌹 Роза") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Демонстрационный режим: операции пока не отправляются на сервер.") }

    Column(modifier.padding(16.dp)) {
        Text("Панель владельца", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Управление внутренними RichGram Stars и подарками", color = SecondaryText, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminStat("Пользователи", "—")
            AdminStat("Выдано Stars", "—")
            AdminStat("Подарки", "—")
        }
        Spacer(Modifier.height(18.dp))
        Text("Начислить RichGram Stars", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        OutlinedTextField(userId, { userId = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ID пользователя") }, singleLine = true)
        OutlinedTextField(amount, { amount = it.filter(Char::isDigit).take(9) }, modifier = Modifier.fillMaxWidth(), label = { Text("Количество Stars") }, singleLine = true)
        OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Комментарий (необязательно)") }, singleLine = true)
        Button(onClick = { message = if (userId.isBlank() || amount.isBlank() || amount == "0") "Укажи ID пользователя и количество Stars." else "Запрос подготовлен. Для реального начисления нужно подключить защищённый сервер." }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Star, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Начислить Stars")
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = DividerColor)
        Text("Выдать подарок", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        OutlinedTextField(gift, { gift = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Название подарка") }, singleLine = true)
        OutlinedTextField(userId, { userId = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ID получателя") }, singleLine = true)
        Button(onClick = { message = if (userId.isBlank()) "Сначала укажи ID получателя." else "Подарок выбран. Отправка станет доступна после подключения сервера." }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CardGiftcard, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Выдать подарок")
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = DividerColor)
        Text("Другие инструменты", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        listOf("Управление Premium", "Список пользователей", "История операций", "Блокировки и ограничения").forEach { label ->
            Row(Modifier.fillMaxWidth().clickable { message = "$label: подключим в следующем этапе." }.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TelegramBlue)
                Spacer(Modifier.width(8.dp))
                Text(label)
            }
            HorizontalDivider(color = DividerColor)
        }
        Spacer(Modifier.height(12.dp))
        Text(message, color = SecondaryText, fontSize = 12.sp)
    }
}

@Composable
private fun RowScope.AdminStat(title: String, value: String) {
    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F6FA))) {
        Column(Modifier.padding(10.dp)) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TelegramBlue)
            Text(title, fontSize = 10.sp, color = SecondaryText)
        }
    }
}
