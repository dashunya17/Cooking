package com.example.cooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
        SharedPreferencesManager.initialize(getApplicationContext());
        /**Проверяем авторизован ли уже пользователь*/
        if (SharedPreferencesManager.isLoggedIn()) {
            startMainActivity();
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
        buttonAuth = findViewById(R.id.buttonExit);
        buttonReg = findViewById(R.id.buttonReg);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setVisibility(View.GONE);
    }

    private void setupViewModel() {
        AuthViewModelFactory factory = new AuthViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }

    private void setupObservers() {
        viewModel.getAuthState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else if (resource.status == Resource.Status.SUCCESS) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
                startMainActivity();
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

        if (show) {
            buttonAuth.setText("Вход...");
        } else {
            buttonAuth.setText("Вход");
        }
    }
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
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