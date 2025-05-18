package com.farukayata.yemektarifi.presentation.screens.home

import EditItemsBottomSheet
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
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyRow
import com.farukayata.yemektarifi.data.remote.ui.components.CategoryCard
import com.farukayata.yemektarifi.data.remote.ui.components.LoadingAnimation
import com.farukayata.yemektarifi.data.remote.ui.components.LottieAnimationView
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import com.farukayata.yemektarifi.data.remote.ui.components.BottomNavigationBar
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp

import com.farukayata.yemektarifi.data.remote.ui.components.HomeRecipeCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val listState = rememberLazyListState()
    val showToolbar by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30 }
    }
    val appBarHeight: Dp = 56.dp
    val popularRecipes by viewModel.popularRecipes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPopularRecipes()
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showToolbar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(shadowElevation = 4.dp) {
                    TopAppBar(
                        title = { Text("Yemek Tarifi Oluşturucu") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(appBarHeight)
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            val navigateToResult by viewModel.navigateToResult.collectAsState()

            LaunchedEffect(navigateToResult) {
                if (navigateToResult) {
                    navController.navigate("result")
                }
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                viewModel.setSelectedImage(uri)
                viewModel.convertImageToBase64Compressed_2_1(uri, context.contentResolver)
                Log.d("Base64Image", "Base64: ${viewModel.selectedImageBase64.value}")
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //foto butonu
                    item {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Fotoğraf Seç",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    //seçtiğimiz görsel
                    item {
                        selectedImageUri?.let { uri ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Seçilen Fotoğraf",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    //ai butonalrı
                    item {
                        if (selectedImageBase64.orEmpty().isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.detectLabels() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Nesneleri Tespit Et")
                                }
                                Button(
                                    onClick = { viewModel.analyzeWithOpenAi() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("OpenAI ile Analiz Et")
                                }
                            }
                        }
                    }
                    //ürünler
                    if (detectedLabels.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tespit Edilen Nesneler",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(detectedLabels) { label ->
                            Text(text = "${label.description} (${(label.score * 100).toInt()}%)")
                        }
                    }
                    //kategorize ettiğimiz ürünler
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
                        item {
                            Text(
                                text = "Kategorize Edilmiş Ürünler",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        sortedGroups.forEach { (category, items) ->
                            item {
                                CategoryCard(
                                    categoryName = category,
                                    items = items
                                )
                            }
                        }
                        item {
                            Button(
                                onClick = { showEditBottomSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Ürünleri Onayla / Düzenle")
                            }
                        }
                    }
                    //lotti aimasyonu
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingAnimation(
                                    modifier = Modifier.size(150.dp)
                                )
                            }
                        }
                    }
                    //adminden çektiğimiz favlar
                    if (popularRecipes.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    text = "En Çok Beğenilen Yemek Tarifleri",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    items(popularRecipes) { recipe ->
                                        HomeRecipeCard(
                                            recipe = recipe,
                                            onClick = {
                                                val encodedName = java.net.URLEncoder.encode(recipe.name, java.nio.charset.StandardCharsets.UTF_8.toString())
                                                navController.navigate("homeRecipeDetail/$encodedName")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                //uyarı alert
                if (userMessage != null) {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.clearUserMessage()
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.clearUserMessage()
                                }
                            ) {
                                Text("Tamam")
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
                                    text = userMessage!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LottieAnimationView()
                            }
                        }
                    )
                }
                //edit bottomsheet
                if (showEditBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showEditBottomSheet = false }
                    ) {
                        EditItemsBottomSheet(
                            initialItems = categorizedItems,
                            onFinalize = { finalList: List<CategorizedItem>, newInputs: List<String> ->
                                viewModel.setUserEditedItems(finalList)
                                viewModel.setFreeTextInputs(newInputs)
                                viewModel.triggerResultNavigation()
                                showEditBottomSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}