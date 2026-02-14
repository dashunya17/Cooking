package com.example.cooking.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.R;
import com.example.cooking.model.RecipeDTO;

import java.util.List;

public class UserRecipeAdapter extends RecyclerView.Adapter<UserRecipeAdapter.RecipeViewHolder> {

    private List<RecipeDTO> recipes;
    private OnUserRecipeClickListener listener;

    public interface OnUserRecipeClickListener {
        void onItemClick(RecipeDTO recipe);
        void onFavoriteClick(RecipeDTO recipe, int position);
    }

    public UserRecipeAdapter(List<RecipeDTO> recipes, OnUserRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_user, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeDTO recipe = recipes.get(position);

        holder.tvTitle.setText(recipe.getTitle() != null ? recipe.getTitle() : "");
        holder.tvCategory.setText(recipe.getCategory() != null ? recipe.getCategory() : "");

        String timeText = (recipe.getCookingTimeMinutes() != null ?
                recipe.getCookingTimeMinutes() + " мин." : "");
        holder.tvCookingTime.setText(timeText);

        holder.tvMatchPercentage.setVisibility(View.GONE);

        updateFavoriteIcon(holder.btnFavorite, recipe);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(recipe);
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(recipe, position);
            }
        });
    }
    private void updateFavoriteIcon(ImageButton btnFavorite, RecipeDTO recipe) {
        if (recipe.getFavorite() != null && recipe.getFavorite()) {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
    public void updateItemFavoriteState(int position, boolean isFavorite) {
        if (position >= 0 && position < recipes.size()) {
            recipes.get(position).setFavorite(isFavorite);
            notifyItemChanged(position);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < recipes.size()) {
            recipes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, recipes.size());
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateList(List<RecipeDTO> newList) {
        this.recipes = newList;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvCookingTime, tvMatchPercentage;
        ImageButton btnFavorite;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvCategory = itemView.findViewById(R.id.tvRecipeCategory);
            tvCookingTime = itemView.findViewById(R.id.tvCookingTime);
            tvMatchPercentage = itemView.findViewById(R.id.tvMatchPercentage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}