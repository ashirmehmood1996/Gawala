package com.android.example.gawala.Generel.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.example.gawala.Generel.Adapters.NotificationsAdapter;
import com.android.example.gawala.Generel.Models.NotificationModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.Callback {

    private RecyclerView recyclerView;
    private NotificationsAdapter notificationsAdapter;
    private ArrayList<NotificationModel> notificationModelArrayList;


    private String myId;
    private AlertDialog mAlertDialog;
    private int indexForAdding;

    private RelativeLayout emptyViewcontainer;
    private FrameLayout pogressBarcontainer;
    SwipeRefreshLayout swipeRefreshLayout;

    private String lastNotificationKey;
    private boolean allDataLoaded;
    private RecyclerView.OnScrollListener onScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        getSupportActionBar().setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initFields();
        attachListeners();
        loadNotifications(false);

    }

    private void attachListeners() {

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadNotifications(true);

        });


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            recyclerView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//
//            });
//
//        } else {


        onScrollListener = new RecyclerView.OnScrollListener() { //fixme  later see for this depricated code
            boolean isScrolling = false;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (allDataLoaded) {
                    recyclerView.removeOnScrollListener(this);
                    return;
                }
                if (recyclerView.getLayoutManager() != null) {
                    int totalItems = recyclerView.getLayoutManager().getItemCount();
                    int visibleItems = recyclerView.getLayoutManager().getChildCount();
                    int scrolledItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (isScrolling && (visibleItems + scrolledItems >= totalItems)) {
                        indexForAdding = totalItems;
                        loadNotificationsAfterScroll();
                        isScrolling = false;
                    }
                }
            }
        };
        recyclerView.setOnScrollListener(onScrollListener);//// FIXME: 1/4/2020 deal with depricated code if time


//        }

    }

    private void loadNotificationsAfterScroll() {
        pogressBarcontainer.setVisibility(View.VISIBLE);
        rootRef.child("notifications")
                .child(myId)//reciever id
                .orderByKey()
                .endAt(lastNotificationKey)
                .limitToLast(21)//because 1 is duplicate
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (NotificationsActivity.this != null) {
                            if (dataSnapshot.exists()) {
//                                notificationModelArrayList.clear();
                                boolean isFirst = true;
                                String duplicatedId = lastNotificationKey; // because the first notification retrieved in this query is not the duplicate one and is the last one of this new query
                                if (dataSnapshot.getChildrenCount() < 20) { //that means all data is loaded  and this is the last batch
                                    allDataLoaded = true;
                                }
                                for (DataSnapshot notificationsSnap : dataSnapshot.getChildren()) {
                                    String notificationId = notificationsSnap.getKey();
                                    if (notificationId.equals(duplicatedId)) {
                                        continue;
                                    }
                                    if (isFirst) {
                                        lastNotificationKey = notificationId;
                                        isFirst = false;
                                    }
                                    String senderId = notificationsSnap.child("sender_id").getValue(String.class);
                                    String title = notificationsSnap.child("title").getValue(String.class);
                                    String message = notificationsSnap.child("message").getValue(String.class);
                                    String timeStamp = notificationsSnap.child("time_stamp").getValue(String.class);//this may be null so deal bufddy
                                    String type = notificationsSnap.child("type").getValue(String.class);
                                    NotificationModel notificationModel = new NotificationModel(notificationId, senderId, title, type, message, timeStamp);
                                    notificationModelArrayList.add(indexForAdding, notificationModel);
//                                    }
                                }
                                notificationsAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(NotificationsActivity.this, "No notifications yet", Toast.LENGTH_SHORT).show();

                            }
                            if (notificationModelArrayList.size() == 0) {
                                emptyViewcontainer.setVisibility(View.VISIBLE);
                            } else {
                                emptyViewcontainer.setVisibility(View.GONE);
                            }
                            pogressBarcontainer.setVisibility(View.GONE);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void initFields() {
        recyclerView = findViewById(R.id.rv_notification);
        notificationModelArrayList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationModelArrayList, this);
        recyclerView.setAdapter(notificationsAdapter);
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        emptyViewcontainer = findViewById(R.id.rl_notification_empty_view_container);

        swipeRefreshLayout = findViewById(R.id.srl_notifications);


        pogressBarcontainer = findViewById(R.id.cv_notification_load);
        initializeDialog();
        allDataLoaded = false;
    }

    private void loadNotifications(boolean isRefreshing) {
        indexForAdding = 0;
        if (!isRefreshing) {
            mAlertDialog.show();
        } else {
            allDataLoaded = false;
            recyclerView.setOnScrollListener(onScrollListener);
        }


        rootRef.child("notifications")
                .child(myId)//reciever id
                .orderByKey()
                .limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (NotificationsActivity.this != null) {
                            if (dataSnapshot.exists()) {
                                notificationModelArrayList.clear();
                                boolean isFirst = true;
                                for (DataSnapshot notificationsSnap : dataSnapshot.getChildren()) {
                                    String notificationId = notificationsSnap.getKey();
                                    if (isFirst) {
                                        lastNotificationKey = notificationId;
                                        isFirst = false;
                                    }
                                    String senderId = notificationsSnap.child("sender_id").getValue(String.class);
                                    String title = notificationsSnap.child("title").getValue(String.class);
                                    String message = notificationsSnap.child("message").getValue(String.class);
                                    String timeStamp = notificationsSnap.child("time_stamp").getValue(String.class);//this may be null so deal bufddy
                                    String type = notificationsSnap.child("type").getValue(String.class);
                                    NotificationModel notificationModel = new NotificationModel(notificationId, senderId, title, type, message, timeStamp);
                                    notificationModelArrayList.add(indexForAdding, notificationModel);

//                                    }
                                }
                            } else {
                                Toast.makeText(NotificationsActivity.this, "No notifications yet", Toast.LENGTH_SHORT).show();

                            }
                            if (notificationModelArrayList.size() == 0) {
                                emptyViewcontainer.setVisibility(View.VISIBLE);
                            } else {
                                emptyViewcontainer.setVisibility(View.GONE);
                            }
                            if (!isRefreshing)
                                mAlertDialog.cancel();
                            else
                                swipeRefreshLayout.setRefreshing(false);

                            notificationsAdapter.notifyDataSetChanged();

                            final LayoutAnimationController controller =
                                    AnimationUtils.loadLayoutAnimation(NotificationsActivity.this, R.anim.layout_animation_fall_down);

                            recyclerView.setLayoutAnimation(controller);
                            recyclerView.getAdapter().notifyDataSetChanged();
                            recyclerView.scheduleLayoutAnimation();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    @Override
    public void onNotificationItemClicked(int pos) {


    }
}
