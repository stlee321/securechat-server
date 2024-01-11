package me.stlee321.securechat.controller

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import me.stlee321.securechat.chatroom.ChatRoom
import me.stlee321.securechat.controller.req.CreateChatRoomRequest
import me.stlee321.securechat.controller.res.ChatItemResponse
import me.stlee321.securechat.service.ChatImageService
import me.stlee321.securechat.service.ChatRoomService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@RestController
@RequestMapping("/api")
class ChatController(
    val chatRoomService: ChatRoomService,
    val chatImageService: ChatImageService,
    @Value("\${secure-chat.domain}") val domain: String
) {
    @GetMapping("/chat/{chatRoomId}")
    fun getChatInfo(@PathVariable chatRoomId: String): ResponseEntity<ChatRoom> {
        val chatRoom: ChatRoom = chatRoomService.getChatInfo(chatRoomId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(chatRoom)
    }
    @PostMapping("/chat")
    fun createChatRoom(@RequestBody chatRoomRequest: CreateChatRoomRequest): ResponseEntity<ChatRoom> {
        if(chatRoomRequest.verification.isBlank())
            return ResponseEntity.badRequest().build()
        if(chatRoomRequest.duration < 1 || chatRoomRequest.duration > 24)
            return ResponseEntity.badRequest().build()
        val newChatRoom = chatRoomService.createChatRoom(chatRoomRequest)
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(newChatRoom)
    }
    @PostMapping("/image/{chatRoomId}")
    fun uploadImage(
        @PathVariable("chatRoomId") chatRoomId: String,
        @RequestParam("image") image: MultipartFile
    ): ResponseEntity<String> {
        val imageId = chatImageService.uploadImage(chatRoomId, image)
            ?: return ResponseEntity.internalServerError().build()
        return ResponseEntity.ok(imageId)
    }
    @GetMapping("/image/{chatRoomId}/{imageId}")
    fun getImageUrl(
        @PathVariable("chatRoomId") chatRoomId: String,
        @PathVariable("imageId") imageId: String
    ): String {
        return chatImageService.getImageUrl(chatRoomId, imageId)
    }
    @GetMapping("/chats/{chatRoomId}/{timestamp}")
    fun getChats(
        @PathVariable("chatRoomId") chatRoomId: String,
        @PathVariable("timestamp") timestamp: Long,
        @RequestParam("size", defaultValue = "10") size: Long
    ): ResponseEntity<List<ChatItemResponse>> {
        val chats = chatRoomService.getChatItems(chatRoomId, timestamp, size)
        return ResponseEntity.ok(chats)
    }
    @GetMapping("/qrcode")
    fun getQrCodeForChatRoom(@RequestParam("id") chatRoomId: String): ResponseEntity<ByteArray> {
        val url = "https://$domain/chat/$chatRoomId"
        val matrix = MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 256, 256)
        try {
            val out = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(matrix, "PNG", out)
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(out.toByteArray())
        }catch(e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }
}