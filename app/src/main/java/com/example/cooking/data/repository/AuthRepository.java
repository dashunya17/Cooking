package com.example.cooking.data.repository;

import com.example.cooking.data.ApiService;
import com.example.cooking.model.AuthRequest;
import com.example.cooking.model.AuthResponse;

import java.util.Map;

import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<AuthResponse> login(AuthRequest request) {
        return apiService.login(request);
    }

    public Call<AuthResponse> register(AuthRequest request) {
        return apiService.register(request);
    }

    public Call<Map<String, Boolean>> checkEmail(String email) {
        return apiService.checkEmail(email);
    }
}
