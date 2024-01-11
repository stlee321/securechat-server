package me.stlee321.securechat.service

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.util.*

@Service
class ChatImageService(
    val s3Template: S3Template,
    @Value("\${secure-chat.s3.bucket-name}") val bucketName: String
) {
    fun getImageUrl(chatRoomId: String, imageId: String): String {
        val url = s3Template.createSignedGetURL(bucketName, "$chatRoomId/$imageId", Duration.ofHours(24L))
        return url.toString()
    }
    fun uploadImage(chatRoomId: String, encryptedImage: MultipartFile): String? {
        val imageId = UUID.randomUUID().toString()
        val objectKey = "$chatRoomId/$imageId"
        val objectMetadata = ObjectMetadata.builder()
            .contentType(encryptedImage.contentType).build()
        try {
            s3Template.upload(bucketName, objectKey, encryptedImage.inputStream, objectMetadata)
            return imageId
        }catch(e: Exception) {
            return null
        }
    }
    suspend fun deleteExpiredChatRoomImages(chatRoomId: String) {
        val expiredImageIds = s3Template.listObjects(bucketName, chatRoomId)
        expiredImageIds.forEach {
            s3Template.deleteObject("s3://$bucketName/${it.filename}")
        }
    }
}