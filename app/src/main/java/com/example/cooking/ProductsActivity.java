package com.example.cooking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.data.SharedPreferencesManager;
import com.example.cooking.Adapter.ProductAdapter;
import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.model.ProductDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsActivity extends AppCompatActivity
        implements ProductAdapter.OnItemClickListener {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<ProductDTO> productList = new ArrayList<>();
    private FloatingActionButton fabAddProduct;
    private ApiService apiService;
    private ImageButton btnExitToLogin;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        if (!SharedPreferencesManager.isInitialized()) {
            SharedPreferencesManager.init(getApplicationContext());
        }

        btnExitToLogin = findViewById(R.id.ExitToLogin);
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        setupClickListeners();

        loadUserProducts();
    }

    private void setupClickListeners() {
        btnExitToLogin.setOnClickListener(v -> showLogoutConfirmationDialog());
        fabAddProduct.setOnClickListener(v -> showAddProductDialog());
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList, this, true);
        rvProducts.setAdapter(adapter);
    }

    private boolean checkAuth() {
        String token = SharedPreferencesManager.getToken();
        Log.d("PRODUCTS", "Проверка токена: " + (token != null ? "есть" : "нет"));

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }
        return true;
    }

    private void loadUserProducts() {
        if (!checkAuth()) return;

        Log.d("PRODUCTS", "=== ЗАГРУЗКА ПРОДУКТОВ ПОЛЬЗОВАТЕЛЯ ===");

        apiService.getUserProducts().enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                Log.d("PRODUCTS", "Код ответа при загрузке: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.updateList(productList);
                    Log.d("PRODUCTS", "Загружено продуктов: " + productList.size());

                    if (productList.isEmpty()) {
                        Toast.makeText(ProductsActivity.this,
                                "У вас пока нет продуктов. Нажмите + чтобы добавить",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e("PRODUCTS", "Ошибка сети при загрузке: " + t.getMessage());
                Toast.makeText(ProductsActivity.this,
                        "Ошибка сети. Проверьте подключение к интернету",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(int code) {
        String message;
        switch (code) {
            case 401:
                message = "Сессия истекла. Войдите снова";
                SharedPreferencesManager.clear();
                startActivity(new Intent(ProductsActivity.this, LoginActivity.class));
                finish();
                break;
            case 500:
                message = "Ошибка сервера. Попробуйте позже";
                break;
            default:
                message = "Ошибка загрузки: " + code;
        }
        Toast.makeText(ProductsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showAddProductDialog() {
        if (!checkAuth()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить продукт");

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);
        autoCompleteTextView.setHint("Начните вводить название...");
        autoCompleteTextView.setThreshold(1);

        loadProductCatalog(autoCompleteTextView);
        builder.setView(autoCompleteTextView);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String productName = autoCompleteTextView.getText().toString().trim();
            if (!productName.isEmpty()) {
                findAndAddProduct(productName);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void loadProductCatalog(AutoCompleteTextView autoComplete) {
        apiService.getProductCatalog(null, "").enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> productNames = new ArrayList<>();
                    for (ProductDTO product : response.body()) {
                        productNames.add(product.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProductsActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            productNames
                    );
                    autoComplete.setAdapter(adapter);
                    autoComplete.showDropDown();

                    Log.d("PRODUCTS", "Загружено продуктов в каталог: " + productNames.size());
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e("PRODUCTS", "Ошибка загрузки каталога: " + t.getMessage());
            }
        });
    }

    private void findAndAddProduct(String productName) {
        Log.d("PRODUCTS", "Поиск продукта: " + productName);

        apiService.getProductCatalog(null, productName).enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ProductDTO selectedProduct = response.body().get(0);

                    boolean exists = false;
                    for (ProductDTO p : productList) {
                        if (p.getId().equals(selectedProduct.getId())) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        Toast.makeText(ProductsActivity.this,
                                "Продукт уже есть в вашем списке",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        addProductToUser(selectedProduct);
                    }
                } else {
                    Toast.makeText(ProductsActivity.this,
                            "Продукт не найден в каталоге",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Toast.makeText(ProductsActivity.this,
                        "Ошибка поиска продукта", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductToUser(ProductDTO product) {
        Log.d("PRODUCTS", "=== ДОБАВЛЕНИЕ ПРОДУКТА ===");
        Log.d("PRODUCTS", "ID: " + product.getId() + ", Название: " + product.getName());

        ProductDTO requestProduct = new ProductDTO();
        requestProduct.setId(product.getId());
        requestProduct.setName(product.getName());
        requestProduct.setCategory(product.getCategory());
        requestProduct.setIsCommon(product.getIsCommon());

        apiService.addUserProduct(requestProduct).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("PRODUCTS", "Код ответа при добавлении: " + response.code());

                if (response.isSuccessful()) {
                    Log.d("PRODUCTS", " Продукт успешно добавлен на сервер");
                    Toast.makeText(ProductsActivity.this,
                            "Продукт добавлен", Toast.LENGTH_SHORT).show();

                    productList.add(product);
                    adapter.notifyItemInserted(productList.size() - 1);


                } else {
                    Log.e("PRODUCTS", "Ошибка добавления. Code: " + response.code());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("PRODUCTS", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String errorMsg = "Ошибка добавления: " + response.code();
                    if (response.code() == 409) {
                        errorMsg = "Продукт уже есть в списке";
                    } else if (response.code() == 400) {
                        errorMsg = "Неверные данные продукта";
                    } else if (response.code() == 401) {
                        errorMsg = "Требуется авторизация";
                        SharedPreferencesManager.clear();
                        startActivity(new Intent(ProductsActivity.this, LoginActivity.class));
                        finish();
                    }
                    Toast.makeText(ProductsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("PRODUCTS", "Ошибка сети при добавлении: " + t.getMessage());
                Toast.makeText(ProductsActivity.this,
                        "Ошибка сети. Проверьте подключение",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(int position, ProductDTO product) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить продукт?")
                .setMessage("Удалить " + product.getName() + " из вашего списка?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    removeProductFromUser(product.getId(), position);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void removeProductFromUser(Long productId, int position) {
        Log.d("PRODUCTS", "Удаление продукта ID: " + productId);

        apiService.removeUserProduct(productId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("PRODUCTS", "Код ответа при удалении: " + response.code());

                if (response.isSuccessful()) {
                    productList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(ProductsActivity.this,
                            "Продукт удален", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Ошибка удаления: " + response.code();
                    if (response.code() == 404) {
                        productList.remove(position);
                        adapter.notifyItemRemoved(position);
                        errorMsg = "Продукт удален из списка";
                    }
                    Toast.makeText(ProductsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("PRODUCTS", "Ошибка сети при удалении: " + t.getMessage());
                Toast.makeText(ProductsActivity.this,
                        "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position, ProductDTO product) {

    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти", (dialog, which) -> logout())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void logout() {
        SharedPreferencesManager.clear();
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}