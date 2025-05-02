package com.farukayata.yemektarifi.presentation.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment

import androidx.compose.material3.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import com.farukayata.yemektarifi.data.remote.ui.components.CategoryCard
import com.farukayata.yemektarifi.data.remote.ui.components.EditItemsBottomSheet
import com.farukayata.yemektarifi.data.remote.ui.components.LoadingAnimation
import com.farukayata.yemektarifi.data.remote.ui.components.LottieAnimationView
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val selectedImageBase64 by viewModel.selectedImageBase64.collectAsState()
    val detectedLabels by viewModel.detectedLabels.collectAsState()

    val localizedObjects by viewModel.localizedObjects.collectAsState()
    val openAiItems by viewModel.openAiItems.collectAsState()
    val categorizedItems by viewModel.categorizedItems.collectAsState()

    val userMessage by viewModel.userMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showEditBottomSheet by remember { mutableStateOf(false) }


    val context = LocalContext.current //-> contentresolver erişimi için lazım

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setSelectedImage(uri)
        viewModel.convertImageToBase64Compressed_2_1(uri, context.contentResolver)

        /*-vision dan 4o ya geçince storrage yüklene fotoyu kaydetmeye gerrek kalmadı
        uri?.let {
            viewModel.uploadImageToFirebase(it)
        }
        */

        Log.d(
            "Base64Image",
            "Base64: ${viewModel.selectedImageBase64.value}"
        ) //base64 çevrilmiş hali görselin
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Fotoğraf Seç")
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Seçilen Fotoğraf",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedImageBase64.orEmpty().isNotEmpty()) {
                Button(
                    onClick = { viewModel.detectLabels() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Görseldeki Nesneleri Tespit Et (label Detection)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.analyzeWithOpenAi() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "OpenAI ile Analiz Et")
                }

                //popup
                var showDialog by remember { mutableStateOf(false) }

                LaunchedEffect(userMessage) {
                    if (!userMessage.isNullOrEmpty()) {
                        showDialog = true
                    }
                }

                if (showDialog) {
                    userMessage?.let { message ->
                        AlertDialog(
                            onDismissRequest = {
                                showDialog = false
                                viewModel.clearUserMessage()
                            },
                            confirmButton = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Button(
                                        onClick = {
                                            showDialog = false
                                            viewModel.clearUserMessage()
                                        }
                                    ) {
                                        Text("Tamam")
                                    }
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Uyarı",
                                        color = Color(0xFFEF5350),
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LottieAnimationView()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(detectedLabels) { label ->
                    Text(text = "${label.description} (${(label.score * 100).toInt()}%)")
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val categoryOrder = listOf(
                "Et ve Et Ürünleri",
                "Balık ve Deniz Ürünleri",
                "Yumurta ve Süt Ürünleri",
                "Tahıllar ve Unlu Mamuller",
                "Baklagiller",
                "Sebzeler",
                "Meyveler",
                "Baharatlar ve Tat Vericiler",
                "Yağlar ve Sıvılar",
                "Konserve ve Hazır Gıdalar",
                "Tatlı Malzemeleri ve Kuruyemişler"
            )

            val grouped = categorizedItems.groupBy { it.category }
            val sortedGroups = categoryOrder.mapNotNull { key -> grouped[key]?.let { key to it } }

            if (categorizedItems.isNotEmpty()) {
                Button(
                    onClick = { showEditBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Ürünleri Onayla / Düzenle")
                }
            }

            if (showEditBottomSheet) {
                ModalBottomSheet(onDismissRequest = { showEditBottomSheet = false }) {
                    EditItemsBottomSheet(
                        initialItems = categorizedItems,
                        onFinalize = { finalList,newInputs ->
                            viewModel.setUserEditedItems(finalList)
                            //viewModel.reAnalyzeWithEditedItems(finalList)
                            viewModel.reAnalyzeWithFreeTextList(finalList,newInputs)
                            showEditBottomSheet = false
                        }
                    )
                }
            }



            //Fade-in animasyon ve loading göstergesi
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    //CircularProgressIndicator() lottie ekledik
                    LoadingAnimation(
                        modifier = Modifier.size(150.dp)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = categorizedItems.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    LazyColumn {
                        sortedGroups.forEach { (category, items) ->
                            item {
                                CategoryCard(
                                    categoryName = category,
                                    items = items
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


//HomeScreen doğrudan ViewModelden selectedImageUriı gözlemlicek
//launcher ile fotoğraf seçince direkt viewModel.setSelectedImage(uri) dicez

/*
    Button(
                onClick = {
                    viewModel.detectLabels()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Görseldeki Ürünleri Tespit Et")
            }
 */

/*
                /*
        LazyColumn {
    items(detectedObjects) { obj ->
        Text(text = "${obj.name} (${(obj.score * 100).toInt()}%)")
    }
}

         */


 */

/*
LazyColumn {
    items(localizedObjects) { obj ->
        Text(text = "${obj.name} (${(obj.score * 100).toInt()}%)")
        Spacer(modifier = Modifier.height(4.dp))
    }
}
*/

        /*-> columnn u box yaptık
            Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                launcher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Fotoğraf Seç")
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Seçilen Fotoğraf",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedImageBase64.orEmpty().isNotEmpty()) {
            Button(
                onClick = {
                    //viewModel.detectObjects()
                    viewModel.detectLabels()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Görseldeki Nesneleri Tespit Et (label Detection)")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.analyzeWithOpenAi()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "OpenAI ile Analiz Et")
            }

            userMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }


        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(detectedLabels) { label ->
                Text(text = "${label.description} (${(label.score * 100).toInt()}%)")
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val categoryOrder = listOf(
            "Et ve Et Ürünleri",
            "Balık ve Deniz Ürünleri",
            "Yumurta ve Süt Ürünleri",
            "Tahıllar ve Unlu Mamuller",
            "Baklagiller",
            "Sebzeler",
            "Meyveler",
            "Baharatlar ve Tat Vericiler",
            "Yağlar ve Sıvılar",
            "Konserve ve Hazır Gıdalar",
            "Tatlı Malzemeleri ve Kuruyemişler"
        )

        val grouped = categorizedItems.groupBy { it.category }
        val sortedGroups = categoryOrder.mapNotNull { key ->
            grouped[key]?.let { key to it }
        }

        LazyColumn {
            sortedGroups.forEach { (category, items) ->
                item {
                    CategoryCard(
                        categoryName = category,
                        items = items
                    )
                }
            }
        }


        /*-> aşağıdaki compose yapısı ile uyumlu olcak yeni bir listeleme metodu yyazdık
        val grouped = categorizedItems.groupBy { it.category }
        val sortedGroups = categoryOrder.mapNotNull { key ->
            grouped[key]?.let { key to it }
        }

        LazyColumn {
            sortedGroups.forEach { (category, items) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(items) { item ->
                    Text(text = "${item.emoji} ${item.name}")
                }
            }
        }

         */
    }
}

         */


        /*--bu kısım yerine yukarıda fade in kullandık ve loaing i onu içinne aldık ve tam sayfa yerine sadece liste ekranı gelen yerde listeleme ola kısımda loadig bar dönüyor
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

         */
         */