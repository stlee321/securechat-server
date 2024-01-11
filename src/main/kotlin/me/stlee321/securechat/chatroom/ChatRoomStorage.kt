package me.stlee321.securechat.chatroom

import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatRoomStorage {
    private val chatRooms = ConcurrentHashMap<String, ChatRoom>()
    fun createNewChatRoom(verification: String, expiration: Long): ChatRoom {
        val newId = UUID.randomUUID().toString()
        val newChatRoom = ChatRoom(newId, verification, expiration)
        chatRooms[newId] = newChatRoom
        return newChatRoom
    }
    fun getChatRoom(id: String): ChatRoom? {
        try{
            UUID.fromString(id)
        }catch(e: Exception) {
            return null
        }
        return chatRooms[id]
    }
    fun removeChatRoom(id: String): Boolean {
        try{
            UUID.fromString(id)
        }catch(e: Exception) {
            return false
        }
        return chatRooms.remove(id) != null
    }
    fun expireChatRooms(): List<String> {
        val now = Date().time
        val expiredChatRoomIds = chatRooms.filter {
            it.value.expiration < now
        }.map { it.key }
        expiredChatRoomIds.forEach {
                removeChatRoom(it)
        }
        return expiredChatRoomIds
    }
}