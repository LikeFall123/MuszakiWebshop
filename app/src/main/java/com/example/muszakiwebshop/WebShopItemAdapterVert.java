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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

public class WebShopItemAdapterVert extends RecyclerView.Adapter<WebShopItemAdapterVert.ViewHolder> implements Filterable {

    private ArrayList<WebShopItem> mWebShopItemData;
    private ArrayList<WebShopItem> mWebShopItemDataAll;
    private Context mContext;
    private int lastPosition = -1;


    public WebShopItemAdapterVert(Context mContext, ArrayList<WebShopItem> itemsData) {
        this.mWebShopItemData = itemsData;
        this.mWebShopItemDataAll = itemsData;
        this.mContext = mContext;
    }


    @Override
    public Filter getFilter() {
        return shoppingFilter;
    }

    //keresesi filter
    private Filter shoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<WebShopItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length()==0){
                results.count = mWebShopItemDataAll.size();
                results.values = mWebShopItemDataAll;
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(WebShopItem item : mWebShopItemDataAll){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mWebShopItemData = (ArrayList) filterResults.values;
            notifyDataSetChanged();
        }
    };

    //layout bindolasa az adapterhez
    @NonNull
    @Override
    public WebShopItemAdapterVert.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_vert,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull WebShopItemAdapterVert.ViewHolder holder, int position) {
        WebShopItem currentItem = mWebShopItemData.get(position);

        holder.bindTo(currentItem);

        //animacio hozzadadasa ha tullepi a kimutatott szamot
        if(holder.getAdapterPosition()>lastPosition){
            Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(anim);
            lastPosition = holder.getAdapterPosition();
        }
    }

//    public void setOrientation(int view){
//        int count = views.size();
//        for(int i=0; i<count; i++)
//        {
//            ViewHolder v = views.get(i);
//            //call imageview from the viewholder object by the variable name used to instatiate it
//            ImageView imageView1 = v.imageViewFromHolder;
//            imageView1.setImageResource(R.mipmap.playsound);
//            LinearLayout linearSeek1 = v.linearLayoutFromHolder;
//            linearSeek1.setVisibility(View.GONE);
//        }
//    }

    @Override
    public int getItemCount() {
        return mWebShopItemData.size();
    }

    //beallitja egy kartyanak az adatait
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private RatingBar mRatingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            mPriceText = itemView.findViewById(R.id.price);

        }

        public void bindTo(WebShopItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mRatingBar.setRating(currentItem.getRatedInfo());

            Glide.with(mContext).load(currentItem.getImage()).into(mItemImage);

            itemView.findViewById(R.id.add_to_cart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation anim = AnimationUtils.loadAnimation(mContext,R.anim.bounce);
                    view.startAnimation(anim);
                    ((WebShopActivity)mContext).updateAlertIcon( currentItem);
                }
            });

            itemView.findViewById(R.id.delete_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation anim = AnimationUtils.loadAnimation(mContext,R.anim.bounce);
                    view.startAnimation(anim);
                    ((WebShopActivity)mContext).deleteItem(currentItem);
                }
            });
        }
    }
}


