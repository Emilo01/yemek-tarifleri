package com.farukayata.yemektarifi.data.remote.repository

import com.farukayata.yemektarifi.R
import com.farukayata.yemektarifi.data.remote.model.MealType

object MealTypeRepository {
    val mealTypes = listOf(
        MealType("Sulu Yemekler", R.drawable.sulu_yemekler, "Geleneksel Türk mutfağının vazgeçilmez sulu yemekleri, doyurucu ve besleyicidir."),
        MealType("Et Yemekleri", R.drawable.et_yemekleri, "Kırmızı etin lezzetini sofralarınıza taşıyan tarifler."),
        MealType("Sebze Yemekleri", R.drawable.sebze_yemekleri, "Vitamin ve mineral deposu sebzelerle hazırlanan sağlıklı yemekler."),
        MealType("Köfte ve Kıyma Yemekleri", R.drawable.kofte_ve_kiyma_tarifleri, "Kıymanın farklı halleriyle sofralarınıza lezzet katın."),
        MealType("Tavuk Yemekleri", R.drawable.tavuk_yemekleri, "Pratik ve lezzetli tavuk yemekleriyle protein ihtiyacınızı karşılayın."),
        MealType("Balık ve Deniz Ürünleri", R.drawable.balik_ve_deniz_yemekleri, "Omega-3 kaynağı balık ve deniz ürünleriyle sağlıklı tarifler."),
        MealType("Baklagil Yemekleri", R.drawable.baklagil_yemekleri, "Lif ve protein açısından zengin baklagil yemekleri."),
        MealType("Pilavlar", R.drawable.pilavlar, "Her yemeğin yanına yakışan, çeşit çeşit pilav tarifleri."),
        MealType("Makarna ve Erişte Çeşitleri", R.drawable.makarna_ve_eriste_yemekleri, "Farklı soslarla zenginleşen makarna ve erişte tarifleri."),
        MealType("Hamur İşleri", R.drawable.hamur_isleri, "Börek, poğaça ve daha fazlası: Hamur işlerinin en güzelleri."),
        MealType("Çorbalar", R.drawable.corbalar, "Her sofranın başlangıcı, şifa kaynağı çorba tarifleri."),
        MealType("Kahvaltılıklar", R.drawable.kahvaltilar, "Güne enerjik başlamak için nefis kahvaltılıklar."),
        MealType("Zeytinyağlı Yemekler", R.drawable.zeytin_yagli_yemekler, "Ege mutfağının hafif ve sağlıklı zeytinyağlıları."),
        MealType("Yumurtalı Yemekler", R.drawable.yumurtali_yemekler, "Protein deposu yumurtayla hazırlanan pratik tarifler."),
        MealType("Dolmalar ve Sarmalar", R.drawable.dolmalar_ve_sarmalar, "Geleneksel dolma ve sarma tarifleriyle sofralarınızı şenlendirin."),
        MealType("Graten ve Fırın Yemekleri", R.drawable.graten_ve_firin_yemekleri, "Fırında pişen, üzeri kızarmış nefis yemekler."),
        MealType("Kavurma ve Izgara Yemekleri", R.drawable.kavurma_ve_izgara_yemekleri, "Izgara ve kavurma tarifleriyle etin en doğal hali."),
        MealType("Salatalar", R.drawable.salatalar, "Rengarenk ve sağlıklı salata tarifleri."),
        MealType("Fast Food ve Sokak Lezzetleri", R.drawable.fast_food_ve_sokak_lezzetleri, "Evde kolayca hazırlayabileceğiniz fast food ve sokak lezzetleri."),
        MealType("Dünya Mutfağına Özgü Ana Yemekler", R.drawable.dunya_mutfagina_ozgu_ana_yemekler, "Dünya mutfağından seçme ana yemek tarifleri."),
        MealType("Tatlılar", R.drawable.tatlilar, "Tatlı krizlerine birebir, pratik ve lezzetli tatlı tarifleri."),
        MealType("Vegan ve Vejetaryen Yemekler", R.drawable.vegan_ve_vejetaryan_yemekleri, "Hayvansal ürün içermeyen sağlıklı tarifler."),
        MealType("Glutensiz/Özel Diyet Yemekleri", R.drawable.glutensiz_ozel_diyet_yemekleri, "Glutensiz ve özel diyetlere uygun yemekler."),
        MealType("Tencere ve Güveç Yemekleri", R.drawable.tencere_ve_guvec_yemekleri, "Ağır ateşte pişen tencere ve güveç yemekleri.")
    )
}