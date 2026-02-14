package com.example.cooking.auth;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.cooking.data.ApiService;
import com.example.cooking.data.RetrofitClient;
import com.example.cooking.data.repository.AuthRepository;

import org.jspecify.annotations.NonNull;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public AuthViewModelFactory(Context context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            ApiService apiService = RetrofitClient.getApiService();
            AuthRepository repository = new AuthRepository(apiService);
            return (T) new AuthViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}