package com.example.locationapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private Context context;
    private List<Property> propertyList;

    public PropertyAdapter(Context context, List<Property> propertyList) {
        this.context = context;
        this.propertyList = propertyList;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);

        holder.tvTitle.setText(property.getTitle());
        holder.tvPrice.setText(formatPrice(property.getPrice()));
        holder.tvLocation.setText(property.getCity());
        holder.tvDetails.setText(formatDetails(property));

        // Load first image if available
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            String imageUrl = property.getImageUrls().get(0);
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_home).into(holder.ivProperty);
        } else {
            holder.ivProperty.setImageResource(R.drawable.ic_home);
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailActivity.class);
            intent.putExtra("PROPERTY_ID", property.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return formatter.format(price);
    }

    private String formatDetails(Property property) {
        return property.getBedrooms() + " Ch | " +
                property.getBathrooms() + " SdB | " +
                property.getArea() + " m²";
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProperty;
        TextView tvTitle, tvPrice, tvLocation, tvDetails;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProperty = itemView.findViewById(R.id.ivProperty);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}
