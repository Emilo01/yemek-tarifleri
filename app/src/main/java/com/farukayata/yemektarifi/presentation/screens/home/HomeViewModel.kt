package com.farukayata.yemektarifi.presentation.screens.home


import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64 // Burayı android.util.Base64 ile değiştirdim
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farukayata.yemektarifi.BuildConfig
import com.farukayata.yemektarifi.data.remote.VisionApiService
import com.farukayata.yemektarifi.data.remote.model.VisionRequest
import com.farukayata.yemektarifi.data.remote.model.VisionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visionApiService: VisionApiService
//VisionApiService'ı constructor parametresi olarak aldık
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _selectedImageBase64 = MutableStateFlow("")
    val selectedImageBase64: StateFlow<String> = _selectedImageBase64

    private val _detectedLabels = MutableStateFlow<List<VisionResponse.LabelAnnotation>>(emptyList())
    val detectedLabels: StateFlow<List<VisionResponse.LabelAnnotation>> = _detectedLabels

    private val _localizedObjects = MutableStateFlow<List<VisionRequest.LocalizedObjectAnnotation>>(emptyList())
    val localizedObjects: StateFlow<List<VisionRequest.LocalizedObjectAnnotation>> = _localizedObjects


    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }


    fun convertImageToBase64Compressed_2_1(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // 1. Sıkıştır
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream) // %90 kalite çok iyi
                val byteArray = outputStream.toByteArray()

                // 2. Gerçek JPEG byte dizisini Base64'e çevir
                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                // 3. Saf Base64'ü ata
                _selectedImageBase64.value = base64String

                Log.d("VisionRequestCheck", "Yeni Base64 uzunluğu: ${base64String.length}")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VisionRequestCheck", "Hata oluştu: ${e.localizedMessage}")
            }
        }
    }



    fun detectLabels() {
        viewModelScope.launch {
            try {
                val base64Image = _selectedImageBase64.value
                if (base64Image.isNotEmpty()) {
                    val request = VisionRequest(
                        requests = listOf(
                            VisionRequest.Request(
                                image = VisionRequest.Image(content = base64Image),
                                features = listOf(
                                    VisionRequest.Feature()
                                ),
                                imageContext = VisionRequest.ImageContext(
                                    languageHints = listOf("en")
                                )
                            )
                        )
                    )
                    //data image mi değil mi prefixs
                    Log.d("VisionRequestCheck", "Gönderilecek Base64 ilk 100 karakter: ${base64Image.take(100)}")

                    Log.d("VisionRequestCheck", "Base64 uzunluğu: ${base64Image.length}")
                    Log.d("VisionRequestCheck", "Request içeriği: $request")
                    val response = visionApiService.annotateImage(
                        apiKey = BuildConfig.VISION_API_KEY,
                        request = request
                    )
                    _detectedLabels.value = response.responses.firstOrNull()?.labelAnnotations ?: emptyList()

                    // Başarılıysa loga yazdıralım
                    response.responses.firstOrNull()?.labelAnnotations?.forEach { label ->
                        Log.d("VisionAPI", "Label: ${label.description} - Score: ${(label.score * 100).toInt()}%")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VisionAPI", "Hata oluştu: ${e.message}")
            }
        }
    }

    fun detectObjects() {
        viewModelScope.launch {
            try {
                val base64Image = _selectedImageBase64.value
                if (base64Image.isNotEmpty()) {
                    val request = VisionRequest(
                        requests = listOf(
                            VisionRequest.Request(
                                image = VisionRequest.Image(content = base64Image),
                                features = listOf(
                                    VisionRequest.Feature(
                                        type = "OBJECT_LOCALIZATION",
                                        maxResults = 20
                                    )
                                )
                            )
                        )
                    )

                    Log.d("VisionRequestCheck", "Gönderilecek Base64 ilk 100 karakter: ${base64Image.take(100)}")
                    Log.d("VisionRequestCheck", "Base64 uzunluğu: ${base64Image.length}")
                    Log.d("VisionRequestCheck", "Request içeriği: $request")

                    val response = visionApiService.annotateImage(
                        apiKey = BuildConfig.VISION_API_KEY,
                        request = request
                    )

                    //val objects = response.responses.firstOrNull()?.localizedObjects ?: emptyList()
                    val objects = response.responses.firstOrNull()?.localizedObjectAnnotations ?: emptyList()

                    _localizedObjects.value = objects

                    objects.forEach {
                        Log.d("VisionAPI-Object", "Object: ${it.name} - Score: ${(it.score * 100).toInt()}%")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VisionAPI", "Hata oluştu: ${e.message}")
            }
        }
    }

}

//Uri'den bitmap alıyor → JPEG'e sıkıştırıyor → byteArray'e çeviriyor → Base64 encode ediyor

//vision api için bu kısımı update ettik;
/*
fun convertImageToBase64(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()

            val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
            _selectedImageBase64.value = base64String
        }
    }
 */

//features şöyle de olabilir ;
/*
features = listOf(
                                VisionRequest.Feature(
                                    type = "LABEL_DETECTION",
                                    maxResults = 10
                                )
                            )
 */


/*
    fun convertImageToBase64(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // %80 kalite sıkıştırıyoruz
            val byteArray = outputStream.toByteArray()

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            _selectedImageBase64.value = base64String
        }
    }

    fun convertImageToBase64_2(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Görseli yeniden boyutlandır: örneğin genişlik 600px yapalım
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                600, // hedef genişlik
                (bitmap.height * (600.0f / bitmap.width)).toInt(), // oranı koru
                true
            )

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            // %60 kaliteyle sıkıştırıyoruz

            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            _selectedImageBase64.value = base64String

            Log.d("VisionRequestCheck", "Yeni Base64 uzunluğu: ${base64String.length}")
        }
    }

    fun convertImageToBase64Compressed(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val byteArray = outputStream.toByteArray()

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Base64 başına data:image/jpeg;base64, ekliyoruz!
            val fullBase64 = "data:image/jpeg;base64,$base64String"

            _selectedImageBase64.value = fullBase64

            Log.d("VisionRequestCheck", "Yeni Base64 uzunluğu: ${fullBase64.length}")
        }
    }

    fun convertImageToBase64Compressed_2(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream) // %60 kalite
            val byteArray = outputStream.toByteArray()

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            _selectedImageBase64.value = base64String // SADECE Base64, başlık ekleme yok

            Log.d("VisionRequestCheck", "Yeni Base64 uzunluğu: ${base64String.length}")
        }
    }

 */