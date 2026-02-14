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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeDTO> recipes;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(RecipeDTO recipe);
        void onDeleteClick(RecipeDTO recipe);
        void onFavoriteClick(RecipeDTO recipe, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, RecipeDTO recipe);
    }

    public RecipeAdapter(List<RecipeDTO> recipes, OnItemClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_admin, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeDTO recipe = recipes.get(position);

        holder.tvTitle.setText(recipe.getTitle() != null ? recipe.getTitle() : "Без названия");
        holder.tvDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "Нет описания");

        String details = "";
        if (recipe.getCookingTimeMinutes() != null) {
            details += recipe.getCookingTimeMinutes() + " мин. ";
        }
        if (recipe.getServings() != null) {
            details += "| " + recipe.getServings() + " порц. ";
        }
        if (recipe.getDifficulty() != null && !recipe.getDifficulty().isEmpty()) {
            details += "| " + recipe.getDifficulty();
        }
        holder.tvDetails.setText(details);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(recipe);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position, recipe);
                return true;
            }
            return false;
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(recipe);
            }
        });
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
        TextView tvTitle, tvDescription, tvDetails;
        ImageButton btnDelete;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvDescription = itemView.findViewById(R.id.tvRecipeDescription);
            tvDetails = itemView.findViewById(R.id.tvRecipeDetails);
            btnDelete = itemView.findViewById(R.id.btnDeleteRecipe);
        }
    }
}