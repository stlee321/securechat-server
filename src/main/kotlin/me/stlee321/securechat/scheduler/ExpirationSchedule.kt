package me.stlee321.securechat.scheduler

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.stlee321.securechat.chatroom.ChatRoomStorage
import me.stlee321.securechat.service.ChatImageService
import me.stlee321.securechat.service.ChatRoomService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ExpirationSchedule(
    val chatRoomStorage: ChatRoomStorage,
    val chatRoomService: ChatRoomService,
    val chatImageService: ChatImageService
) {
    @Scheduled(initialDelay = 5, fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun expireChatRoom() {
        val expiredChatIds = chatRoomStorage.expireChatRooms()
        runBlocking {
            expiredChatIds.forEach {
                launch {
                    chatRoomService.deleteExpiredChatRoomItems(it)
                }
                launch {
                    chatImageService.deleteExpiredChatRoomImages(it)
                }
            }
        }
    }
}