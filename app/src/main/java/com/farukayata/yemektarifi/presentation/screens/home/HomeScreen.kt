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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

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



    val context = LocalContext.current //-> contentresolver erişimi için lazım

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setSelectedImage(uri)
        viewModel.convertImageToBase64Compressed_2_1(uri, context.contentResolver)

        uri?.let {
            viewModel.uploadImageToFirebase(it)
        }

        Log.d("Base64Image", "Base64: ${viewModel.selectedImageBase64.value}") //base64 çevrilmiş hali görselin
    }

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

        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(detectedLabels) { label ->
                Text(text = "${label.description} (${(label.score * 100).toInt()}%)")
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // OpenAI'dan gelen ürünler listesi
        LazyColumn {
            items(openAiItems) { item ->
                Text(text = item)
                Spacer(modifier = Modifier.height(4.dp))
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