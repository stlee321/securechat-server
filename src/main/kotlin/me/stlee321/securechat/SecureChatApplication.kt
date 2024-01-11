package me.stlee321.securechat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SecureChatApplication

fun main(args: Array<String>) {
	runApplication<SecureChatApplication>(*args)
}
