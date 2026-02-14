package com.example.cooking.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.Adapter.ProductAdapter;
import com.example.cooking.LoginActivity;
import com.example.cooking.R;
import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.model.ProductDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;

public class AdminProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<ProductDTO> productList = new ArrayList<>();
    private FloatingActionButton fabAddProduct;
    private ApiService apiService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        rvProducts = view.findViewById(R.id.rvProducts);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        loadProducts();

        fabAddProduct.setOnClickListener(v -> showAddProductDialog());


        return view;
    }
    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(productList, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position, ProductDTO product) {
                showDeleteDialog(position, product);
            }

            @Override
            public void onItemClick(int position, ProductDTO product) {
                showEditProductDialog(product);
            }
        }, true);
        rvProducts.setAdapter(adapter);
    }
    private void loadProducts() {
        Call<List<ProductDTO>> call = apiService.getAllProductsForAdmin();
        call.enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminProducts", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить продукт");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_product, null);

        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        Spinner spIsCommon = dialogView.findViewById(R.id.spIsCommon);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.boolean_choices,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIsCommon.setAdapter(spinnerAdapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            boolean isCommon = spIsCommon.getSelectedItemPosition() == 0;

            if (name.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            ProductDTO product = new ProductDTO();
            product.setName(name);
            product.setCategory(category);
            product.setIsCommon(isCommon);

            addProduct(product);
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
    private void addProduct(ProductDTO product) {
        apiService.createProduct(product).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Продукт добавлен", Toast.LENGTH_SHORT).show();
                    loadProducts(); // обновляем список
                } else {
                    String error = "Ошибка добавления: " + response.code();
                    if (response.code() == 403) {
                        error = "Нет прав администратора";
                    } else if (response.code() == 401) {
                        error = "Требуется авторизация";
                    }
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showEditProductDialog(ProductDTO product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Редактировать продукт");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_product, null);

        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        Spinner spIsCommon = dialogView.findViewById(R.id.spIsCommon);

        etName.setText(product.getName());
        etCategory.setText(product.getCategory());

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.boolean_choices,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIsCommon.setAdapter(spinnerAdapter);
        spIsCommon.setSelection(product.getIsCommon() != null && product.getIsCommon() ? 0 : 1);

        builder.setView(dialogView);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            product.setName(etName.getText().toString().trim());
            product.setCategory(etCategory.getText().toString().trim());
            product.setIsCommon(spIsCommon.getSelectedItemPosition() == 0);

            updateProduct(product);
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void updateProduct(ProductDTO product) {
        apiService.updateProduct(product.getId(), product).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Продукт обновлён", Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(getContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showDeleteDialog(int position, ProductDTO product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление продукта")
                .setMessage("Вы уверены, что хотите удалить продукт \"" +
                        product.getName() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteProduct(position, product.getId());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteProduct(int position, Long productId) {
        apiService.deleteProduct(productId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Продукт удалён", Toast.LENGTH_SHORT).show();
                    productList.remove(position);
                    adapter.notifyItemRemoved(position);
                } else {
                    Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}