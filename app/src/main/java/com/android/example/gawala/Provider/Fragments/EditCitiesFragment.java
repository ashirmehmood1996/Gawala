package com.android.example.gawala.Provider.Fragments;

import android.content.DialogInterface;
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

import com.android.example.gawala.Provider.Adapters.SelectedCitiesAdapter;
import com.android.example.gawala.Provider.Adapters.SpinnerAdapter;
import com.android.example.gawala.Provider.Models.CityModel;
import com.android.example.gawala.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
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

    private static final String SELECT_COUNTRY = "Select Country";
    private ImageButton cancelImageButton;
//    private Button doneButton;
    //    private RecyclerView selectedCitiesRecyclerView;
//    private SelectedCitiesAdapter selectedCitiesAdapter;
    private ArrayList<CityModel> selectedCityModelArrayList;
    private ArrayList<String> countriesArrayList;
    private ArrayList<String> citiesArrayList;
    private Spinner countrySpinner, citySpinner;
    private Button addNewCityButton;
    private SpinnerAdapter countrySpinnerAdapter, citiesSpinnerAdapter;

    private ChipGroup chipGroup;

    private String myId;

    private Callback callback;
    private ValueEventListener mCitiesNodeListener;
    private DatabaseReference citiesNodeRef;
    private String SELECT_CITY = "Select City";


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
        chipGroup = rootView.findViewById(R.id.cg_eidt_cities);

        cancelImageButton = rootView.findViewById(R.id.ib_eidt_cities_dialig_back);
//        doneButton = rootView.findViewById(R.id.bt_edit_cities_dialig_done);

        countrySpinner = rootView.findViewById(R.id.sp_eidt_cities_dialig_countries);
        countriesArrayList = new ArrayList<>();
        countrySpinnerAdapter = new SpinnerAdapter(getActivity(), countriesArrayList);
        countrySpinner.setAdapter(countrySpinnerAdapter);

        citySpinner = rootView.findViewById(R.id.sp_eidt_cities_dialig_cities);
        citiesArrayList = new ArrayList<>();
        citiesSpinnerAdapter = new SpinnerAdapter(getActivity(), citiesArrayList);
        citySpinner.setAdapter(citiesSpinnerAdapter);

        addNewCityButton = rootView.findViewById(R.id.bt_edit_cities_add_new);
        addNewCityButton.setEnabled(false);
//        selectedCitiesRecyclerView = rootView.findViewById(R.id.rv_edit_cities_dialog_cities);
        selectedCityModelArrayList = new ArrayList<>();
//        selectedCitiesAdapter = new SelectedCitiesAdapter(selectedCityModelArrayList, this);
//        selectedCitiesRecyclerView.setAdapter(selectedCitiesAdapter);


        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        citiesNodeRef = rootRef.child("users").child(myId).child("cities");

        mCitiesNodeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selectedCityModelArrayList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot countrySnap : dataSnapshot.getChildren()) {
                        String countryName = countrySnap.getKey();
                        for (DataSnapshot citySnap : countrySnap.getChildren()) {
                            String cityName = citySnap.getKey();
                            CityModel cityModel = new CityModel(countryName, cityName);
                            selectedCityModelArrayList.add(cityModel);
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "no cities were added", Toast.LENGTH_SHORT).show();
                }
                populateChips();


//                selectedCitiesAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void populateChips() {
        chipGroup.removeAllViews();
        for (CityModel cityModel : selectedCityModelArrayList) {
            Chip chip = new Chip(getActivity());
            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(getActivity(),
                    null,
                    0,
                    R.style.Widget_MaterialComponents_Chip_Entry);
            chip.setChipDrawable(chipDrawable);

            chip.setText(String.format("%s, %s", cityModel.getCity(), cityModel.getCountry()));
            chip.setOnCloseIconClickListener(v -> {
                int index = chipGroup.indexOfChild(v);
                onRemoveCity(index);
            });
            chipGroup.addView(chip);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        this.callback.onArrayListUpdated(selectedCityModelArrayList);
        super.onDismiss(dialog);
    }

    private void attachListeners() {
        cancelImageButton.setOnClickListener(this);
//        doneButton.setOnClickListener(this);
        addNewCityButton.setOnClickListener(this);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (countrySpinner.getSelectedItem().equals(SELECT_COUNTRY)) {
                    citiesArrayList.clear();
                    citiesSpinnerAdapter.notifyDataSetChanged();
                    citySpinner.setVisibility(View.GONE);
                    addNewCityButton.setEnabled(false);
                } else {
                    loadCities((String) countrySpinner.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (citySpinner.getSelectedItem().equals(SELECT_CITY)) {
                    addNewCityButton.setEnabled(false);
                } else {
                    addNewCityButton.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void loadCities(String selectedItem) {
        citiesArrayList.clear();
        String jsonString = loadJSONFromAsset();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray currentCitiesJsonArray = jsonObject.getJSONArray(selectedItem);

            for (int i = 0; i < currentCitiesJsonArray.length(); i++) {
                if (((String) currentCitiesJsonArray.get(i)).isEmpty()) continue;
                citiesArrayList.add((String) currentCitiesJsonArray.get(i));
            }
            Collections.sort(citiesArrayList);
            citiesArrayList.add(0, SELECT_CITY);
            citySpinner.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
            citySpinner.setVisibility(View.GONE);

        }
        citiesSpinnerAdapter.notifyDataSetChanged();
        addNewCityButton.setEnabled(true);


    }

    private void loadData() {
        loadCountries();
        loadSelectedCities();
//        selectedCitiesAdapter.notifyDataSetChanged();
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
            countriesArrayList.add(0, SELECT_COUNTRY);
            countrySpinnerAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_eidt_cities_dialig_back:
                dismiss();
                break;
//            case R.id.bt_edit_cities_dialig_done:
//                dismiss();
//                break;
            case R.id.bt_edit_cities_add_new:
                addNewCityToFirebase();
                break;
        }
    }

    private void addNewCityToFirebase() {
//        rootRef.child("users")
//                .child(myId)
//                .child("cities")
//                .child(countrySpinner.getSelectedItem().toString())//selected contry
//                .push()//unique key for city
//                .setValue(citySpinner.getSelectedItem().toString())
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(getContext(), "City Added Successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getContext(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

        addNewCityButton.setEnabled(false);
        String country = countrySpinner.getSelectedItem().toString();
        String city = country;
        if (citySpinner != null && citySpinner.getSelectedItem() != null) {
            city = citySpinner.getSelectedItem().toString();
        }
        if (country.equals(SELECT_COUNTRY) || city.equals(SELECT_CITY)) {
            Toast.makeText(getActivity(), "please Set both the Country and City", Toast.LENGTH_SHORT).show();
            return;
        }


        rootRef.child("users")
                .child(myId)
                .child("cities")
                .child(country)//selected contry
                .child(city)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "City Added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void onRemoveCity(int position) {

        CityModel cityModel = selectedCityModelArrayList.get(position);
        rootRef.child("users").child(myId).child("cities")
                .child(cityModel.getCountry()).child(cityModel.getCity()).removeValue()
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