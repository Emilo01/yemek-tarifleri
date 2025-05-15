package com.farukayata.yemektarifi.data.remote


import com.farukayata.yemektarifi.data.remote.model.User
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.storage.FirebaseStorage
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val adminUserId = "admin_user" // Admin kullanıcı ID'si

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun removeFavoriteRecipeFromSubcollection(userId: String, recipeName: String): Result<Unit> {
        return try {
            val favRef = usersCollection.document(userId).collection("favorites").document(recipeName)
            favRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipesFromSubcollection(userId: String): Result<List<RecipeItem>> {
        return try {
            val favs = usersCollection.document(userId).collection("favorites").get().await()
            val recipes = favs.toObjects(RecipeItem::class.java)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRecipeImageToStorage(imageUrl: String, recipeName: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val bytes = inputStream.readBytes()
                inputStream.close()

                val storageRef = FirebaseStorage.getInstance().reference
                    .child("recipe_images/${recipeName}_${System.currentTimeMillis()}.jpg")

                storageRef.putBytes(bytes).await()
                storageRef.downloadUrl.await().toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("StorageUpload", "Görsel yüklenemedi: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun addFavoriteRecipeWithImage(
        userId: String,
        recipe: RecipeItem
    ): Result<Unit> {
        return try {
            val newImageUrl = uploadRecipeImageToStorage(recipe.imageUrl, recipe.name)
            if (newImageUrl == null) {
                return Result.failure(Exception("Görsel Storage'a yüklenemedi veya kaynak erişilemez."))
            }
            val updatedRecipe = recipe.copy(imageUrl = newImageUrl)
            usersCollection.document(userId)
                .collection("favorites")
                .document(recipe.name)
                .set(updatedRecipe)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}












/*
package com.farukayata.yemektarifi.data.remote


import com.farukayata.yemektarifi.data.remote.model.User
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.storage.FirebaseStorage
import java.net.URL

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val adminUserId = "admin_user" // Admin kullanıcı ID'si

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFavoriteRecipe(userId: String, recipeName: String): Result<Unit> {
        return try {
            // Kullanıcının favorilerine ekle
            val userDoc = usersCollection.document(userId)
            val user = userDoc.get().await().toObject(User::class.java)

            if (user != null) {
                val updatedFavorites = user.favoriteRecipes + recipeName
                userDoc.update("favoriteRecipes", updatedFavorites).await()

                // Admin kullanıcısının favorilerine de ekle
                val adminDoc = usersCollection.document(adminUserId)
                val adminUser = adminDoc.get().await().toObject(User::class.java)

                if (adminUser != null) {
                    val adminFavorites = adminUser.favoriteRecipes + recipeName
                    adminDoc.update("favoriteRecipes", adminFavorites).await()
                } else {
                    // Admin kullanıcısı yoksa oluştur
                    val newAdminUser = User(
                        id = adminUserId,
                        name = "Admin",
                        email = "admin@yemektarifi.com",
                        favoriteRecipes = listOf(recipeName)
                    )
                    adminDoc.set(newAdminUser).await()
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavoriteRecipe(userId: String, recipeName: String): Result<Unit> {
        return try {
            // Kullanıcının favorilerinden çıkar
            val userDoc = usersCollection.document(userId)
            val user = userDoc.get().await().toObject(User::class.java)

            if (user != null) {
                val updatedFavorites = user.favoriteRecipes.filter { it != recipeName }
                userDoc.update("favoriteRecipes", updatedFavorites).await()

                // Admin kullanıcısının favorilerinden de çıkar
                val adminDoc = usersCollection.document(adminUserId)
                val adminUser = adminDoc.get().await().toObject(User::class.java)

                if (adminUser != null) {
                    val adminFavorites = adminUser.favoriteRecipes.filter { it != recipeName }
                    adminDoc.update("favoriteRecipes", adminFavorites).await()
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipes(userId: String): Result<List<String>> {
        return try {
            val user = getUser(userId).getOrNull()
            if (user != null) {
                Result.success(user.favoriteRecipes)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SUBCOLLECTION TABANLI FAVORİLER ---
    suspend fun addFavoriteRecipeToSubcollection(userId: String, recipe: RecipeItem): Result<Unit> {
        return try {
            val favRef = usersCollection.document(userId).collection("favorites").document(recipe.name)
            favRef.set(recipe).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavoriteRecipeFromSubcollection(userId: String, recipeName: String): Result<Unit> {
        return try {
            val favRef = usersCollection.document(userId).collection("favorites").document(recipeName)
            favRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipesFromSubcollection(userId: String): Result<List<RecipeItem>> {
        return try {
            val favs = usersCollection.document(userId).collection("favorites").get().await()
            val recipes = favs.toObjects(RecipeItem::class.java)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRecipeImageToStorage(imageUrl: String, recipeName: String): String? {
        return try {
            // 1. Görseli indir
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            inputStream.close()

            // 2. Storage referansı oluştur
            val storageRef = FirebaseStorage.getInstance().reference
                .child("recipe_images/${recipeName}_${System.currentTimeMillis()}.jpg")

            // 3. Yükle
            val uploadTask = storageRef.putBytes(bytes).await()
            // 4. Download URL al
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun addFavoriteRecipeWithImage(
        userId: String,
        recipe: RecipeItem
    ): Result<Unit> {
        return try {
            // 1. Görseli Storage'a yükle
            val newImageUrl = uploadRecipeImageToStorage(recipe.imageUrl, recipe.name)
            // 2. Kalıcı linki RecipeItem'a ata
            val updatedRecipe = recipe.copy(imageUrl = newImageUrl ?: recipe.imageUrl)
            // 3. Firestore'a kaydet
            usersCollection.document(userId)
                .collection("favorites")
                .document(recipe.name)
                .set(updatedRecipe)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


 */









/*
package com.farukayata.yemektarifi.data.remote


import com.farukayata.yemektarifi.data.remote.model.User
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val adminUserId = "admin_user" // Admin kullanıcı ID'si

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFavoriteRecipe(userId: String, recipeName: String): Result<Unit> {
        return try {
            // Kullanıcının favorilerine ekle
            val userDoc = usersCollection.document(userId)
            val user = userDoc.get().await().toObject(User::class.java)

            if (user != null) {
                val updatedFavorites = user.favoriteRecipes + recipeName
                userDoc.update("favoriteRecipes", updatedFavorites).await()

                // Admin kullanıcısının favorilerine de ekle
                val adminDoc = usersCollection.document(adminUserId)
                val adminUser = adminDoc.get().await().toObject(User::class.java)

                if (adminUser != null) {
                    val adminFavorites = adminUser.favoriteRecipes + recipeName
                    adminDoc.update("favoriteRecipes", adminFavorites).await()
                } else {
                    // Admin kullanıcısı yoksa oluştur
                    val newAdminUser = User(
                        id = adminUserId,
                        name = "Admin",
                        email = "admin@yemektarifi.com",
                        favoriteRecipes = listOf(recipeName)
                    )
                    adminDoc.set(newAdminUser).await()
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavoriteRecipe(userId: String, recipeName: String): Result<Unit> {
        return try {
            // Kullanıcının favorilerinden çıkar
            val userDoc = usersCollection.document(userId)
            val user = userDoc.get().await().toObject(User::class.java)

            if (user != null) {
                val updatedFavorites = user.favoriteRecipes.filter { it != recipeName }
                userDoc.update("favoriteRecipes", updatedFavorites).await()

                // Admin kullanıcısının favorilerinden de çıkar
                val adminDoc = usersCollection.document(adminUserId)
                val adminUser = adminDoc.get().await().toObject(User::class.java)

                if (adminUser != null) {
                    val adminFavorites = adminUser.favoriteRecipes.filter { it != recipeName }
                    adminDoc.update("favoriteRecipes", adminFavorites).await()
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipes(userId: String): Result<List<String>> {
        return try {
            val user = getUser(userId).getOrNull()
            if (user != null) {
                Result.success(user.favoriteRecipes)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SUBCOLLECTION TABANLI FAVORİLER ---
    suspend fun addFavoriteRecipeToSubcollection(userId: String, recipe: RecipeItem): Result<Unit> {
        return try {
            val favRef = usersCollection.document(userId).collection("favorites").document(recipe.name)
            favRef.set(recipe).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavoriteRecipeFromSubcollection(userId: String, recipeName: String): Result<Unit> {
        return try {
            val favRef = usersCollection.document(userId).collection("favorites").document(recipeName)
            favRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipesFromSubcollection(userId: String): Result<List<RecipeItem>> {
        return try {
            val favs = usersCollection.document(userId).collection("favorites").get().await()
            val recipes = favs.toObjects(RecipeItem::class.java)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


 */







/*
package com.farukayata.yemektarifi.data.remote


import com.farukayata.yemektarifi.data.remote.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val adminUserId = "admin_user" // Admin kullanıcı ID'si

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFavoriteRecipe(userId: String, recipeName: String): Result<Unit> {
        return try {
            // Kullanıcının favorilerine ekle
            val userDoc = usersCollection.document(userId)
            val user = userDoc.get().await().toObject(User::class.java)

            if (user != null) {
                val updatedFavorites = user.favoriteRecipes + recipeName
                userDoc.update("favoriteRecipes", updatedFavorites).await()

                // Admin kullanıcısının favorilerine de ekle
                val adminDoc = usersCollection.document(adminUserId)
                val adminUser = adminDoc.get().await().toObject(User::class.java)

                if (adminUser != null) {
                    val adminFavorites = adminUser.favoriteRecipes + recipeName
                    adminDoc.update("favoriteRecipes", adminFavorites).await()
                } else {
                    // Admin kullanıcısı yoksa oluştur
                    val newAdminUser = User(
                        id = adminUserId,
                        name = "Admin",
                        email = "admin@yemektarifi.com",
                        favoriteRecipes = listOf(recipeName)
                    )
                    adminDoc.set(newAdminUser).await()
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavoriteRecipe(userId: String, recipeName: String): Result<Unit> {
        return try {
            // Kullanıcının favorilerinden çıkar
            val userDoc = usersCollection.document(userId)
            val user = userDoc.get().await().toObject(User::class.java)

            if (user != null) {
                val updatedFavorites = user.favoriteRecipes.filter { it != recipeName }
                userDoc.update("favoriteRecipes", updatedFavorites).await()

                // Admin kullanıcısının favorilerinden de çıkar
                val adminDoc = usersCollection.document(adminUserId)
                val adminUser = adminDoc.get().await().toObject(User::class.java)

                if (adminUser != null) {
                    val adminFavorites = adminUser.favoriteRecipes.filter { it != recipeName }
                    adminDoc.update("favoriteRecipes", adminFavorites).await()
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipes(userId: String): Result<List<String>> {
        return try {
            val user = getUser(userId).getOrNull()
            if (user != null) {
                Result.success(user.favoriteRecipes)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

 */