package com.android.example.gawala.Transporter.Fragments;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class TransporterClientsFragment extends DialogFragment {

    private static final String KEY_PROVIDER_ID = "providerID";
    //data related
    private Query mClientsRef;
    private String myId;

    private String providerId;


    //Ui related
    private RecyclerView recyclerView;
    private ClientsAdapter clientsAdapter;
    private ArrayList<ConsumerModel> consumerModelArrayList;
    private ChildEventListener mClientsChildEventListener;
    private ImageButton backButton;


    public TransporterClientsFragment() {
    }

    public static TransporterClientsFragment newInstance(String providerId) {
        TransporterClientsFragment transporterClientsFragment = new TransporterClientsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PROVIDER_ID, providerId);
        transporterClientsFragment.setArguments(bundle);
        return transporterClientsFragment;
    }

    // FIXME: LATER if time 12/26/2019 may be we should load the already loaded consumer in main activty here rather than fetching them again?
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
        providerId = getArguments().getString(KEY_PROVIDER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_transporter_clients, container, false);
        initfields(rootView);
        initListeners();
        attachListeners();
        return rootView;
    }


    private void initfields(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_frag_transporter_clients);
        consumerModelArrayList = new ArrayList<>();
        clientsAdapter = new ClientsAdapter(consumerModelArrayList, getActivity());
        recyclerView.setAdapter(clientsAdapter);
        backButton = rootView.findViewById(R.id.ib_frag_transporter_clients_back);


        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mClientsRef = rootRef.child("clients").child(providerId)//this will only work for a single provider but if we provide for multiple providers then we may change the query
                .orderByChild("transporter_id").equalTo(myId);

    }

    private void initListeners() {


        mClientsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String timeStamp = dataSnapshot.child("time_stamp").getValue(String.class);

                rootRef.child("users").child(dataSnapshot.getKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
//                                if (clientsContainer.getVisibility() == GONE)
//                                    clientsContainer.setVisibility(View.VISIBLE);
//                                if (emptyViewContainerRelativeLayout.getVisibility() == VISIBLE)
//                                    emptyViewContainerRelativeLayout.setVisibility(GONE);

                                String id = userSnapshot.getKey();
                                String name = userSnapshot.child("name").getValue(String.class);
                                String number = userSnapshot.child("number").getValue(String.class);
                                String imageUri = "";
                                if (userSnapshot.hasChild("profile_image_uri")) {
                                    imageUri = userSnapshot.child("profile_image_uri").getValue(String.class);
                                }

                                String lat = userSnapshot.child("location").child("lat").getValue(String.class);
                                String lng = userSnapshot.child("location").child("lng").getValue(String.class);

                                final ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng, imageUri, 0);

                                if (lat != null) {
                                    String address = lat + "\n" + lng;
                                    if (userSnapshot.child("location").hasChild("address")) {
                                        address = userSnapshot.child("location").child("address").getValue(String.class);
                                    }
                                    consumerModel.setLocationName(address);

                                }

//                        consumerModel.setAmountOfMilk(milkDemand);

                                consumerModelArrayList.add(consumerModel);
                                clientsAdapter.notifyDataSetChanged();
//                                if (lat != null) {
//                                    createNewMarker(consumerModel);
//                                }


//                                countDownLatch.countDown();
//                                System.out.println("count: " + countDownLatch.getCount());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void attachListeners() {
        mClientsRef.addChildEventListener(mClientsChildEventListener);
        backButton.setOnClickListener(v -> dismiss());

    }

    @Override
    public void onDestroy() {
        mClientsRef.removeEventListener(mClientsChildEventListener);
        super.onDestroy();
    }

    // adapter related
    private class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientHolder> {
        private ArrayList<ConsumerModel> consumerModelArrayList;
        private Context context;

        public ClientsAdapter(ArrayList<ConsumerModel> consumerModelArrayList, Context context) {
            this.consumerModelArrayList = consumerModelArrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public ClientHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ClientHolder(LayoutInflater.from(context).inflate(R.layout.li_stop_markers, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ClientHolder holder, int position) {
            ConsumerModel currentConsumer = consumerModelArrayList.get(position);
            String location = null;

            if (currentConsumer.getLocationName() == null || currentConsumer.getLocationName().isEmpty()) {
                location = currentConsumer.getLatitude() + " " + currentConsumer.getLongitude();
            } else {
                location = currentConsumer.getLocationName();
            }

            if (!currentConsumer.getImageUrl().isEmpty()) {
                Glide.with(context).load(currentConsumer.getImageUrl()).into(holder.circularImageView);
            } else {
                Glide.with(context).load(R.drawable.ic_person_black_24dp).into(holder.circularImageView);
            }
            holder.locationTextView.setText(location);
            holder.nameTextView.setText(currentConsumer.getName());

        }

        @Override
        public int getItemCount() {
            return consumerModelArrayList.size();
        }

        class ClientHolder extends RecyclerView.ViewHolder {
            TextView locationTextView, nameTextView;
            LinearLayout containerLinearLayout;
            CircularImageView circularImageView;

            ClientHolder(@NonNull View itemView) {
                super(itemView);
                locationTextView = itemView.findViewById(R.id.tv_li_stops_markers_location);
                nameTextView = itemView.findViewById(R.id.tv_li_stops_markers_name);
                circularImageView = itemView.findViewById(R.id.civ_li_stops_markers_picture);
            }
        }
    }
}
