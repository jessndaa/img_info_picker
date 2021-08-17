package com.tahamalas.custom_image_picker

class PhoneAlbum(private val id: String, private val name: String, private val coverUri: String, private val photosCount: Int) {

    fun toJson(): String {
        return "{\"id\": \"$id\", \"name\": \"$name\", \"coverUri\": \"$coverUri\", \"photosCount\": $photosCount}"
    }
}