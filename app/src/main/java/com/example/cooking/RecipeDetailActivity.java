package com.example.cooking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.Adapter.IngredientAdapter;
import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.model.IngredientDTO;
import com.example.cooking.model.RecipeDTO;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvCookingTime, tvServings, tvDifficulty, tvDescription, tvCookingSteps;
    private RecyclerView rvIngredients;
    private MaterialButton btnFavorite;
    private ApiService apiService;
    private Long recipeId;
    private RecipeDTO currentRecipe;


    public static Intent newIntent(Context context, Long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeId = getIntent().getLongExtra("recipe_id", -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Ошибка загрузки рецепта", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        apiService = RetrofitClient.getApiService();
        loadRecipeDetails();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvCookingTime = findViewById(R.id.tvCookingTime);
        tvServings = findViewById(R.id.tvServings);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvDescription = findViewById(R.id.tvDescription);
        tvCookingSteps = findViewById(R.id.tvCookingSteps);
        rvIngredients = findViewById(R.id.rvIngredients);
        btnFavorite = findViewById(R.id.btnFavorite);

        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRecipeDetails() {
        apiService.getRecipeById(recipeId).enqueue(new Callback<RecipeDTO>() {
            @Override
            public void onResponse(@NonNull Call<RecipeDTO> call, @NonNull Response<RecipeDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRecipe = response.body();
                    displayRecipe(currentRecipe);
                } else {
                    Toast.makeText(RecipeDetailActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RecipeDTO> call, @NonNull Throwable t) {
                Toast.makeText(RecipeDetailActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipe(RecipeDTO recipe) {

        tvTitle.setText(recipe.getTitle());
        tvCategory.setText(recipe.getCategory());
        tvDescription.setText(recipe.getDescription());
        tvCookingSteps.setText(recipe.getCookingSteps());


        if (recipe.getCookingTimeMinutes() != null) {
            tvCookingTime.setText(recipe.getCookingTimeMinutes() + " мин");
        }


        if (recipe.getServings() != null) {
            tvServings.setText(String.valueOf(recipe.getServings()));
        }


        tvDifficulty.setText(recipe.getDifficulty());


        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            IngredientAdapter ingredientAdapter = new IngredientAdapter(recipe.getIngredients(), null);
            rvIngredients.setAdapter(ingredientAdapter);
        }


        updateFavoriteButton(recipe.getFavorite() != null && recipe.getFavorite());


        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            btnFavorite.setText("В избранном");
            btnFavorite.setIconResource(android.R.drawable.btn_star_big_on);
        } else {
            btnFavorite.setText("В избранное");
            btnFavorite.setIconResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void toggleFavorite() {
        if (currentRecipe == null) return;

        boolean isFavorite = currentRecipe.getFavorite() != null && currentRecipe.getFavorite();

        // Блокируем кнопку
        btnFavorite.setEnabled(false);

        if (isFavorite) {
            // Удалить из избранного
            apiService.removeFromFavorites(recipeId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    btnFavorite.setEnabled(true);
                    if (response.isSuccessful()) {
                        currentRecipe.setFavorite(false);
                        updateFavoriteButton(false);
                        Toast.makeText(RecipeDetailActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RecipeDetailActivity.this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    btnFavorite.setEnabled(true);
                    Toast.makeText(RecipeDetailActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Добавить в избранное
            apiService.addToFavorites(recipeId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    btnFavorite.setEnabled(true);
                    if (response.isSuccessful()) {
                        currentRecipe.setFavorite(true);
                        updateFavoriteButton(true);
                        Toast.makeText(RecipeDetailActivity.this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RecipeDetailActivity.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    btnFavorite.setEnabled(true);
                    Toast.makeText(RecipeDetailActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}