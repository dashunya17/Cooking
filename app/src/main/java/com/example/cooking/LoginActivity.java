package com.example.cooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cooking.auth.AuthViewModel;
import com.example.cooking.auth.AuthViewModelFactory;
import com.example.cooking.data.SharedPreferencesManager;
import com.example.cooking.utils.Resource;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonAuth, buttonReg;
    private ProgressBar progressBar;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SharedPreferencesManager.isLoggedIn()) {
            Log.d("LoginActivity", "User already logged in. isAdmin: " + SharedPreferencesManager.isAdmin());
            if (SharedPreferencesManager.isAdmin()) {
                startAdminActivity();
            } else {
                startProductsActivity();
            }
            return;
        }

        initViews();
        setupViewModel();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonAuth = findViewById(R.id.buttonAuth);
        buttonReg = findViewById(R.id.buttonReg);


        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupViewModel() {
        AuthViewModelFactory factory = new AuthViewModelFactory(this);
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }

    private void setupObservers() {
        viewModel.getAuthState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else if (resource.status == Resource.Status.SUCCESS) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Вход выполнен!", Toast.LENGTH_SHORT).show();

                if (SharedPreferencesManager.isAdmin()) {
                    startAdminActivity();
                } else {
                    startProductsActivity();
                }
            } else if (resource.status == Resource.Status.ERROR) {
                showLoading(false);
                String errorMessage = resource.message != null ? resource.message : "Ошибка авторизации";
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        buttonAuth.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            Log.d("LoginActivity", "Login attempt. Email: " + email);

            if (validateInput(email, password)) {
                viewModel.login(email, password);
            }
        });

        buttonReg.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Введите email");
            editTextEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Введите корректный email");
            editTextEmail.requestFocus();
            isValid = false;
        } else {
            editTextEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Введите пароль");
            editTextPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Пароль должен быть не менее 6 символов");
            editTextPassword.requestFocus();
            isValid = false;
        } else {
            editTextPassword.setError(null);
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        buttonAuth.setEnabled(!show);
        buttonReg.setEnabled(!show);

        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        if (show) {
            buttonAuth.setText("Вход...");
        } else {
            buttonAuth.setText("Вход");
        }
    }

    private void startProductsActivity() {
        Intent intent = new Intent(this, UserMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;
    }
}
