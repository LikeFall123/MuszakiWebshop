package com.example.muszakiwebshop;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muszakiwebshop.Notifications.AlarmReceiver;
import com.example.muszakiwebshop.Notifications.NotificationJobService;
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

public class WebShopActivity extends AppCompatActivity {

    private static final String PREF_KEY = WebShopActivity.class.getPackage().toString();

    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<WebShopItem> mItemList;
    private WebShopItemAdapter mAdapter;
    private WebShopItemAdapterVert mAdapterVert;

    private SharedPreferences preferences;

    private int gridNumber = 1;

    private FrameLayout circle;
    private TextView contentTextView;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    //notificationon es alarmok
    private WebShopNotification mNotificationHandler;
    private AlarmManager alarmManager;
    private JobScheduler mJobScheduler;

    public WebShopActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_shop);

        user = FirebaseAuth.getInstance().getCurrentUser();

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        if(user!=null){
        }else{
            finish();
        }

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new WebShopItemAdapter(this, mItemList);
        mAdapterVert = new WebShopItemAdapterVert(this,mItemList);

        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("items");
        queryData();

        gridNumber = preferences.getInt("gridNumber",1);
        refreshCartCount();
//

        mNotificationHandler = new WebShopNotification(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        setJobScheduler();


        //initializeData();
    }

    private void initializeData(){
        String[] itemList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemPricing = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemImage = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for(int i = 0; i < itemList.length; i++){
            //firestorehoz adja
            mItems.add(new WebShopItem(itemList[i],itemInfo[i],itemPricing[i],itemRate.getFloat(i,0),itemImage.getResourceId(i,0),0));
            //arraylisthez adja hozza
            //mItemList.add(new WebShopItem(itemList[i],itemInfo[i],itemPricing[i],itemRate.getFloat(i,0),itemImage.getResourceId(i,0)));
        }

        itemImage.recycle();

    }

    //lekeri az adatokat a Firestorebol, ha ures, akkor pedig az initDataval fel is tolti
    private void queryData() {
        mItemList.clear();
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(10).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WebShopItem item = document.toObject(WebShopItem.class);
                        item.setId(document.getId());
                        mItemList.add(item);
                    }

                    if (mItemList.size() == 0) {
                        initializeData();
                        queryData();
                    }

                    // Notify the adapter of the change.
                    mAdapter.notifyDataSetChanged();
                    mAdapterVert.notifyDataSetChanged();
                });


    }

    public void deleteItem(WebShopItem item){
        DocumentReference refItems = mItems.document(item._getId());

        refItems.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(WebShopActivity.this, item.getName() + " ki lett törölve!",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WebShopActivity.this, "Elemet: " + item._getId() + ", nem lehet törölni!",Toast.LENGTH_LONG).show();
            }
        });

        queryData();
        refreshCartCount();
        mNotificationHandler.cancel();
    }


    //beallitja a menu savot + keresesnek querytextlistener -> filtert hivja az adapterbol
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.web_shop_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mAdapter.getFilter().filter(newText);

                return false;
            }
        });
        return true;
    }

    //melyik gombot valasztottuk ki + action listener
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.log_out:
                for(WebShopItem items : mItemList){
                    mItems.document(items._getId()).update("cartedCount", 0)
                            .addOnFailureListener(fail -> {
                                Toast.makeText(this, "Item " + items._getId() + " cannot be changed.", Toast.LENGTH_LONG).show();
                            });
                }
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.setting_button:
                return true;
            case R.id.cart:
                cart();
                return true;
            case R.id.view_selector:
                if(gridNumber==2){
                    changeSpanCount(item, R.drawable.ic_view_grid,1);
                    gridNumber = 1;
                }else if(gridNumber==1){
                    changeSpanCount(item, R.drawable.ic_view_row,2);
                    gridNumber = 2;
                }
                return true;
            case R.id.about_button:
                about();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    //beallitja mennyi termek van a kosarban ( cart + custom menu )
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        circle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(alertMenuItem);
            }
        });


        return super.onPrepareOptionsMenu(menu);
    }

    private void about(){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void cart() {
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    public void updateAlertIcon(WebShopItem item){
        queryData();
        mItems.document(item._getId()).update("cartedCount", item.getCartedCount() + 1)
                .addOnFailureListener(fail -> {
                    Toast.makeText(this, "Item " + item._getId() + " cannot be changed.", Toast.LENGTH_LONG).show();
                });

        refreshCartCount();
        mNotificationHandler.send(item.getName());
        queryData();
    }

    private void refreshCartCount() {

        mItems.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i = 0;
                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                    WebShopItem item = document.toObject(WebShopItem.class);
                    item.setId(document.getId());
                    if(item.getCartedCount()>0){
                        i=i+item.getCartedCount();
                    }
                }
                WebShopActivity.this.refreshCartIcon(i);
            }
        });
    }

    private void refreshCartIcon(int i) {
        if(0<i){
            contentTextView.setText(String.valueOf(i));
        }else{
            contentTextView.setText("");
        }
        circle.setVisibility((i > 0) ? VISIBLE : GONE);
    }


    private void setAlarmManager(){
        long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime()+repeatInterval;
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                repeatInterval,
                pendingIntent);

        //igy lehet leallitani
        //alarmManager.cancel(pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setJobScheduler() {
        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadLine = 5000;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0,name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadLine);
        mJobScheduler.schedule(builder.build());

        //igy lehet megallitani
        //mJobScheduler.cancel(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshCartCount();
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
        refreshCartCount();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshCartCount();
    }

}
