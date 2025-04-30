package com.farukayata.yemektarifi.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

//storrege yüklenen görseli kaydetmede kullanıyorduk fakat şu anlık base 64 e geçtik gerek kalmadı
@Singleton
class StorageRepository @Inject constructor(){
    private val storageRef = FirebaseStorage.getInstance().reference

    suspend fun uploadImageAndGetUrl(uri: Uri): String {
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }
}
