package com.farukayata.yemektarifi.data.remote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditItemsBottomSheet(
    initialItems: List<CategorizedItem>,
    onFinalize: (List<CategorizedItem>,List<String>) -> Unit
    //revize ettik
) {
    var step by remember { mutableStateOf(1) }
    var editedItems by remember { mutableStateOf(initialItems.toMutableList()) }
    var newItemText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ürünleri ${if (step == 1) "çıkar" else "ekle"}", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow {
            editedItems.forEach { item ->
                AssistChip(
                    onClick = {
                        if (step == 1) editedItems.remove(item)
                    },
                    label = { Text("${item.emoji} ${item.name}") }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (step == 2) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text("Yeni ürün girin (ör: 🥒 Salatalık - Sebzeler)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (step == 1) {
                    step = 2
                } else {
                    if (newItemText.contains("-")) {
                        val parts = newItemText.split("-")
                        val emoji = parts[0].takeWhile { !it.isLetterOrDigit() }.trim()
                        val name = parts[0].dropWhile { !it.isLetterOrDigit() }.trim()
                        val category = parts[1].trim()
                        editedItems.add(CategorizedItem(emoji, name, category))
                    }
                    //onFinalize(editedItems, listOf(newItemText)) muz,elme yapımıyor du
                    //kullanıcının eksik girdi girmesini önlemek içinn revize ettik
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
