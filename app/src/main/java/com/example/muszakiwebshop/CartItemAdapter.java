package com.example.muszakiwebshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private ArrayList<WebShopItem> mCartItemData;
    private Context mContext;
    private int lastPosition = -1;

    public CartItemAdapter(Context mContext, ArrayList<WebShopItem> itemsData) {
        this.mCartItemData = itemsData;
        this.mContext = mContext;
    }


    //layout bindolasa az adapterhez
    @NonNull
    @Override
    public CartItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.cart_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemAdapter.ViewHolder holder, int position) {
        WebShopItem currentItem = mCartItemData.get(position);

        holder.bindTo(currentItem);

        //animacio hozzadadasa ha tullepi a kimutatott szamot
        if(holder.getAdapterPosition()>lastPosition){
            Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(anim);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mCartItemData.size();
    }

    //beallitja egy kartyanak az adatait
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private TextView mCountText;
        private ImageView mItemImage;
        private RatingBar mRatingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            mPriceText = itemView.findViewById(R.id.price);
            mCountText = itemView.findViewById(R.id.count);

        }

        public void bindTo(WebShopItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mRatingBar.setRating(currentItem.getRatedInfo());
            mCountText.setText(String.valueOf(currentItem.getCartedCount()));

            Glide.with(mContext).load(currentItem.getImage()).into(mItemImage);

            itemView.findViewById(R.id.buy_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation anim = AnimationUtils.loadAnimation(mContext,R.anim.bounce);
                    view.startAnimation(anim);
                    ((CartActivity)mContext).buyItem(currentItem);
                }
            });

            itemView.findViewById(R.id.delete_from_cart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation anim = AnimationUtils.loadAnimation(mContext,R.anim.bounce);
                    view.startAnimation(anim);
                    ((CartActivity)mContext).deleteFromCart(currentItem);
                }
            });
        }
    }
}


