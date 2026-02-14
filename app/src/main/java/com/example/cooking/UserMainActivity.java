package com.example.cooking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cooking.fragments.FavoritesFragment;
import com.example.cooking.fragments.RecommendedFragment;
import com.example.cooking.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);


        setupBottomNavigationColors(bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();



             if (id == R.id.nav_recommended) {
                selectedFragment = new RecommendedFragment();
            } else if (id == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (id == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, ProductsActivity.class));
                return true;
            }



            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_recommended);
        }
    }

    private void setupBottomNavigationColors(BottomNavigationView bottomNav) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };

        int[] colors = new int[] {
                ContextCompat.getColor(this, R.color.black),  // ВЫБРАННЫЙ: черный
                ContextCompat.getColor(this, R.color.dark_gray)  // НЕВЫБРАННЫЙ: темно-серый
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        bottomNav.setItemIconTintList(colorStateList);
        bottomNav.setItemTextColor(colorStateList);


        bottomNav.setBackgroundColor(ContextCompat.getColor(this, R.color.fon));
    }

}