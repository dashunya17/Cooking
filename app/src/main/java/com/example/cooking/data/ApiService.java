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
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest authRequest);

    @POST("auth/register")
    Call<AuthResponse> register(@Body AuthRequest authRequest);

    @GET("auth/check-email")
    Call<Map<String, Boolean>> checkEmail(@Query("email") String email);

    @GET("products/available")
    Call<List<ProductDTO>> getUserProducts();

    @POST("products/available")
    Call<Void> addUserProduct(@Body ProductDTO productDTO);

    @DELETE("products/available/{productId}")
    Call<Void> removeUserProduct(@Path("productId") Long productId);

    @GET("products/catalog")
    Call<List<ProductDTO>> getProductCatalog(
            @Query("category") String category,
            @Query("search") String search
    );

    @GET("recipes/recommended")
    Call<List<RecipeDTO>> getRecommendedRecipes(@Query("limit") int limit);

    @GET("recipes/{id}")
    Call<RecipeDTO> getRecipeById(@Path("id") Long id);

    @POST("recipes/{recipeId}/favorite")
    Call<Void> addToFavorites(@Path("recipeId") Long recipeId);

    @DELETE("recipes/{recipeId}/favorite")
    Call<Void> removeFromFavorites(@Path("recipeId") Long recipeId);

    @GET("recipes/favorites")
    Call<List<RecipeDTO>> getFavorites();
}
