package com.example.cooking.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cooking.data.SharedPreferencesManager;
import com.example.cooking.data.repository.AuthRepository;
import com.example.cooking.model.AuthRequest;
import com.example.cooking.model.AuthResponse;
import com.example.cooking.utils.Resource;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<AuthResponse>> authState = new MutableLiveData<>();
    private Call<AuthResponse> currentCall;

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String email, String password) {
        // Отменяем предыдущий запрос
        cancelCurrentCall();

        authState.setValue(Resource.loading(null));

        AuthRequest request = new AuthRequest(email, password, null);

        currentCall = authRepository.login(request);
        currentCall.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // СОХРАНЯЕМ ДАННЫЕ ЧЕРЕЗ ВАШ SharedPreferencesManager
                    SharedPreferencesManager.saveToken(authResponse.getToken());
                    SharedPreferencesManager.saveUserData(authResponse);

                    authState.setValue(Resource.success(authResponse));
                } else {
                    String errorMsg = "Ошибка авторизации";
                    try {
                        if (response.errorBody() != null) {
                            // Пытаемся получить сообщение об ошибке от сервера
                            errorMsg = response.errorBody().string();
                        } else if (response.code() == 401) {
                            errorMsg = "Неверный email или пароль";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    authState.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    authState.setValue(Resource.error(
                            "Ошибка сети: " + (t.getMessage() != null ? t.getMessage() : "проверьте подключение"),
                            null
                    ));
                }
            }
        });
    }

    public void register(String email, String password, String fullName) {
        cancelCurrentCall();
        authState.setValue(Resource.loading(null));

        AuthRequest request = new AuthRequest(email, password, fullName);

        currentCall = authRepository.register(request);
        currentCall.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Сохраняем данные
                    SharedPreferencesManager.saveToken(authResponse.getToken());
                    SharedPreferencesManager.saveUserData(authResponse);

                    authState.setValue(Resource.success(authResponse));
                } else {
                    String errorMsg = "Ошибка регистрации";
                    if (response.code() == 400) {
                        errorMsg = "Email уже используется";
                    }
                    authState.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    authState.setValue(Resource.error("Ошибка сети: " + t.getMessage(), null));
                }
            }
        });
    }

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
            currentCall = null;
        }
    }

    public LiveData<Resource<AuthResponse>> getAuthState() {
        return authState;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelCurrentCall();
    }
}
