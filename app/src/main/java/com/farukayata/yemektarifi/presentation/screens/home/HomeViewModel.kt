package com.farukayata.yemektarifi.presentation.screens.home


import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farukayata.yemektarifi.BuildConfig
import com.farukayata.yemektarifi.data.remote.OpenAiService
import com.farukayata.yemektarifi.data.remote.StorageRepository
import com.farukayata.yemektarifi.data.remote.UserRepository
import com.farukayata.yemektarifi.data.remote.VisionApiService
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.farukayata.yemektarifi.data.remote.model.VisionRequest
import com.farukayata.yemektarifi.data.remote.model.VisionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visionApiService: VisionApiService,
    //VisionApiServiceı constructor parametresi olarak aldık
    private val storageRepository: StorageRepository,
    private val openAiService: OpenAiService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _selectedImageBase64 = MutableStateFlow("")
    val selectedImageBase64: StateFlow<String> = _selectedImageBase64

    private val _detectedLabels = MutableStateFlow<List<VisionResponse.LabelAnnotation>>(emptyList())
    val detectedLabels: StateFlow<List<VisionResponse.LabelAnnotation>> = _detectedLabels

    private val _localizedObjects = MutableStateFlow<List<VisionRequest.LocalizedObjectAnnotation>>(emptyList())
    val localizedObjects: StateFlow<List<VisionRequest.LocalizedObjectAnnotation>> = _localizedObjects

    private val _openAiItems = MutableStateFlow<List<String>>(emptyList())
    val openAiItems: StateFlow<List<String>> = _openAiItems

    private val _categorizedItems = MutableStateFlow<List<CategorizedItem>>(emptyList())
    val categorizedItems: StateFlow<List<CategorizedItem>> = _categorizedItems

    //boş response;
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userEditedItems = MutableStateFlow<List<CategorizedItem>>(emptyList())
    val userEditedItems: StateFlow<List<CategorizedItem>> = _userEditedItems

    private val _navigateToResult = MutableStateFlow(false)
    val navigateToResult: StateFlow<Boolean> = _navigateToResult

    private val _isResultLoading = MutableStateFlow(false)
    val isResultLoading: StateFlow<Boolean> = _isResultLoading

    private val _freeTextInputs = MutableStateFlow<List<String>>(emptyList())
    val freeTextInputs: StateFlow<List<String>> = _freeTextInputs

    private val _selectedMealType = MutableStateFlow("")
    val selectedMealType: StateFlow<String> = _selectedMealType

    private val _recipes = MutableStateFlow<List<RecipeItem>>(emptyList())
    val recipes: StateFlow<List<RecipeItem>> = _recipes

    // Cache için yeni state
    private var cachedRecipes: List<RecipeItem>? = null

    private val _popularRecipes = MutableStateFlow<List<RecipeItem>>(emptyList())
    val popularRecipes: StateFlow<List<RecipeItem>> = _popularRecipes

    fun setSelectedMealType(type: String) {
        _selectedMealType.value = type
    }

    fun setRecipes(newRecipes: List<RecipeItem>) {
        Log.d("RecipeFlow", "HomeViewModel'a tarif geldi: ${newRecipes.map { it.name }}")
        _recipes.value = newRecipes
        // Yeni tarifleri cache'e kaydet
        cachedRecipes = newRecipes
    }

    // Cacheden tarifleri yüklicez
    fun loadCachedRecipes() {
        cachedRecipes?.let {
            _recipes.value = it
        }
    }
    fun clearCache() {
        cachedRecipes = null
    }

    fun setFreeTextInputs(inputs: List<String>) {
        _freeTextInputs.value = inputs
    }

    fun setMealType(mealType: String) {
        _selectedMealType.value = mealType
    }




    fun startReAnalyze() {
        val currentItems = _userEditedItems.value
        val inputs = _freeTextInputs.value

        if (currentItems.isEmpty() && inputs.isEmpty()) {
            _categorizedItems.value = emptyList()
            _isResultLoading.value = false
            _navigateToResult.value = false
            return
        }

        reAnalyzeWithFreeTextList(currentItems, inputs)
    }



    fun triggerResultNavigation() {
        _navigateToResult.value = true

        if (_userEditedItems.value.isEmpty() && _freeTextInputs.value.isEmpty()) {
            _categorizedItems.value = emptyList()
        }
    }

    fun resetResultNavigation() { //detailscreenle ekledik
        _navigateToResult.value = false

        //Ana sayfaya dönünce kullanıcı onayladığı liste görüncek
        _categorizedItems.value = _userEditedItems.value
    }

    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }


    fun convertImageToBase64Compressed_2_1(uri: Uri?, contentResolver: ContentResolver) {
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                //Sıkıştır
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 65, outputStream)
                val byteArray = outputStream.toByteArray()

                //Gerçek JPEG byte dizisini Base64e çevirdik
                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                //Saf Base64ü atadık
                _selectedImageBase64.value = base64String

                Log.d("VisionRequestCheck", "Yeni Base64 uzunluğu: ${base64String.length}")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VisionRequestCheck", "Hata oluştu: ${e.localizedMessage}")
            }
        }
    }



    fun analyzeWithOpenAi() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            //Ağ ve Base64 işlemleri ana thread yerine IO threadde çalışır
            //val imageUrl = _uploadedImageUrl.value ?: return@launch - storrage image kaydetmeyi saldık
            val base64 = _selectedImageBase64.value


            //seçilenn görsel boşsa
            if (base64.isBlank()) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Lütfen önce bir görsel yükleyin."
                }
                _isLoading.value = false
                return@launch
            }

            val promptText = """
            Aşağıdaki görselde yemek yapımında kullanılabilecek gıda ürünleri olabilir. Görseli analiz et ve sadece yenilebilir, yemek yapımında kullanılan ürünleri aşağıdaki formatta listele:

            🍅 Domates - Sebzeler  
            🧀 Peynir - Yumurta ve Süt Ürünleri  
            🐟 Somon - Balık ve Deniz Ürünleri  
            🍎 Elma - Meyveler  

            Lütfen her satıra bir ürün gelecek şekilde, yanına uygun bir emoji ve aşağıda belirtilen kategorilerden birini ekleyerek **Türkçe** yaz:

            Et ve Et Ürünleri  
            Balık ve Deniz Ürünleri  
            Yumurta ve Süt Ürünleri  
            Tahıllar ve Unlu Mamuller  
            Baklagiller  
            Sebzeler  
            Meyveler  
            Baharatlar ve Tat Vericiler  
            Yağlar ve Sıvılar  
            Konserve ve Hazır Gıdalar  
            Tatlı Malzemeleri ve Kuruyemişler  

            **Yalnızca bu kategori adlarını kullan.** Marka isimlerini veya tekrarlayan benzer ürünleri listeleme. Liste tekrarsız olsun.
        """.trimIndent()

            val json = """
            {
              "model": "gpt-4o",
              "temperature": 0.2,
               "top_p": 1,
              "messages": [
                {
                  "role": "user",
                  "content": [
                    {
                      "type": "text",
                      "text": "${promptText.replace("\"", "\\\"").replace("\n", "\\n")}"
                    },
                    {
                      "type": "image_url",
                      "image_url": {
                        "url": "data:image/jpeg;base64,$base64"
                      }
                    }
                  ]
                }
              ],
              "max_tokens": 500
            }
        """.trimIndent()

            val requestBody = json.toRequestBody("application/json".toMediaType())

            try {
                val response = openAiService.getImageAnalysis(requestBody)
                val result = response.choices.firstOrNull()?.message?.content
                Log.d("OpenAIResult", result ?: "Null")
                val cleanedItems = result
                    ?.lines()
                    ?.mapNotNull { line ->
                        val parts = line.split(" - ")
                        if (parts.size == 2) {
                            val emojiAndName = parts[0].trim()
                            val category = parts[1].trim()

                            val emoji = emojiAndName.takeWhile { !it.isLetterOrDigit() }.trim()
                            val name = emojiAndName.dropWhile { !it.isLetterOrDigit() }.trim()

                            CategorizedItem(emoji, name, category)
                        } else null
                    } ?: emptyList()

                if (cleanedItems.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _userMessage.value = "Görselde analiz edilebilecek ürün bulunamadı. Lütfen daha net bir fotoğraf yükleyin."
                    }
                }

                //_categorizedItems.value = cleanedItems
                //artık ui güncellemesini main thread de yapıyoruz şu eklenti ile ;Dispatchers.io
                withContext(Dispatchers.Main) {
                    _categorizedItems.value = cleanedItems
                    //_navigateToResult.value = true //edititembottomsheetten burraya çektik çünkü state etmeden çektiğimiz için liste gücel gitmiyordu ilk de
                }

            } catch (e: Exception) {
                Log.e("OpenAI", "Hata: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }

    }

    fun setUserEditedItems(items: List<CategorizedItem>) {
        _userEditedItems.value = items
    }




    //Listeye kullanıcıdan gelen eksik formatlı ürünler varsa bunları temizleyerek analiz ettik

    fun reAnalyzeWithFreeTextList(items: List<CategorizedItem>, userInputs: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _isResultLoading.value = true

            // Kullanıcının onayladığı mevcut ürünleri formatla
            val formattedItems = items.joinToString("\n") { "${it.emoji} ${it.name} - ${it.category}" }

            // Serbest yazılmış ürünleri açık biçimde listele (her biri başında "-" olacak şekilde)
            val userFreeInputsFormatted = userInputs
                .filter { it.isNotBlank() }
                .joinToString("\n") { "${it.trim()}" }

            val fullPrompt = """
            Aşağıda yemek yapımında kullanılabilecek bazı ürünler verilmiştir. 
            Bunları analiz et ve sadece yenilebilir, yemek yapımında kullanılan ürünleri aşağıdaki gibi formatla:

            Örnek:
            🥩 Et - Et ve Et Ürünleri
            🐟 Palamut - Balık ve Deniz Ürünleri
            🥚 Yumurta - Yumurta ve Süt Ürünleri

            Format: [emoji] [ürün adı] - [kategori adı]

            Aşağıdaki sistem tarafından algılanan ürünleri analiz et:
            $formattedItems

            Kullanıcının sonradan manuel olarak eklediği ürünler:
            $userFreeInputsFormatted

            Lütfen aşağıdaki kategori adlarından birini kullan:
            Et ve Et Ürünleri  
            Balık ve Deniz Ürünleri  
            Yumurta ve Süt Ürünleri  
            Tahıllar ve Unlu Mamuller  
            Baklagiller  
            Sebzeler  
            Meyveler  
            Baharatlar ve Tat Vericiler  
            Yağlar ve Sıvılar  
            Konserve ve Hazır Gıdalar  
            Tatlı Malzemeleri ve Kuruyemişler

            🔴 Yalnızca bu kategori adlarını kullan.  
            🔴 Her ürün için mutlaka **emoji, ürün adı ve kategori** içeren tek satırlık çıktı ver.  
            🔴 Açıklayıcı cümle, açıklama veya başlık ekleme.  
            🔴 Sadece ürün listesi ver.
        """.trimIndent()

            // Escape karakterleri
            val safePrompt = fullPrompt.replace("\"", "\\\"").replace("\n", "\\n")

            val json = """
            {
              "model": "gpt-4o",
              "temperature": 0.2,
              "top_p": 1,
              "messages": [
                {
                  "role": "user",
                  "content": [
                    {
                      "type": "text",
                      "text": "$safePrompt"
                    }
                  ]
                }
              ],
              "max_tokens": 500
            }
        """.trimIndent()

            val requestBody = json.toRequestBody("application/json".toMediaType())

            try {
                val response = openAiService.getImageAnalysis(requestBody)
                val result = response.choices.firstOrNull()?.message?.content
                Log.d("OpenAIResult", result ?: "Null")

                val cleanedItems = result?.lines()?.mapNotNull { line ->
                    val parts = line.split(" - ")
                    if (parts.size == 2) {
                        val emojiAndName = parts[0].trim().removePrefix("-").trim()
                        val category = parts[1].trim()
                        val emoji = emojiAndName.takeWhile { !it.isLetterOrDigit() }.trim()
                        val name = emojiAndName.dropWhile { !it.isLetterOrDigit() }.trim()
                        CategorizedItem(emoji, name, category)
                    } else null
                } ?: emptyList()

                withContext(Dispatchers.Main) {
                    _categorizedItems.value = cleanedItems
                    _userEditedItems.value = cleanedItems
                    _isResultLoading.value = false
                    _navigateToResult.value = true
                }

            } catch (e: Exception) {
                Log.e("OpenAI", "Hata: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    _isResultLoading.value = false
                }
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearUserMessage() {
        _userMessage.value = null
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

    fun fetchPopularRecipes() {
        viewModelScope.launch {
            // Firestore'dan admin kullanıcısının favori tariflerini çek
            val adminId = "iiinsswnIob4JP0T8AqptPtBc3F2" // Firestore'daki admin id'si
            val result = userRepository.getFavoriteRecipesFromSubcollection(adminId)
            result.onSuccess { favs ->
                // Rastgele 4 tarif seç
                _popularRecipes.value = favs.shuffled().take(4)
            }
        }
    }
}



//Uri'den bitmap alıyor → JPEG'e sıkıştırıyor → byteArray'e çeviriyor → Base64 encode ediyor