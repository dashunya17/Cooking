package com.example.cooking.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.R;
import com.example.cooking.RecipeDetailActivity;
import com.example.cooking.Adapter.RecipeAdapter;
import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.model.ProductDTO;
import com.example.cooking.model.RecipeDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private TextInputEditText etSearchQuery;
    private MaterialButton btnFilters, btnSearch;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecipeAdapter adapter;
    private List<RecipeDTO> recipeList = new ArrayList<>();

    private ApiService apiService;

    private List<ProductDTO> userProducts = new ArrayList<>();
    private Set<Long> selectedProductIds = new HashSet<>();
    private int minIngredients = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initViews(view);
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        loadUserProducts();

        btnSearch.setOnClickListener(v -> performSearch());
        btnFilters.setOnClickListener(v -> showFilterDialog());

        return view;
    }

    private void initViews(View view) {
        etSearchQuery = view.findViewById(R.id.etSearchQuery);
        btnFilters = view.findViewById(R.id.btnFilters);
        btnSearch = view.findViewById(R.id.btnSearch);
        rvResults = view.findViewById(R.id.rvSearchResults);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(recipeList, new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecipeDTO recipe) {
                startActivity(RecipeDetailActivity.newIntent(requireContext(), recipe.getId()));
            }

            @Override
            public void onDeleteClick(RecipeDTO recipe) {
            }

            @Override
            public void onFavoriteClick(RecipeDTO recipe, int position) {
                toggleFavorite(recipe, position);
            }
        });
        rvResults.setAdapter(adapter);
    }

    private void loadUserProducts() {
        apiService.getUserProducts().enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDTO>> call,
                                   @NonNull Response<List<ProductDTO>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    userProducts.clear();
                    userProducts.addAll(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDTO>> call, @NonNull Throwable t) {

            }
        });
    }

    private void showFilterDialog() {
        if (userProducts.isEmpty()) {
            Toast.makeText(getContext(), "У вас нет добавленных продуктов", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Фильтры");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);


        TextView tvIngredients = new TextView(getContext());
        tvIngredients.setText("Выберите продукты:");
        tvIngredients.setTextSize(16);
        tvIngredients.setPadding(0, 0, 0, 16);
        layout.addView(tvIngredients);


        for (ProductDTO product : userProducts) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(product.getName());
            checkBox.setTag(product.getId());
            checkBox.setChecked(selectedProductIds.contains(product.getId()));
            layout.addView(checkBox);
        }


        TextView tvMinLabel = new TextView(getContext());
        tvMinLabel.setText("Минимум совпадений:");
        tvMinLabel.setPadding(0, 24, 0, 8);
        layout.addView(tvMinLabel);

        EditText etMinIngredients = new EditText(getContext());
        etMinIngredients.setHint("1");
        etMinIngredients.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etMinIngredients.setText(String.valueOf(minIngredients));
        layout.addView(etMinIngredients);

        builder.setView(layout);

        builder.setPositiveButton("Применить", (dialog, which) -> {

            selectedProductIds.clear();
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (cb.isChecked()) {
                        selectedProductIds.add((Long) cb.getTag());
                    }
                }
            }

            try {
                minIngredients = Integer.parseInt(etMinIngredients.getText().toString().trim());
                if (minIngredients < 0) minIngredients = 0;
            } catch (NumberFormatException e) {
                minIngredients = 1;
            }

            updateFilterButtonText();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void updateFilterButtonText() {
        if (selectedProductIds.isEmpty()) {
            btnFilters.setText("Фильтры");
        } else {
            btnFilters.setText("Фильтры (" + selectedProductIds.size() + ")");
        }
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();

        if (query.isEmpty() && selectedProductIds.isEmpty()) {
            Toast.makeText(getContext(), "Введите запрос или выберите фильтры", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        Call<List<RecipeDTO>> call = apiService.searchRecipes(
                query.isEmpty() ? null : query,
                selectedProductIds.isEmpty() ? null : new ArrayList<>(selectedProductIds),
                minIngredients
        );

        call.enqueue(new Callback<List<RecipeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RecipeDTO>> call,
                                   @NonNull Response<List<RecipeDTO>> response) {
                if (!isAdded()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    recipeList.clear();
                    recipeList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (recipeList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvResults.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Ошибка поиска", Toast.LENGTH_SHORT).show();
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

    private void toggleFavorite(RecipeDTO recipe, int position) {
        if (recipe.getFavorite() != null && recipe.getFavorite()) {
            apiService.removeFromFavorites(recipe.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (isAdded() && response.isSuccessful()) {
                        recipe.setFavorite(false);
                        adapter.notifyItemChanged(position);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.addToFavorites(recipe.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (isAdded() && response.isSuccessful()) {
                        recipe.setFavorite(true);
                        adapter.notifyItemChanged(position);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvResults.setVisibility(show ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }
}