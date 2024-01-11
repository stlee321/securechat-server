package me.stlee321.securechat.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WSConfig(
    @Value("\${secure-chat.rabbitmq.host}") val relayHost: String,
    @Value("\${secure-chat.rabbitmq.port}") val relayPort: Int,
    @Value("\${secure-chat.rabbitmq.client-name}") val clientLogin: String,
    @Value("\${secure-chat.rabbitmq.client-passcode}") val clientPasscode: String
) : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/api/ws")
            .setAllowedOrigins("*")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
        registry.enableStompBrokerRelay("/exchange", "/queue", "/amq/queue", "/topic", "/reply-queue")
            .setRelayHost(relayHost)
            .setRelayPort(relayPort)
            .setClientLogin(clientLogin)
            .setClientPasscode(clientPasscode)
    }
}