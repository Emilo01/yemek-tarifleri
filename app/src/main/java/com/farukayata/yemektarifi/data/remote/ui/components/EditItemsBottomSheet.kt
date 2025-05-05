// 🔽 Gerekli importlar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import com.farukayata.yemektarifi.data.remote.model.categoryStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditItemsBottomSheet(
    initialItems: List<CategorizedItem>,
    onFinalize: (List<CategorizedItem>, List<String>) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var editedItems by remember { mutableStateOf(initialItems.toMutableList()) }
    var newItemText by remember { mutableStateOf("") }

    val removedItems = remember { mutableStateListOf<CategorizedItem>() } //sildiğimiz chipsleri tuttuk
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ürünleri ${if (step == 1) "çıkar" else "ekle"}", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        val categoryOrder = listOf(
            "Et ve Et Ürünleri", "Balık ve Deniz Ürünleri", "Yumurta ve Süt Ürünleri",
            "Tahıllar ve Unlu Mamuller", "Baklagiller", "Sebzeler", "Meyveler",
            "Baharatlar ve Tat Vericiler", "Yağlar ve Sıvılar", "Konserve ve Hazır Gıdalar",
            "Tatlı Malzemeleri ve Kuruyemişler"
        )

        val groupedItems = categoryOrder.flatMap { category ->
            editedItems.filter { it.category == category }
        }

        FlowRow {
            //editedItems.forEach { item ->
            //artık yukarıda yaptığımız gruplamayı kulaarak renklendirdiğimiz chipsleri grup grup peş peşe gösteriyoruz
            groupedItems.forEach { item ->
                val isVisible = !removedItems.contains(item)
                val styleColor = categoryStyles[item.category]?.color ?: MaterialTheme.colorScheme.surfaceVariant

                AnimatedVisibility(
                    visible = isVisible,
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    AssistChip(
                        onClick = {
                            if (step == 1) {
                                removedItems.add(item) // önce gizle
                                coroutineScope.launch {
                                    delay(300) // 300ms sonra listeden sil
                                    editedItems.remove(item)
                                    //removedItems.remove(item) -> bu varken silinne ürün direkt kaldırılıyordu üstü çizili gözükmüyordu
                                }
                            }
                        },
                        label = { Text("${item.emoji} ${item.name}") },
                        colors = AssistChipDefaults.assistChipColors(
                            //containerColor = if (removedItems.contains(item)) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant,
                            containerColor = styleColor,//artık chips renkleri ürünün aitt olduğu kategory tipi ile aynı renk
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        //&& removedItems.isNotEmpty() -> kısmını if içinden kaldırdık sebebi ürün eklem kısmında ürün silkmeden gittiğimizde text field gözükmüyordu
        if (step == 2 ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text("Yeni ürün girin (ör: 🥒 Salatalık - Sebzeler)") },
                modifier = Modifier.fillMaxWidth()
            )

            //artık silinen ürünler üstü çizili burda gösterilcek
            if (removedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Silinen Ürünler",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow {
                    removedItems.forEach { item ->
                        AssistChip(
                            onClick = {
                                // Kullanıcı silinen ürünleri geri eklediği kısım
                                editedItems.add(item)
                                removedItems.remove(item)
                            },
                            label = {
                                Text(
                                    "${item.emoji} ${item.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                        color = Color.Red
                                    )
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (step == 1) {
                    step = 2
                } else {
                    val userInputs = newItemText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onFinalize(editedItems, userInputs)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (step == 1) "Devam Et" else "Listeyi Güncelle")
        }
    }
}
