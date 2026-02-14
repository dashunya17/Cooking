package com.example.cooking.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cooking.data.SharedPreferencesManager;
import com.example.cooking.data.repository.AuthRepository;
import com.example.cooking.model.AuthRequest;
import com.example.cooking.model.AuthResponse;
import com.example.cooking.utils.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<AuthResponse>> authState = new MutableLiveData<>();

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String email, String password) {
        authState.setValue(Resource.loading(null));

        AuthRequest request = new AuthRequest(email, password);
        Call<AuthResponse> call = authRepository.login(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    SharedPreferencesManager.setToken(authResponse.getToken());
                    SharedPreferencesManager.setTokenType(authResponse.getType()); // обязательно!
                    SharedPreferencesManager.setUserId(authResponse.getUserId());
                    SharedPreferencesManager.setUserEmail(authResponse.getEmail());
                    SharedPreferencesManager.setUserName(authResponse.getFullName());
                    SharedPreferencesManager.setAdmin("ADMIN".equalsIgnoreCase(authResponse.getRole()));
                    SharedPreferencesManager.setLoggedIn(true);

                    authState.setValue(Resource.success(authResponse));
                } else {
                    authState.setValue(Resource.error("Неверный email или пароль", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                authState.setValue(Resource.error("Ошибка сети: " + t.getMessage(), null));
            }
        });
    }

    public void register(String email, String password, String name) {
        authState.setValue(Resource.loading(null));

        AuthRequest request = new AuthRequest(email, password, name);
        Call<AuthResponse> call = authRepository.register(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    SharedPreferencesManager.setToken(authResponse.getToken());
                    SharedPreferencesManager.setTokenType(authResponse.getType());
                    SharedPreferencesManager.setUserId(authResponse.getUserId());
                    SharedPreferencesManager.setUserEmail(authResponse.getEmail());
                    SharedPreferencesManager.setUserName(authResponse.getFullName());
                    SharedPreferencesManager.setAdmin("ADMIN".equalsIgnoreCase(authResponse.getRole()));
                    SharedPreferencesManager.setLoggedIn(true);

                    authState.setValue(Resource.success(authResponse));
                } else {
                    String errorMessage = "Ошибка регистрации";
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("Email already exists")) {
                                errorMessage = "Email уже используется";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    authState.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                authState.setValue(Resource.error("Ошибка сети: " + t.getMessage(), null));
            }
        });
    }

    public LiveData<Resource<AuthResponse>> getAuthState() {
        return authState;
    }
}