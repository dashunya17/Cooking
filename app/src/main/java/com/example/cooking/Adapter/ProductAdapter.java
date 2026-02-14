package com.example.cooking.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.R;
import com.example.cooking.model.ProductDTO;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDTO> products;
    private OnItemClickListener listener;
    private boolean showDeleteButton;

    public interface OnItemClickListener {
        void onDeleteClick(int position, ProductDTO product);
        void onItemClick(int position, ProductDTO product);
    }

    public ProductAdapter(List<ProductDTO> products, OnItemClickListener listener, boolean showDeleteButton) {
        this.products = products;
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDTO product = products.get(position);

        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());

        String commonStatus = product.getIsCommon() ? "Общий" : "Личный";
        holder.tvIsCommon.setText(commonStatus);

        if (showDeleteButton) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(position, product);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<ProductDTO> newList) {
        products.clear();
        products.addAll(newList);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvIsCommon;
        ImageButton btnDelete;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
            tvIsCommon = itemView.findViewById(R.id.tvIsCommon);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}