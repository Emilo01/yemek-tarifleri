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
    private val adminCollection = firestore.collection("admin_recipes")
    // Admin koleksiyonumuz , tüm tarifleri getirtmek içi kullancaz
    private val adminUserId = "iiinsswnIob4JP0T8AqptPtBc3F2"

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

            // Admin için fav dan kaldırdık
            val adminFavRef = usersCollection.document(adminUserId).collection("favorites").document(recipeName)
            adminFavRef.delete().await()

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
            //güncel tarifler için
            val updatedRecipe = recipe.copy(imageUrl = newImageUrl)

            usersCollection.document(userId)
                .collection("favorites")
                .document(recipe.name)
                .set(updatedRecipe)
                .await()

            //admin kullanıcı için ekleme yaptığımız kısım
            usersCollection.document(adminUserId)
                .collection("favorites")
                .document(recipe.name)
                .set(updatedRecipe)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin koleksiyonundan tüm tarifleri getirtcez burdan fuature olcak bu
    suspend fun getAllAdminRecipes(): Result<List<RecipeItem>> {
        return try {
            val recipes = adminCollection.get().await()
            Result.success(recipes.toObjects(RecipeItem::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}