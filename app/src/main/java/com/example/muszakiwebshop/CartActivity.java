package com.example.muszakiwebshop;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muszakiwebshop.Notifications.CartNotification;
import com.example.muszakiwebshop.Notifications.WebShopNotification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private static final String PREF_KEY = WebShopActivity.class.getPackage().toString();

    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private ArrayList<WebShopItem> mCartList;
    private CartItemAdapter mAdapter;
    private CartItemAdapterVert mAdapterVert;

    private SharedPreferences preferences;

    private int gridNumber = 1;

    private CollectionReference mItems;

    private FirebaseFirestore mFirestore;

    private CartNotification mNotificationHandler;

    public CartActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_layout);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        if(user!=null){

        }else{
            finish();
        }

        gridNumber = preferences.getInt("gridNumber",1);

        mCartList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,gridNumber));
        mAdapter = new CartItemAdapter(this, mCartList);
        mAdapterVert = new CartItemAdapterVert(this, mCartList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();

        //az osszes
        mItems = mFirestore.collection("items");

        mNotificationHandler = new CartNotification(this);

        queryData();
        //initializeData();
    }

    private void queryData(){
        mCartList.clear();
        mItems.whereGreaterThan("cartedCount",0).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    WebShopItem item = document.toObject(WebShopItem.class);
                    item.setId(document.getId());
                    mCartList.add(item);
                }
                mAdapter.notifyDataSetChanged();
                mAdapterVert.notifyDataSetChanged();
            }
        });

    }

    private void emptyCart() {
        for(WebShopItem item : mCartList){
            mItems.document(item._getId()).update("cartedCount",0).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CartActivity.this, "Elemet: " + item._getId() + ", nem lehet törölni!",Toast.LENGTH_LONG).show();
                }
            });
        }
        mCartList.removeAll(mCartList);
        queryData();
    }


    public void buyItem(WebShopItem item){
        mItems.document(item._getId()).update("cartedCount", 0).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CartActivity.this, "Elemet: " + item._getId() + " nem lehet megváltoztatni", Toast.LENGTH_LONG).show();
            }
        });
        Toast.makeText(CartActivity.this, item.getName()+ " terméket megvetted!", Toast.LENGTH_LONG).show();
        mNotificationHandler.send(item.getName());
        queryData();
    }

    public void deleteFromCart(WebShopItem item){

        if(item.getCartedCount()>1){
            mItems.document(item._getId()).update("cartedCount",item.getCartedCount()-1).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CartActivity.this,"Elemet: "+item._getId()+" nem lehet megváltoztatni",Toast.LENGTH_LONG).show();
                }
            });
        }else if(item.getCartedCount()==1) {
            mItems.document(item._getId()).update("cartedCount", 0).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CartActivity.this, "Elemet: " + item._getId() + " nem lehet megváltoztatni", Toast.LENGTH_LONG).show();
                }
            });
        }

        queryData();
    }

    //beallitja a menu savot + keresesnek querytextlistener -> filtert hivja az adapterbol
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cart_menu,menu);
        return true;
    }

    //melyik gombot valasztottuk ki + action listener
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.view_selector:
                if (gridNumber==2) {
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                    gridNumber = 1;
                } else if (gridNumber==1){
                    changeSpanCount(item, R.drawable.ic_view_row, 2);
                    gridNumber = 2;
                }
                return true;
            case R.id.empty_cart:
                emptyCart();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void return_to_main(MenuItem item) {
       finish();
    }

    private void changeSpanCount(MenuItem item, int drawable, int spanCount){
        item.setIcon(drawable);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
        if(gridNumber==1){
            mRecyclerView.setAdapter(mAdapterVert);
        }else if(gridNumber==2){
            mRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("gridNumber", gridNumber);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

}


