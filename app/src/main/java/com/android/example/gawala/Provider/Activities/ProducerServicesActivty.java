package com.android.example.gawala.Provider.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.example.gawala.Consumer.Activities.ConProducerServiceDetailsActivty;
import com.android.example.gawala.Consumer.Activities.ProducerDetailActivty;
import com.android.example.gawala.Generel.Adapters.GoodsAdapter;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProducerServicesActivty extends AppCompatActivity implements GoodsAdapter.CallBack {
    private FloatingActionButton addServiceButton;
    private DatabaseReference myGoodsRef;
    private ChildEventListener goodsListListener;

    private ArrayList<GoodModel> goodModelArrayList;
    private RecyclerView recyclerView;
    private GoodsAdapter goodsAdapter;
    private RelativeLayout emptyViewContainerRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prodcuer_services_activty);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFields();
        atachListeners();
        myGoodsRef.addChildEventListener(goodsListListener);
        checkIfAnyGoodIsAdded();
    }


    private void initFields() {
        addServiceButton = findViewById(R.id.fab_prod_service_add_new);
        recyclerView = findViewById(R.id.rv_prod_service);
        goodModelArrayList = new ArrayList<>();
        goodsAdapter = new GoodsAdapter(goodModelArrayList, this);
        recyclerView.setAdapter(goodsAdapter);

        myGoodsRef = rootRef.child("goods").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        initGoodsListener();
        emptyViewContainerRelativeLayout = findViewById(R.id.rl_pro_services_empty_view_container);
    }

    private void initGoodsListener() {
        goodsListListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (emptyViewContainerRelativeLayout.getVisibility() == View.VISIBLE) {
                    emptyViewContainerRelativeLayout.setVisibility(View.GONE);
                }
                GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
                goodModelArrayList.add(goodModel);
                goodsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GoodModel newGoodModel = dataSnapshot.getValue(GoodModel.class);
                for (int i = 0; i < goodModelArrayList.size(); i++) {
                    GoodModel goodModel = goodModelArrayList.get(i);
                    if (goodModel.getId().equals(newGoodModel.getId())) {
                        goodModelArrayList.set(i, newGoodModel); //// TODO: 10/5/2019  check wether this logic is good enough
                        break;
                    }
                }
                goodsAdapter.notifyDataSetChanged();


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                GoodModel newGoodModel = dataSnapshot.getValue(GoodModel.class);
                for (int i = 0; i < goodModelArrayList.size(); i++) {
                    GoodModel goodModel = goodModelArrayList.get(i);
                    if (goodModel.getId().equals(newGoodModel.getId())) {
                        goodModelArrayList.remove(goodModel);//// TODO: 10/5/2019  check wether this logic is good enough
                        break;
                    }
                }
                goodsAdapter.notifyDataSetChanged();
                if (goodModelArrayList.isEmpty()) {
                    emptyViewContainerRelativeLayout.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void atachListeners() {
        addServiceButton.setOnClickListener(v -> startActivity(new Intent(ProducerServicesActivty.this, ProducerAddServiceActivity.class)));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if ((newState == SCROLL_STATE_IDLE /*|| newState == SCROLL_STATE_SETTLING*/)/* && addServiceButton.getVisibility() != View.VISIBLE*/)
                    addServiceButton.show();
                else if (newState == SCROLL_STATE_DRAGGING && addServiceButton.getVisibility() == View.VISIBLE)
                    addServiceButton.hide();
                /*if ((!recyclerView.canScrollVertically(1) || !recyclerView.canScrollVertically(-1))
                        && addServiceButton.getVisibility() != View.VISIBLE) {
                    addServiceButton.show();

                }*/
            }


        });

    }

    private void checkIfAnyGoodIsAdded() {
        myGoodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    emptyViewContainerRelativeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

//    @Override
//    protected void onStart() {
//        myGoodsRef.addChildEventListener(goodsListListener);
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        myGoodsRef.removeEventListener(goodsListListener);
//        goodModelArrayList.clear();
//        goodsAdapter.notifyDataSetChanged();
//        super.onStop();
//    }

    @Override
    protected void onDestroy() {
        myGoodsRef.removeEventListener(goodsListListener);
        super.onDestroy();
    }

    @Override
    public void onGoodItemClick(int position, ImageView sharedImageView) {
        GoodModel currentModel = goodModelArrayList.get(position);
        Intent intent = new Intent(this, ProducerServiceDetailsActivity.class);
        intent.putExtra("goods_model", currentModel);
        intent.putExtra(ProducerDetailActivty.EXTRA_ANIMAL_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedImageView,
                ViewCompat.getTransitionName(sharedImageView));
        startActivity(intent, options.toBundle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);

        }
    }
}
