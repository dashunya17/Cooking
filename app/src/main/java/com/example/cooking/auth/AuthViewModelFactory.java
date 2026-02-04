package com.example.cooking.auth;


import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.data.repository.AuthRepository;

import org.jspecify.annotations.NonNull;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final AuthRepository authRepository;

    public AuthViewModelFactory() {
        ApiService apiService = RetrofitClient.getApiService();
        this.authRepository = new AuthRepository(apiService);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
