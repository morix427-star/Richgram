package com.richgram.app

import android.content.Context
import io.github.tdlibandroid.ktx.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TelegramMessageItem(
    val id: Long,
    val text: String,
    val outgoing: Boolean,
    val time: String
)

data class TelegramChatItem(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

class TelegramSession(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val client: TdClient

    private val _authState = MutableStateFlow("Подключаем Telegram…")
    val authState: StateFlow<String> = _authState.asStateFlow()
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()
    private val _chats = MutableStateFlow<List<TelegramChatItem>>(emptyList())
    val chats: StateFlow<List<TelegramChatItem>> = _chats.asStateFlow()
    private val chatCache = linkedMapOf<Long, TdApi.Chat>()
    private val messageCache = linkedMapOf<Long, MutableList<TelegramMessageItem>>()
    private val _messages = MutableStateFlow<List<TelegramMessageItem>>(emptyList())
    val messages: StateFlow<List<TelegramMessageItem>> = _messages.asStateFlow()
    private val _selectedChat = MutableStateFlow<TelegramChatItem?>(null)
    val selectedChat: StateFlow<TelegramChatItem?> = _selectedChat.asStateFlow()

    init {
        System.loadLibrary("tdjni")
        client = TdClient(
            filesDir = context.filesDir.absolutePath + "/richgram-tdlib",
            verbosityLevel = 1,
            apiId = BuildConfig.TELEGRAM_API_ID,
            apiHash = BuildConfig.TELEGRAM_API_HASH
        )
        client.init()
        scope.launch {
            client.updates.collect { update ->
                when (update) {
                    is TdApi.UpdateAuthorizationState -> {
                        if (update.authorizationState is TdApi.AuthorizationStateWaitTdlibParameters) {
                            client.send(TdApi.SetTdlibParameters().apply {
                                useTestDc = false
                                databaseDirectory = context.filesDir.absolutePath + "/richgram-tdlib/db"
                                filesDirectory = context.filesDir.absolutePath + "/richgram-tdlib/files"
                                useFileDatabase = true
                                useChatInfoDatabase = true
                                useMessageDatabase = true
                                useSecretChats = true
                                apiId = BuildConfig.TELEGRAM_API_ID
                                apiHash = BuildConfig.TELEGRAM_API_HASH
                                systemLanguageCode = Locale.getDefault().language
                                deviceModel = android.os.Build.MODEL
                                systemVersion = android.os.Build.VERSION.RELEASE
                                applicationVersion = "0.1.0"
                            })
                        }
                        _authState.value = when (update.authorizationState) {
                            is TdApi.AuthorizationStateWaitTdlibParameters -> "Подготовка базы Telegram…"
                            is TdApi.AuthorizationStateWaitPhoneNumber -> "phone"
                            is TdApi.AuthorizationStateWaitCode -> "code"
                            is TdApi.AuthorizationStateWaitPassword -> "password"
                            is TdApi.AuthorizationStateReady -> {
                                scope.launch {
                                    try { client.send(TdApi.LoadChats(TdApi.ChatListMain(), 100)) }
                                    catch (e: Exception) { _error.value = e.message ?: "Не удалось загрузить чаты" }
                                }
                                "ready"
                            }
                            is TdApi.AuthorizationStateLoggingOut -> "Выход из аккаунта…"
                            is TdApi.AuthorizationStateClosing -> "Закрытие…"
                            is TdApi.AuthorizationStateClosed -> "closed"
                            else -> "Подключаем Telegram…"
                        }
                    }
                    is TdApi.UpdateNewChat -> {
                        chatCache[update.chat.id] = update.chat
                        publishChats()
                    }
                    is TdApi.UpdateChatTitle -> {
                        chatCache[update.chatId]?.title = update.title
                        publishChats()
                    }
                    is TdApi.UpdateChatLastMessage -> {
                        chatCache[update.chatId]?.apply {
                            lastMessage = update.lastMessage
                            positions = update.positions
                        }
                        publishChats()
                    }
                    is TdApi.UpdateNewMessage -> {
                        val msg = update.message
                        chatCache[msg.chatId]?.lastMessage = msg
                        val list = messageCache.getOrPut(msg.chatId) { mutableListOf() }
                        if (list.none { it.id == msg.id }) list.add(toMessage(msg))
                        if (_selectedChat.value?.id == msg.chatId) _messages.value = list.sortedBy { it.id }
                        publishChats()
                    }
                }
            }
        }
    }


    private fun toMessage(message: TdApi.Message): TelegramMessageItem {
        val text = when (val content = message.content) {
            is TdApi.MessageText -> content.text.text
            is TdApi.MessagePhoto -> "📷 Фото"
            is TdApi.MessageVideo -> "🎥 Видео"
            is TdApi.MessageDocument -> "📎 Файл"
            is TdApi.MessageVoiceNote -> "🎤 Голосовое сообщение"
            is TdApi.MessageAudio -> "🎵 Аудио"
            is TdApi.MessageSticker -> "Стикер"
            else -> "Сообщение"
        }
        return TelegramMessageItem(
            message.id, text, message.isOutgoing,
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.date * 1000L))
        )
    }

    fun openChat(chat: TelegramChatItem) {
        _selectedChat.value = chat
        _messages.value = messageCache[chat.id]?.sortedBy { it.id } ?: emptyList()
        scope.launch {
            try {
                val result: TdApi.Messages = client.send(TdApi.GetChatHistory(chat.id, 0L, 0, 50, false))
                val loaded = result.messages.map { toMessage(it) }.toMutableList()
                messageCache[chat.id] = loaded
                _messages.value = loaded.sortedBy { it.id }
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить переписку"
            }
        }
    }

    fun closeChat() {
        _selectedChat.value = null
        _messages.value = emptyList()
        _error.value = ""
    }

    fun sendText(chatId: Long, text: String) {
        scope.launch {
            try {
                val content = TdApi.InputMessageText(
                    TdApi.FormattedText(text.trim(), emptyArray()),
                    null,
                    true
                )
                client.send(TdApi.SendMessage(chatId, null, null, null, content))
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось отправить сообщение"
            }
        }
    }

    private fun publishChats() {
        _chats.value = chatCache.values
            .sortedByDescending { chat -> chat.positions.maxOfOrNull { it.order } ?: 0L }
            .map { chat ->
                val message = chat.lastMessage?.let { messageText(it.content) }.orEmpty()
                val time = chat.lastMessage?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.date * 1000L))
                }.orEmpty()
                TelegramChatItem(chat.id, chat.title, message, time, chat.unreadCount)
            }
    }

    private fun messageText(content: TdApi.MessageContent): String = when (content) {
        is TdApi.MessageText -> content.text.text
        is TdApi.MessagePhoto -> "📷 Фото"
        is TdApi.MessageVideo -> "🎥 Видео"
        is TdApi.MessageDocument -> "📎 Файл"
        is TdApi.MessageVoiceNote -> "🎤 Голосовое сообщение"
        is TdApi.MessageAudio -> "🎵 Аудио"
        is TdApi.MessageSticker -> "Стикер"
        else -> "Сообщение"
    }

    fun sendPhone(phone: String) {
        scope.launch {
            try { _error.value = ""; client.send(TdApi.SetAuthenticationPhoneNumber(phone.trim(), null)) }
            catch (e: Exception) { _error.value = e.message ?: "Не удалось отправить номер" }
        }
    }

    fun sendCode(code: String) {
        scope.launch {
            try { _error.value = ""; client.send(TdApi.CheckAuthenticationCode(code.trim())) }
            catch (e: Exception) { _error.value = e.message ?: "Неверный код" }
        }
    }

    fun sendPassword(password: String) {
        scope.launch {
            try { _error.value = ""; client.send(TdApi.CheckAuthenticationPassword(password)) }
            catch (e: Exception) { _error.value = e.message ?: "Неверный пароль" }
        }
    }

    fun close() = client.close()
}
