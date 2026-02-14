package com.example.cooking.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cooking.R;
import com.example.cooking.model.ProductDTO;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductCatalogAdapter extends RecyclerView.Adapter<ProductCatalogAdapter.CatalogViewHolder> {

    private List<ProductDTO> productList;
    private List<ProductDTO> filteredList;
    private OnCatalogItemClickListener listener;
    private Set<Long> userProductIds;

    public interface OnCatalogItemClickListener {
        void onAddProduct(ProductDTO product);
    }

    public ProductCatalogAdapter(List<ProductDTO> productList,
                                 OnCatalogItemClickListener listener,
                                 Set<Long> userProductIds) {
        this.productList = productList;
        this.filteredList = new ArrayList<>(productList);
        this.listener = listener;
        this.userProductIds = userProductIds != null ? userProductIds : new HashSet<>();
    }

    public void filter(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (ProductDTO product : productList) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                        product.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void updateUserProducts(Set<Long> newUserProductIds) {
        this.userProductIds = newUserProductIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_catalog, parent, false);
        return new CatalogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogViewHolder holder, int position) {
        ProductDTO product = filteredList.get(position);
        holder.bind(product, listener, userProductIds.contains(product.getId()));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class CatalogViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvCategory;
        private Button btnAdd;
        private ImageView ivAlreadyAdded;

        public CatalogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            ivAlreadyAdded = itemView.findViewById(R.id.ivAlreadyAdded);
        }

        public void bind(ProductDTO product,
                         OnCatalogItemClickListener listener,
                         boolean isAlreadyAdded) {

            tvProductName.setText(product.getName());
            tvCategory.setText(product.getCategory());

            if (isAlreadyAdded) {
                btnAdd.setVisibility(View.GONE);
                ivAlreadyAdded.setVisibility(View.VISIBLE);
                itemView.setAlpha(0.6f);
                btnAdd.setEnabled(false);
            } else {
                btnAdd.setVisibility(View.VISIBLE);
                ivAlreadyAdded.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
                btnAdd.setEnabled(true);

                btnAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddProduct(product);
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                if (!isAlreadyAdded && listener != null) {
                    listener.onAddProduct(product);
                }
            });
        }
    }
}