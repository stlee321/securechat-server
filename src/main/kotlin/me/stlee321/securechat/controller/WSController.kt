package me.stlee321.securechat.controller

import io.awspring.cloud.dynamodb.DynamoDbTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.stlee321.securechat.chat.ChatItem
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller

@Controller
class WSController(val dynamoDbTemplate: DynamoDbTemplate) {
    @MessageMapping("/{chatRoomId}")
    fun handleChat(@Payload chatItem: ChatItem): String {
        return runBlocking {
            launch(Dispatchers.IO) {
                dynamoDbTemplate.save(chatItem)
            }
            chatItem.getContent()
        }
    }
}