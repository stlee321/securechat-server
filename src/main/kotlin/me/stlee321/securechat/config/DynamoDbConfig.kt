package me.stlee321.securechat.config

import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver
import io.awspring.cloud.dynamodb.DynamoDbTableSchemaResolver
import me.stlee321.securechat.chat.ChatItem
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema

@Configuration
class DynamoDbConfig {
    @Value("\${secure-chat.dynamodb.table-name}") lateinit var tableName: String
    @Bean
    fun dynamoDbTableNameResolver(): DynamoDbTableNameResolver {
        return object : DynamoDbTableNameResolver {
            override fun <T : Any?> resolve(clazz: Class<T>): String {
                return tableName
            }
        }
    }
    @Bean
    fun chatItemTableSchema() : StaticTableSchema<ChatItem> {
        return StaticTableSchema.builder(ChatItem::class.java)
            .newItemSupplier(::ChatItem)
            .addAttribute(String::class.java) { a ->
                a.name("chatRoomId")
                    .getter(ChatItem::getChatRoomId)
                    .setter(ChatItem::setChatRoomId)
                    .tags(StaticAttributeTags.primaryPartitionKey())
            }
            .addAttribute(Long::class.java) { a ->
                a.name("timestamp")
                    .getter(ChatItem::getTimestamp)
                    .setter(ChatItem::setTimestamp)
                    .tags(StaticAttributeTags.primarySortKey())
            }
            .addAttribute(String::class.java) { a ->
                a.name("content")
                    .getter(ChatItem::getContent)
                    .setter(ChatItem::setContent)
            }
            .build()
    }
    @Bean
    fun dynamoDbTableSchemaResolver(chatItemTableSchema: StaticTableSchema<ChatItem>) : DynamoDbTableSchemaResolver {
        return object : DynamoDbTableSchemaResolver {
            override fun <T : Any?> resolve(clazz: Class<T>, tableName: String): TableSchema<*> {
                return chatItemTableSchema
            }
        }
    }
}