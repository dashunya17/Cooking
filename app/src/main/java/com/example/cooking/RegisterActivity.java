package com.example.cooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cooking.auth.AuthViewModel;
import com.example.cooking.auth.AuthViewModelFactory;
import com.example.cooking.utils.Resource;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmailAddress, editTextPassword;
    private Button buttonReg, buttonExit;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupViewModel();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonReg = findViewById(R.id.buttonReg);
        buttonExit = findViewById(R.id.buttonExit);
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
                Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                startProductsActivity();
            } else if (resource.status == Resource.Status.ERROR) {
                showLoading(false);
                String errorMessage = resource.message != null ? resource.message : "Ошибка регистрации";
                if (errorMessage.contains("Email уже используется")) {
                    editTextEmailAddress.setError("Этот email уже занят");
                    editTextEmailAddress.requestFocus();
                } else {
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupClickListeners() {
        buttonReg.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmailAddress.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            if (validateInput(name, email, password)) {
                viewModel.register(email, password, name);
            }
        });
        buttonExit.setOnClickListener(v -> {
            finish();
        });
    }

    private boolean validateInput(String name, String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Введите имя");
            editTextName.requestFocus();
            isValid = false;
        } else if (name.length() < 2) {
            editTextName.setError("Имя должно содержать минимум 2 символа");
            editTextName.requestFocus();
            isValid = false;
        } else {
            editTextName.setError(null);
        }


        if (TextUtils.isEmpty(email)) {
            editTextEmailAddress.setError("Введите email");
            editTextEmailAddress.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailAddress.setError("Введите корректный email");
            editTextEmailAddress.requestFocus();
            isValid = false;
        } else {
            editTextEmailAddress.setError(null);
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
        buttonReg.setEnabled(!show);
        buttonExit.setEnabled(!show);

        if (show) {
            buttonReg.setText("Регистрация...");
        } else {
            buttonReg.setText("Зарегистрироваться");
        }
    }

    private void startProductsActivity() {
        Intent intent = new Intent(this, UserMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}