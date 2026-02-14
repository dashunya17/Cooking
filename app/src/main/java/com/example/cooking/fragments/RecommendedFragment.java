package com.example.cooking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.Adapter.UserRecipeAdapter;
import com.example.cooking.R;
import com.example.cooking.RecipeDetailActivity;
import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.model.RecipeDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendedFragment extends Fragment {

    private RecyclerView rvRecipes;
    private UserRecipeAdapter adapter;
    private List<RecipeDTO> recipeList = new ArrayList<>();
    private ProgressBar progressBar;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommended, container, false);

        rvRecipes = view.findViewById(R.id.rvRecipes);
        progressBar = view.findViewById(R.id.progressBar);
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        loadRecommendedRecipes();

        return view;
    }

    private void setupRecyclerView() {
        rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserRecipeAdapter(recipeList, new UserRecipeAdapter.OnUserRecipeClickListener() {
            @Override
            public void onItemClick(RecipeDTO recipe) {
                startActivity(RecipeDetailActivity.newIntent(requireContext(), recipe.getId()));
            }

            @Override
            public void onFavoriteClick(RecipeDTO recipe, int position) {
                toggleFavorite(recipe, position);
            }
        });

        rvRecipes.setAdapter(adapter);
    }

    private void loadRecommendedRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getRecommendedRecipes(20).enqueue(new Callback<List<RecipeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RecipeDTO>> call,
                                   @NonNull Response<List<RecipeDTO>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    recipeList.clear();
                    recipeList.addAll(response.body());
                    adapter.updateList(recipeList);
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RecipeDTO>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorite(RecipeDTO recipe, int position) {



        if (recipe.getFavorite() != null && recipe.getFavorite()) {

            apiService.removeFromFavorites(recipe.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                    if (response.isSuccessful()) {

                        recipe.setFavorite(false);
                        adapter.updateItemFavoriteState(position, false);
                        Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();

                        adapter.updateItemFavoriteState(position, true);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                    adapter.updateItemFavoriteState(position, true);
                }
            });
        } else {

            apiService.addToFavorites(recipe.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {

                        recipe.setFavorite(true);
                        adapter.updateItemFavoriteState(position, true);
                        Toast.makeText(getContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                        adapter.updateItemFavoriteState(position, false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                    adapter.updateItemFavoriteState(position, false);
                }
            });
        }
    }
}