package com.android.example.gawala.Provider.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Provider.Adapters.SelectedCitiesAdapter;
import com.android.example.gawala.Provider.Adapters.SpinnerAdapter;
import com.android.example.gawala.Transporter.Models.CityModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class EditCitiesFragment extends DialogFragment implements View.OnClickListener, SelectedCitiesAdapter.Callback {

    private ImageButton cancelImageButton;
    private Button doneButton;
    private RecyclerView selectedCitiesRecyclerView;
    private SelectedCitiesAdapter selectedCitiesAdapter;
    private ArrayList<CityModel> cityModelArrayList;
    private ArrayList<String> countriesArrayList;
    private ArrayList<String> currentCitiesArrayList;
    private Spinner countrySpinner, citySpinner;
    private Button addNewCityButton;
    private SpinnerAdapter countrySpinnerAdapter, currentCitiesSpinnerAdapter;
    private String myId;

    private Callback callback;
    private ValueEventListener mCitiesNodeListener;
    private DatabaseReference citiesNodeRef;


    public static EditCitiesFragment getInmstance() {
        return new EditCitiesFragment();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_edit_cities, container, false);

        initFields(rootView);
        attachListeners();

        loadData();
        return rootView;
    }

    private void initFields(View rootView) {


        cancelImageButton = rootView.findViewById(R.id.ib_eidt_cities_dialig_cancel);
        doneButton = rootView.findViewById(R.id.bt_edit_cities_dialig_done);

        countrySpinner = rootView.findViewById(R.id.sp_eidt_cities_dialig_countries);
        countriesArrayList = new ArrayList<>();
        countrySpinnerAdapter = new SpinnerAdapter(getActivity(), countriesArrayList);
        countrySpinner.setAdapter(countrySpinnerAdapter);

        citySpinner = rootView.findViewById(R.id.sp_eidt_cities_dialig_cities);
        currentCitiesArrayList = new ArrayList<>();
        currentCitiesSpinnerAdapter = new SpinnerAdapter(getActivity(), currentCitiesArrayList);
        citySpinner.setAdapter(currentCitiesSpinnerAdapter);

        addNewCityButton = rootView.findViewById(R.id.bt_edit_cities_add_new);
        selectedCitiesRecyclerView = rootView.findViewById(R.id.rv_edit_cities_dialog_cities);
        cityModelArrayList = new ArrayList<>();
        selectedCitiesAdapter = new SelectedCitiesAdapter(cityModelArrayList, this);
        selectedCitiesRecyclerView.setAdapter(selectedCitiesAdapter);


        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        citiesNodeRef = rootRef.child("users")
                .child(myId)
                .child("cities");

        mCitiesNodeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cityModelArrayList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot countrySnap : dataSnapshot.getChildren()) {
                        String countryName = countrySnap.getKey();
                        for (DataSnapshot citySnap : countrySnap.getChildren()) {
                            String cityName = citySnap.getValue(String.class);
                            String id = citySnap.getKey();
                            CityModel cityModel = new CityModel(id, countryName, cityName);
                            cityModelArrayList.add(cityModel);
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "no cities were added", Toast.LENGTH_SHORT).show();
                }
                selectedCitiesAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void attachListeners() {
        cancelImageButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
        addNewCityButton.setOnClickListener(this);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadCities((String) countrySpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadCities(String selectedItem) {
        currentCitiesArrayList.clear();
        String jsonString = loadJSONFromAsset();

        String locale = getActivity().getResources().getConfiguration().locale.getDisplayCountry();
        System.out.println("countryname :" + locale);

        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray currentCitiesJsonArray = jsonObject.getJSONArray(selectedItem);

            for (int i = 0; i < currentCitiesJsonArray.length(); i++) {
                if (((String) currentCitiesJsonArray.get(i)).isEmpty()) continue;
                currentCitiesArrayList.add((String) currentCitiesJsonArray.get(i));
            }
            Collections.sort(currentCitiesArrayList);
            currentCitiesSpinnerAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void loadData() {
        loadCountries();
        loadSelectedCities();
        selectedCitiesAdapter.notifyDataSetChanged();
        // TODO: 11/14/2019  loadt the data from json parse it
    }

    private void loadSelectedCities() {
        citiesNodeRef.addValueEventListener(mCitiesNodeListener);
    }


    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("countriesToCities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onDestroy() {
        citiesNodeRef.removeEventListener(mCitiesNodeListener);
        super.onDestroy();
    }

    private void loadCountries() {
        long start = Calendar.getInstance().getTimeInMillis();
        String jsonString = loadJSONFromAsset();
        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("json total time : " + (end - start));

        countriesArrayList.clear();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray countriesJsonArray = jsonObject.names();

            for (int i = 0; i < countriesJsonArray.length(); i++) {
                if (((String) countriesJsonArray.get(i)).isEmpty()) continue;
                countriesArrayList.add((String) countriesJsonArray.get(i));
            }
            Collections.sort(countriesArrayList);
            countrySpinnerAdapter.notifyDataSetChanged();


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_eidt_cities_dialig_cancel:
            case R.id.bt_edit_cities_dialig_done:
                this.callback.onArrayListUpdated(cityModelArrayList);
                dismiss();
                break;
            case R.id.bt_edit_cities_add_new:
                addNewCityToFirebase();
                break;
        }
    }

    private void addNewCityToFirebase() {
        rootRef.child("users")
                .child(myId)
                .child("cities")
                .child(countrySpinner.getSelectedItem().toString())//selected contry
                .push()//unique key for city
                .setValue(citySpinner.getSelectedItem().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "City Added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRemoveCity(int position) {

        CityModel cityModel = cityModelArrayList.get(position);
        rootRef.child("users").child(myId).child("cities")
                .child(cityModel.getCountry()).child(cityModel.getId()).removeValue()
                .addOnCompleteListener(task -> Toast.makeText(getContext(), "city removed successfully", Toast.LENGTH_SHORT).show());

        Toast.makeText(getContext(), String.format("I was clicked at poostion %d", position), Toast.LENGTH_SHORT).show();
    }

    public interface Callback {
        void onArrayListUpdated(ArrayList<CityModel> arrayList);
    }
}


//now add the selected country to firebase and also load them at run time
//        then put that in a suitable app flow place
//then let the consumer only see those producers that cover his/her area and the stick to sticky notes