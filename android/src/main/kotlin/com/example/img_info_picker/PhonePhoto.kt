package com.tahamalas.custom_image_picker

class PhonePhoto(private val id: String, private val photoUri: String, private val albumName: String, private val videoTumbUrl :String = "", private val fileType: String= "", private val lastModifiedDate: String= "", private val createdDate: String= "") {
    fun toJson() : String{
        return "{" +
                "\"Id\": \"$id\", " +
                "\"AlbumName\": \"$albumName\", " +
                "\"PhotoUri\": \"$photoUri\"," +
                "\"LastModifiedDate\": \"$lastModifiedDate\"," +
                "\"CreatedDate\": \"$createdDate\"," +
                "\"FileType\": \"$fileType\"," +
                "\"VideoTumblr\": \"$videoTumbUrl\"" +
        "}"
    }
}