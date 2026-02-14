package com.example.cooking;
import com.example.cooking.data.SharedPreferencesManager;
import android.app.Application;

public class App  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesManager.init(this);
    }
}
