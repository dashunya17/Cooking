package com.example.cooking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private UserRecipeAdapter adapter;
    private List<RecipeDTO> recipeList = new ArrayList<>();
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        loadFavorites();

        return view;
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserRecipeAdapter(recipeList, new UserRecipeAdapter.OnUserRecipeClickListener() {
            @Override
            public void onItemClick(RecipeDTO recipe) {
                startActivity(RecipeDetailActivity.newIntent(requireContext(), recipe.getId()));
            }

            @Override
            public void onFavoriteClick(RecipeDTO recipe, int position) {
                removeFromFavorites(recipe, position);
            }
        });

        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        showLoading(true);
        apiService.getFavorites().enqueue(new Callback<List<RecipeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RecipeDTO>> call,
                                   @NonNull Response<List<RecipeDTO>> response) {
                if (!isAdded()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    recipeList.clear();
                    recipeList.addAll(response.body());

                    for (RecipeDTO recipe : recipeList) {
                        recipe.setFavorite(true);
                    }

                    adapter.updateList(recipeList);

                    if (recipeList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RecipeDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromFavorites(RecipeDTO recipe, int position) {

        setButtonsEnabled(false);

        apiService.removeFromFavorites(recipe.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                setButtonsEnabled(true);

                if (response.isSuccessful()) {

                    recipeList.remove(position);
                    adapter.removeItem(position);

                    Toast.makeText(getContext(), "Рецепт удалён из избранного", Toast.LENGTH_SHORT).show();


                    if (recipeList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show();

                    adapter.updateItemFavoriteState(position, true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setButtonsEnabled(true);
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                adapter.updateItemFavoriteState(position, true);
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvFavorites.setVisibility(show ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void setButtonsEnabled(boolean enabled) {

    }

    @Override
    public void onResume() {
        super.onResume();

        loadFavorites();
    }
}