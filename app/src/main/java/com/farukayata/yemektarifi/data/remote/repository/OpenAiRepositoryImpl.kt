package com.farukayata.yemektarifi.data.remote.repository

import android.util.Log
import com.farukayata.yemektarifi.data.remote.OpenAiService
import com.farukayata.yemektarifi.data.remote.model.OpenAiResponse
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.farukayata.yemektarifi.domain.OpenAiRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class OpenAiRepositoryImpl @Inject constructor(
    private val openAiService: OpenAiService,
    private val gson: Gson
) : OpenAiRepository {

    override suspend fun getRecipes(mealType: String, items: List<String>): List<RecipeItem> = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(mealType, items)

        val requestJson = mapOf(
            "model" to "gpt-4o",
            "messages" to listOf(
                mapOf("role" to "system", "content" to "Sen yaratıcı ama disiplinli bir aşçı asistanısın. Verilen malzemelerle Kullanıcıdan gelen yemek türüne sıkı şekilde uyarak, sadece o türe uygun 3 tarif tarif sun."),
                mapOf("role" to "user", "content" to prompt)
            ),
            "temperature" to 0.7,
            "max_tokens" to 1500,
            "stop" to listOf("Tarif 4:")
        )

        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            gson.toJson(requestJson)
        )

        val response = openAiService.getImageAnalysis(requestBody)
        val rawText = response.choices.firstOrNull()?.message?.content ?: return@withContext emptyList()
        val parsedRecipes = parseRecipesFromText(rawText)

        // DALL·E görsellerini paralel olarak al
        parsedRecipes.map { recipe ->
            async {
                val imageUrl = generateImageFromDescription(recipe.imageUrl)
                recipe.copy(imageUrl = imageUrl)
            }
        }.awaitAll()
    }

    /*
    Elimde şu malzemeler var: ${items.joinToString(", ")}.
        Bu malzemelerle "$mealType" kategorisine uygun 3 yaratıcı yemek tarifi öner.
     */
    //Yemeklerin tarifleri özgün ve gerçek dünya kültürlerinden esinlenmiş olsun.
    // şunu ekledim ama siline de bilir gibi \"$mealType\" türüne ait olmayan tarifler **önerme**. sonnuçları baya azalta bilir
    private fun buildPrompt(mealType: String, items: List<String>): String {

        val extraHints1 = when (mealType) {
            "Sulu Yemekler" -> "Örnek sulu yemekler: Kuru Fasulye, Taze Fasulye, Tas Kebabı, Türlü, Etli Patates"
            "Et Yemekleri" -> "Örnek et yemekleri: Et Sote, Tas Kebabı, Biftek"
            "Sebze Yemekleri" -> "Örnek sebze yemekleri: Karnıyarık, Taze Fasulye, Ispanak"
            "Köfte ve Kıyma Yemekleri" -> "Örnek: Izgara Köfte, Kadınbudu, Dalyan Köfte"
            "Tavuk Yemekleri" -> "Örnek: Tavuk Sote, Fırın Tavuk, Tavuk Güveç"
            "Balık ve Deniz Ürünleri" -> "Örnek: Somon, Hamsi Tava, Karides Güveç"
            "Baklagil Yemekleri" -> "Örnek: Nohut Yemeği, Barbunya, Mercimek"
            "Pilavlar" -> "Örnek: Bulgur Pilavı, İç Pilav, Şehriyeli Pirinç Pilavı"
            "Makarna ve Erişte Çeşitleri" -> "Örnek: Spagetti, Fettuccine, Erişte"
            "Hamur İşleri" -> "Örnek: Poğaça, Açma, Gözleme"
            "Çorbalar" -> "Örnek: Mercimek, Tarhana, Ezogelin"
            "Kahvaltılıklar" -> "Örnek: Menemen, Omlet, Sucuklu Yumurta"
            "Zeytinyağlı Yemekler" -> "Örnek: Zeytinyağlı Yaprak Sarma, Enginar, Barbunya"
            "Yumurtalı Yemekler" -> "Örnek: Çılbır, Sahanda Yumurta, Ispanaklı Yumurta"
            "Dolmalar ve Sarmalar" -> "Örnek: Biber Dolması, Yaprak Sarma, Kabak Dolması"
            "Graten ve Fırın Yemekleri" -> "Örnek: Patates Graten, Fırın Makarna, Lazanya"
            "Kavurma ve Izgara Yemekleri" -> "Örnek: Et Kavurma, Tavuk Izgara, Sebze Izgara"
            "Salatalar" -> "Örnek: Çoban Salata, Ton Balıklı Salata, Nohut Salatası"
            "Fast Food ve Sokak Lezzetleri" -> "Örnek: Hamburger, Döner, Midye Tava"
            "Dünya Mutfağına Özgü Ana Yemekler" -> "Örnek: Sushi, Paella, Curry, Pizza"
            "Tatlılar" -> "Örnek: Baklava, Sütlaç, Kazandibi, Revani"
            "Vegan ve Vejetaryen Yemekler" -> "Örnek: Mercimek Köftesi, Kısır, Nohut Yemeği"
            "Glutensiz/Özel Diyet Yemekleri" -> "Örnek: Kinoa Salatası, Fırın Sebze, Yoğurtlu Kabak"
            "Tencere ve Güveç Yemekleri" -> "Örnek: Güveçte Türlü, Fırın Tavuk, Etli Sebze"
            else -> ""
        }

        val extraHints = when (mealType) {
            "Çorba" -> "Örnek çorbalar: Mercimek, Tarhana, Ezogelin"
            "Tatlı" -> "Örnek tatlılar: Sütlaç, Profiterol, Kazandibi"
            "Salata" -> "Örnek salatalar: Çoban Salata, Sezar Salata, Bulgur Salatası"
            "Izgara" -> "Örnek ızgaralar: Izgara Köfte, Tavuk Şiş, Balık Izgara"
            "Makarna" -> "Örnek makarnalar: Spagetti Napoliten, Fettucine Alfredo, Lazanya"
            "Pilav" -> "Örnek pilavlar: Bulgur Pilavı, Şehriyeli Pirinç Pilavı, İç Pilav"
            "Kahvaltı" -> "Örnek kahvaltılıklar: Menemen, Omlet, Sucuklu Yumurta"
            "Kebap" -> "Örnek kebaplar: Adana Kebap, Urfa Kebap, Tavuk Şiş"
            "Sandviç" -> "Örnek sandviçler: Tavuklu, Ton Balıklı, Sebzeli"
            "Pizza" -> "Örnek pizzalar: Margherita, Karışık Pizza, Sebzeli Pizza"
            "Fast Food" -> "Örnek fast foodlar: Hamburger, Patates Kızartması, Nuggets"
            "Tatlı ve Hamur" -> "Örnek hamur işleri: Baklava, Revani, Kek"
            "Dünya Mutfağı" -> "Örnek dünya yemekleri: Sushi, Tacos, Pad Thai"
            "Sebze Yemeği" -> "Örnek sebze yemekleri: Karnıyarık, Fasulye, Ispanak"
            "Et Yemeği" -> "Örnek et yemekleri: Et Sote, Tas Kebabı, Biftek"
            "Tavuk Yemeği" -> "Örnek tavuk yemekleri: Tavuk Sote, Fırın Tavuk, Güveç"
            "Balık Yemeği" -> "Örnek balıklar: Somon, Hamsi Tava, Levrek Buğulama"
            "Diyet" -> "Örnek diyet yemekleri: Yoğurtlu Kabak, Sebze Haşlama"
            "Glutensiz" -> "Örnek glutensiz tarifler: Kinoa Salatası, Fırın Sebze"
            "Vegan" -> "Örnek vegan tarifler: Mercimek Köftesi, Noodle, Humus"
            "Vejetaryen" -> "Örnek vejetaryenler: Sebzeli Makarna, Enginar"
            "Aperatif" -> "Örnek atıştırmalıklar: Sigara Böreği, Peynir Tabağı"
            "Fırın" -> "Örnek fırın tarifleri: Fırın Makarna, Mücver"
            "Hamur İşi" -> "Örnek hamur işleri: Poğaça, Açma, Börek"
            "Yemek" -> "Örnek yemekler: Kuru Fasulye, Taze Fasulye"
            else -> ""
        }

        return """
        Elimde şu malzemeler var: ${items.joinToString(", ")}.
        
        Yemek türü: "$mealType"

        🔴 Lütfen sadece "$mealType" türüne uygun tarifler öner. Diğer türlere ait tarifler geçersizdir.

        🔍 Örnek $mealType yemekleri: $extraHints1
        
        Lütfen bu malzemelere *öncelik vererek*, "$mealType" kategorisinde Dünya Mutfağın'da yaygın olarak yapılan, özgün sadece o türe uygun 3 farklı yemek tarifi oluştur.Ve yalnızca \"$mealType\" kategorisine **tam uygunlukta** tarifler öner. Diğer türlerden tarifler **önerme**.

        Her tarifte, malzeme eksikse "Eksik Malzemeler" başlığında belirt. Eksik olsa bile tarif öner.
        
        Her tarif için şu alanları **net ve ayrı** şekilde belirt:
        - Malzeme Kullanım Detayı: (örn: “2 adet büyük bostan patlıcan, 1 çay kaşığı tuz, 1 adet orta boy soğan”)
        - Tarif Adı:
        - Süre (örn. 30 dk):
        - Bölge:
        - Özet (1–2 cümlelik tanıtım, tadı veya kültürel kökeni hakkında bilgi ver): 
        - Açıklama (Adım adım, 3–5 aşamada hazırlanışı anlatmalı. Bu kısmı **boş bırakma**):
        - Kullanılan Malzemeler:
        - Eksik Malzemeler (eğer varsa, yoksa "Eksik yok" yaz):
        - Görsel Tanımı (DALL·E ile üretim için kullanılacak bir açıklama):
        
        ❗️ Örnek format:

        Tarif 1:
        Tarif Adı: Ananaslı Tavuk Salatası
        Süre: 25 dk
        Bölge: Tropikal
        Malzeme Kullanım Detayı:
        - 200 gram haşlanmış tavuk göğsü
        - 4 dilim ananas
        - Yarım demet marul
        - 2 yemek kaşığı yoğurt
        - Tuz, karabiber
        Açıklama: ...
        Kullanılan Malzemeler: Tavuk, Ananas, Marul
        Eksik Malzemeler: Yoğurt
        Görsel Tanımı: Ananas dilimleri ve tavuk parçaları ile hazırlanmış renkli bir salata

        Tarif 2:
        ...

        Lütfen sade, Türkçe ve tekrar etmeyen içerik üret.
        
        Aşağıdaki örnek tariflerden ilham alabilirsin:
        $extraHints1
        
    """.trimIndent()
    }

    private fun parseRecipesFromText(text: String): List<RecipeItem> {
        val recipes = mutableListOf<RecipeItem>()
        val entries = text.split(Regex("Tarif \\d+:")).map { it.trim() }.filter { it.isNotBlank() }

        for (entry in entries) {
            val summary = Regex("(?i)Özet\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)?.trim() ?: ""
            //val ingredientDetails = Regex("(?i)Malzeme Kullanım Detayı\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)?.trim() ?: ""
            val ingredientDetails = Regex(
                "(?i)Malzeme Kullanım Detayı\\s*:\\s*(.*?)(?=Tarif Adı:|Süre:|Bölge:|Özet:|Açıklama:|Kullanılan Malzemeler:|Eksik Malzemeler:|Görsel Tanımı:|$)",
                RegexOption.DOT_MATCHES_ALL
            ).find(entry)?.groupValues?.get(1)?.trim() ?: ""

            Log.d("RecipeDebug", "Malzeme Detayı: $ingredientDetails")

            Log.d("ParseRecipe", "Entry: $entry") //tarif bloğum için

            val name = Regex("(?i)Tarif Adı\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)?.trim() ?: "Bilinmeyen"
            val duration = Regex("(?i)Süre\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)?.trim() ?: "-"
            val region = Regex("(?i)Bölge\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)?.trim() ?: "-"
            //val description = Regex("(?i)Açıklama\\s*:\\s*(.*?)(?=Kullanılan Malzemeler:)").find(entry)?.groupValues?.get(1)?.trim() ?: "-"
            val description = Regex(
                "(?i)Açıklama\\s*:\\s*(.*?)(?=Kullanılan Malzemeler:)",
                RegexOption.DOT_MATCHES_ALL
            ).find(entry)?.groupValues?.get(1)?.trim() ?: "-"

            val ingredients = Regex("(?i)Kullanılan Malzemeler\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)
                ?.split(",")?.map { it.trim() } ?: emptyList()
            val missing = Regex("(?i)Eksik Malzemeler\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)
                ?.takeIf { it != "Eksik yok" }
                ?.split(",")?.map { it.trim() } ?: emptyList()
            val imageDescription = Regex("(?i)Görsel Tanımı\\s*:\\s*(.*)").find(entry)?.groupValues?.get(1)?.trim() ?: ""

            Log.d("ParseRecipe", "Name: $name, Description: $description, Image: $imageDescription")//yaptığım parse işlemi sonrası chek etmek için
            Log.d("RecipeDebug", "Açıklama alanı: $description")
            Log.d("RecipeDebug", "Açıklama alanı: $description")


            recipes.add(
                RecipeItem(
                    name = name,
                    imageUrl = imageDescription, // şimdilik imageDescription, DALL·E sonrası güncellenecek
                    duration = duration,
                    region = region,
                    description = description,
                    ingredients = ingredients,
                    missingIngredients = missing,
                    ingredientDetails = ingredientDetails,
                    summary = summary
                )
            )
        }

        return recipes
    }

    suspend fun generateImageFromDescription(description: String): String = withContext(Dispatchers.IO) {

        //promttun boş olma durumunda
        if (description.isBlank()) return@withContext ""

        val cleanedDescription = description //emoji ve özel karakterler 400 hatası verdirtiyor o yüzden temizledik promttu
            .replace(Regex("[^\\p{L}\\p{N}\\s,.!?-]"), "")
            .trim()

        //temizlediğimiz promttu aşırı ezme durumunda işlemi iptal etcez
        if (cleanedDescription.length < 10) return@withContext ""

        val promptJson = mapOf(
            "model" to "dall-e-2",
            "prompt" to cleanedDescription,
            "n" to 1,
            "size" to "256x256"
        )
        //1024x1024--512x512-256x256

        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            gson.toJson(promptJson)
        )

        try {
            val response = openAiService.generateImage(requestBody)
            return@withContext response.data.firstOrNull()?.url ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "" // Hata alınca boş string döcek artık ve uygulama çökmicek
        }

        /*
        val response = openAiService.generateImage(requestBody) //bu OpenAiImageResponse olcak
        return@withContext response.data.firstOrNull()?.url ?: ""
        */
    }


}
