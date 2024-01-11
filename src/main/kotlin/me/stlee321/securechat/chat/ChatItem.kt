package me.stlee321.securechat.chat

import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
class ChatItem : Comparable<ChatItem> {
    private var chatRoomId: String = ""
    private var timestamp: Long = 0
    private var content: String = ""
    @DynamoDbPartitionKey
    @DynamoDbAttribute("chatId")
    fun getChatRoomId(): String {
        return "chat-room#$chatRoomId"
    }
    fun setChatRoomId(chatRoomId: String) {
        this.chatRoomId = chatRoomId
    }
    @DynamoDbSortKey
    @DynamoDbAttribute("timestamp")
    fun getTimestamp(): Long {
        return timestamp
    }
    fun setTimestamp(timestamp: Long) {
        this.timestamp = timestamp
    }
    fun getContent(): String {
        return this.content
    }
    fun setContent(content: String) {
        this.content = content
    }

    override fun compareTo(other: ChatItem): Int {
        return timestamp.compareTo(other.getTimestamp())
    }

    override fun toString(): String {
        return "ChatItem($chatRoomId, $timestamp)"
    }

    fun getKey(): Key {
        return Key.builder()
            .partitionValue(getChatRoomId())
            .sortValue(getTimestamp()).build()
    }
}