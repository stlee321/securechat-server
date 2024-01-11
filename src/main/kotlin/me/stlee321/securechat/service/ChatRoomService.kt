package me.stlee321.securechat.service

import io.awspring.cloud.dynamodb.DynamoDbTemplate
import me.stlee321.securechat.chat.ChatItem
import me.stlee321.securechat.chatroom.ChatRoom
import me.stlee321.securechat.chatroom.ChatRoomStorage
import me.stlee321.securechat.controller.req.CreateChatRoomRequest
import me.stlee321.securechat.controller.res.ChatItemResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch

@Service
class ChatRoomService(
    val chatRoomStorage: ChatRoomStorage,
    val dynamoDbTemplate: DynamoDbTemplate,
    val dynamoDbClient: DynamoDbEnhancedClient,
    val chatItemTableSchema: StaticTableSchema<ChatItem>,
    @Value("\${secure-chat.dynamodb.table-name}") val tableName: String
) {
    fun getChatInfo(chatRoomId: String): ChatRoom? {
        return chatRoomStorage.getChatRoom(chatRoomId)
    }

    fun createChatRoom(chatRoomRequest: CreateChatRoomRequest): ChatRoom? {
        val durationMillis = chatRoomRequest.duration * 60 * 60 * 1000L
        val expiration = System.currentTimeMillis() + durationMillis
        return chatRoomStorage.createNewChatRoom(chatRoomRequest.verification, expiration)
    }

    fun getChatItems(chatRoomId: String, timestamp: Long, size: Long): List<ChatItemResponse> {
        val result = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
            .queryConditional(
                QueryConditional
                .sortLessThanOrEqualTo(
                    Key.builder()
                    .partitionValue("chat-room#$chatRoomId")
                    .sortValue(timestamp).build()))
            .scanIndexForward(false)
            .build(), ChatItem::class.java)
        return result.items().stream().limit(size).map {
            ChatItemResponse(it.getContent(), it.getTimestamp())
        }.toList().reversed()
    }
    suspend fun deleteExpiredChatRoomItems(chatRoomId: String) {
        val results = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
            .queryConditional(
                QueryConditional
                    .sortGreaterThan(Key.builder()
                        .partitionValue("chat-room#$chatRoomId")
                        .sortValue(0)
                        .build())
            ) .build(), ChatItem::class.java)
        val batchRequestBuilder = BatchWriteItemEnhancedRequest.builder()
        val chatItemMappedTableResource =
            dynamoDbClient.table(tableName, chatItemTableSchema)
        val deleteBatchBuilder = WriteBatch.builder(ChatItem::class.java)
            .mappedTableResource(chatItemMappedTableResource)
        results.items().forEach {item ->
            deleteBatchBuilder
                .addDeleteItem { builder ->
                    builder.key(Key.builder()
                        .partitionValue(item.getChatRoomId())
                        .sortValue(item.getTimestamp()).build())
                }
        }
        batchRequestBuilder.writeBatches(deleteBatchBuilder.build())
        dynamoDbClient.batchWriteItem(batchRequestBuilder.build())
    }
}