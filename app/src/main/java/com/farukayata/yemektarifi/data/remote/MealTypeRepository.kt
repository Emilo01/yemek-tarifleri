package com.farukayata.yemektarifi.data.remote.repository

import com.farukayata.yemektarifi.R
import com.farukayata.yemektarifi.data.remote.model.MealType

object MealTypeRepository {
    val mealTypes = listOf(
        MealType("Sulu Yemekler", R.drawable.sulu_yemekler),
        MealType("Et Yemekleri", R.drawable.et_yemekleri),
        MealType("Sebze Yemekleri", R.drawable.sebze_yemekleri), // klasör varsa düzeltilecek
        MealType("Köfte ve Kıyma Yemekleri", R.drawable.kofte_ve_kiyma_tarifleri),
        MealType("Tavuk Yemekleri", R.drawable.tavuk_yemekleri),
        MealType("Balık ve Deniz Ürünleri", R.drawable.balik_ve_deniz_yemekleri),
        MealType("Baklagil Yemekleri", R.drawable.baklagil_yemekleri),
        MealType("Pilavlar", R.drawable.pilavlar),
        MealType("Makarna ve Erişte Çeşitleri", R.drawable.makarna_ve_eriste_yemekleri),
        MealType("Hamur İşleri", R.drawable.hamur_isleri),
        MealType("Çorbalar", R.drawable.corbalar),
        MealType("Kahvaltılıklar", R.drawable.kahvaltilar),
        MealType("Zeytinyağlı Yemekler", R.drawable.zeytin_yagli_yemekler),
        MealType("Yumurtalı Yemekler", R.drawable.yumurtali_yemekler),
        MealType("Dolmalar ve Sarmalar", R.drawable.dolmalar_ve_sarmalar),
        MealType("Graten ve Fırın Yemekleri", R.drawable.graten_ve_firin_yemekleri),
        MealType("Kavurma ve Izgara Yemekleri", R.drawable.kavurma_ve_izgara_yemekleri),
        MealType("Salatalar", R.drawable.salatalar),
        MealType("Fast Food ve Sokak Lezzetleri", R.drawable.fast_food_ve_sokak_lezzetleri),
        MealType("Dünya Mutfağına Özgü Ana Yemekler", R.drawable.dunya_mutfagina_ozgu_ana_yemekler),
        MealType("Tatlılar", R.drawable.tatlilar),
        MealType("Vegan ve Vejetaryen Yemekler", R.drawable.vegan_ve_vejetaryan_yemekleri),
        MealType("Glutensiz/Özel Diyet Yemekleri", R.drawable.glutensiz_ozel_diyet_yemekleri),
        MealType("Tencere ve Güveç Yemekleri", R.drawable.tencere_ve_guvec_yemekleri)
    )
}
