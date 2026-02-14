package com.example.cooking.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.Adapter.IngredientAdapter;
import com.example.cooking.Adapter.RecipeAdapter;
import com.example.cooking.R;
import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.model.IngredientDTO;
import com.example.cooking.model.ProductDTO;
import com.example.cooking.model.RecipeDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRecipesFragment extends Fragment {

    private RecyclerView rvRecipes;
    private RecipeAdapter recipeAdapter;
    private List<RecipeDTO> recipeList = new ArrayList<>();

    private FloatingActionButton fabAddRecipe;
    private ApiService apiService;


    private ArrayList<IngredientDTO> selectedIngredients;
    private IngredientAdapter ingredientAdapter;
    private RecyclerView rvIngredients;
    private Button btnAddIngredient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_recipes, container, false);

        rvRecipes = view.findViewById(R.id.rvRecipes);
        fabAddRecipe = view.findViewById(R.id.fabAddRecipe);
        apiService = RetrofitClient.getApiService();
        selectedIngredients = new ArrayList<>();

        setupRecyclerView();
        setupSwipeToDelete();
        loadRecipes();

        fabAddRecipe.setOnClickListener(v -> showAddRecipeDialog());

        return view;
    }

    private void setupRecyclerView() {
        rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));


        recipeAdapter = new RecipeAdapter(recipeList, new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecipeDTO recipe) {
                showRecipeDetails(recipe);
            }

            @Override
            public void onDeleteClick(RecipeDTO recipe) {
                showDeleteDialog(recipe);
            }

            @Override
            public void onFavoriteClick(RecipeDTO recipe, int position) {

            }
        });

        rvRecipes.setAdapter(recipeAdapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    RecipeDTO recipe = recipeList.get(position);
                    showDeleteDialog(recipe, position);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvRecipes);
    }

    private void loadRecipes() {
        apiService.getAllRecipesForAdmin().enqueue(new Callback<List<RecipeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RecipeDTO>> call,
                                   @NonNull Response<List<RecipeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipeList.clear();
                    recipeList.addAll(response.body());
                    recipeAdapter.notifyDataSetChanged();
                    Log.d("AdminRecipes", "Загружено рецептов: " + recipeList.size());
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("AdminRecipes", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RecipeDTO>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("AdminRecipes", "Network error: " + t.getMessage());
            }
        });
    }


    private void deleteRecipe(Long recipeId, int position) {
        apiService.deleteRecipe(recipeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {

                    recipeList.remove(position);
                    recipeAdapter.notifyItemRemoved(position);

                    Toast.makeText(getContext(), "Рецепт удалён", Toast.LENGTH_SHORT).show();


                    Snackbar.make(rvRecipes, "Рецепт удален", Snackbar.LENGTH_LONG)
                            .setAction("ОТМЕНА", v -> {
                                loadRecipes();
                            })
                            .show();
                } else {
                    String errorMsg = "Ошибка удаления: " + response.code();
                    if (response.code() == 403) {
                        errorMsg = "Нет прав администратора";
                    } else if (response.code() == 401) {
                        errorMsg = "Требуется авторизация";
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    recipeAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                recipeAdapter.notifyItemChanged(position);
            }
        });
    }

    private void showDeleteDialog(RecipeDTO recipe) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление рецепта")
                .setMessage("Вы уверены, что хотите удалить рецепт \"" + recipe.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    int position = recipeList.indexOf(recipe);
                    if (position != -1) {
                        deleteRecipe(recipe.getId(), position);
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    recipeAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private void showDeleteDialog(RecipeDTO recipe, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление рецепта")
                .setMessage("Вы уверены, что хотите удалить рецепт \"" + recipe.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteRecipe(recipe.getId(), position);
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    recipeAdapter.notifyItemChanged(position);
                })
                .show();
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить рецепт");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_recipe, null);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etCookingSteps = dialogView.findViewById(R.id.etCookingSteps);
        EditText etCookingTime = dialogView.findViewById(R.id.etCookingTime);
        Spinner spDifficulty = dialogView.findViewById(R.id.spDifficulty);
        EditText etServings = dialogView.findViewById(R.id.etServings);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);

        rvIngredients = dialogView.findViewById(R.id.rvIngredients);
        btnAddIngredient = dialogView.findViewById(R.id.btnAddIngredient);

        selectedIngredients = new ArrayList<>();
        ingredientAdapter = new IngredientAdapter(selectedIngredients, position -> {
            selectedIngredients.remove(position);
            ingredientAdapter.notifyItemRemoved(position);
        });

        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIngredients.setAdapter(ingredientAdapter);

        btnAddIngredient.setOnClickListener(v -> showProductSelectionDialog());

        builder.setView(dialogView);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            if (etTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                RecipeDTO recipe = new RecipeDTO();
                recipe.setTitle(etTitle.getText().toString().trim());
                recipe.setDescription(etDescription.getText().toString().trim());
                recipe.setCookingSteps(etCookingSteps.getText().toString().trim());
                recipe.setCookingTimeMinutes(Integer.parseInt(etCookingTime.getText().toString().trim()));
                recipe.setDifficulty(spDifficulty.getSelectedItem().toString());
                recipe.setServings(Integer.parseInt(etServings.getText().toString().trim()));
                recipe.setCategory(etCategory.getText().toString().trim());
                recipe.setIngredients(new ArrayList<>(selectedIngredients));

                createRecipe(recipe);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Неверный формат числа", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showProductSelectionDialog() {
        apiService.getProductCatalog(null, "").enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDTO>> call,
                                   @NonNull Response<List<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductDTO> products = response.body();
                    if (products.isEmpty()) {
                        Toast.makeText(getContext(), "Нет доступных продуктов", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] productNames = new String[products.size()];
                    for (int i = 0; i < products.size(); i++) {
                        productNames[i] = products.get(i).getName();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Выберите продукт")
                            .setItems(productNames, (dialog, which) -> {
                                ProductDTO selectedProduct = products.get(which);
                                showQuantityDialog(selectedProduct);
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки продуктов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDTO>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuantityDialog(ProductDTO product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить ингредиент: " + product.getName());

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_ingredient_quantity, null);

        EditText etQuantity = view.findViewById(R.id.etQuantity);
        Spinner spUnit = view.findViewById(R.id.spUnit);

        builder.setView(view);
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String quantityStr = etQuantity.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                Toast.makeText(getContext(), "Введите количество", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantity = Double.parseDouble(quantityStr);
                String unit = spUnit.getSelectedItem().toString();

                IngredientDTO ingredient = new IngredientDTO();
                ingredient.setProductId(product.getId());
                ingredient.setProductName(product.getName());
                ingredient.setQuantity(quantity);
                ingredient.setUnit(unit);

                selectedIngredients.add(ingredient);
                ingredientAdapter.notifyItemInserted(selectedIngredients.size() - 1);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Неверное число", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void createRecipe(RecipeDTO recipe) {
        apiService.createRecipe(recipe).enqueue(new Callback<RecipeDTO>() {
            @Override
            public void onResponse(@NonNull Call<RecipeDTO> call,
                                   @NonNull Response<RecipeDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Рецепт добавлен", Toast.LENGTH_SHORT).show();
                    loadRecipes();
                } else {
                    String errorMsg = "Ошибка добавления: " + response.code();
                    if (response.code() == 403) {
                        errorMsg = "Нет прав администратора";
                    } else if (response.code() == 401) {
                        errorMsg = "Требуется авторизация";
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RecipeDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRecipeDetails(RecipeDTO recipe) {
        StringBuilder ingredients = new StringBuilder();
        if (recipe.getIngredients() != null) {
            for (IngredientDTO ingredient : recipe.getIngredients()) {
                ingredients.append("• ")
                        .append(ingredient.getProductName())
                        .append(": ")
                        .append(ingredient.getQuantity())
                        .append(" ")
                        .append(ingredient.getUnit())
                        .append("\n");
            }
        }

        String message = "Описание: " + recipe.getDescription() +
                "\n\nИнгредиенты:\n" + ingredients +
                "\nВремя: " + recipe.getCookingTimeMinutes() + " мин." +
                "\nПорций: " + recipe.getServings() +
                "\nСложность: " + recipe.getDifficulty() +
                "\nКатегория: " + recipe.getCategory() +
                "\n\nШаги приготовления:\n" + recipe.getCookingSteps();

    }

}