package com.example.cooking.data;

import com.example.cooking.model.AuthRequest;
import com.example.cooking.model.AuthResponse;
import com.example.cooking.model.ProductDTO;
import com.example.cooking.model.RecipeDTO;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @GET("auth/check-email")
    Call<Map<String, Boolean>> checkEmail(@Query("email") String email);

    @GET("products/available")
    Call<List<ProductDTO>> getUserProducts();

    @POST("products/available")
    Call<Void> addUserProduct(@Body ProductDTO product);

    @DELETE("products/available/{productId}")
    Call<Void> removeUserProduct(@Path("productId") Long productId);

    @GET("products/catalog")
    Call<List<ProductDTO>> getProductCatalog(
            @Query("category") String category,
            @Query("search") String search
    );

    @GET("products/admin/all")
    Call<List<ProductDTO>> getAllProductsForAdmin();

    @POST("products")
    Call<ProductDTO> createProduct(@Body ProductDTO product);

    @PUT("products/{id}")
    Call<ProductDTO> updateProduct(@Path("id") Long id, @Body ProductDTO product);

    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") Long id);

    @GET("recipes/recommended")
    Call<List<RecipeDTO>> getRecommendedRecipes(@Query("limit") int limit);

    @GET("recipes/favorites")
    Call<List<RecipeDTO>> getFavorites();

    @GET("recipes/{id}")
    Call<RecipeDTO> getRecipeById(@Path("id") Long id);

    @POST("recipes/{recipeId}/favorite")
    Call<Void> addToFavorites(@Path("recipeId") Long recipeId);

    @DELETE("recipes/{recipeId}/favorite")
    Call<Void> removeFromFavorites(@Path("recipeId") Long recipeId);

    @GET("recipes/search")
    Call<List<RecipeDTO>> searchRecipes(
            @Query("query") String query,
            @Query("productIds") List<Long> productIds,
            @Query("minIngredients") int minIngredients
    );

    @GET("recipes/admin/all")
    Call<List<RecipeDTO>> getAllRecipesForAdmin();

    @POST("recipes")
    Call<RecipeDTO> createRecipe(@Body RecipeDTO recipe);

    @DELETE("recipes/{id}")
    Call<Void> deleteRecipe(@Path("id") Long id);
}