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

class TelegramSession(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val client: TdClient

    private val _authState = MutableStateFlow("Подключаем Telegram…")
    val authState: StateFlow<String> = _authState.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

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
                if (update is TdApi.UpdateAuthorizationState) {
                    _authState.value = when (update.authorizationState) {
                        is TdApi.AuthorizationStateWaitPhoneNumber -> "phone"
                        is TdApi.AuthorizationStateWaitCode -> "code"
                        is TdApi.AuthorizationStateWaitPassword -> "password"
                        is TdApi.AuthorizationStateReady -> "ready"
                        is TdApi.AuthorizationStateLoggingOut -> "Выход из аккаунта…"
                        is TdApi.AuthorizationStateClosing -> "Закрытие…"
                        is TdApi.AuthorizationStateClosed -> "closed"
                        else -> "Подключаем Telegram…"
                    }
                }
            }
        }
    }

    fun sendPhone(phone: String) {
        scope.launch {
            try {
                _error.value = ""
                client.send(TdApi.SetAuthenticationPhoneNumber(phone.trim(), null))
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось отправить номер"
            }
        }
    }

    fun sendCode(code: String) {
        scope.launch {
            try {
                _error.value = ""
                client.send(TdApi.CheckAuthenticationCode(code.trim()))
            } catch (e: Exception) {
                _error.value = e.message ?: "Неверный код"
            }
        }
    }

    fun sendPassword(password: String) {
        scope.launch {
            try {
                _error.value = ""
                client.send(TdApi.CheckAuthenticationPassword(password))
            } catch (e: Exception) {
                _error.value = e.message ?: "Неверный пароль"
            }
        }
    }

    fun close() = client.close()
}
