package com.android.example.gawala.Generel.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Fraagments.ImageViewerFragment;
import com.android.example.gawala.Generel.Fraagments.RatingFragment;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Provider.Activities.ProviderTransportersActivity;
import com.android.example.gawala.Provider.Fragments.EditCitiesFragment;
import com.android.example.gawala.Provider.Models.CityModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProfileActivity extends AppCompatActivity implements EditCitiesFragment.Callback, RatingFragment.Callbacks {
//    public static final String PROVIDER_ID_ARRAY = "provider_id_array";
    private static final int RC_SET_DELIVERY_LOCATION = 121;
    private static final String DIALOG_EDIT_CITIES = "dialogFragment";
    public static final String USER_ID = "user_id";
    public static final String REQUEST_USER_TYPE = "user_type";
    public static final String OTHER_USER = "otherUser";
    private static final int RC_SELECT_TRANSPORTER = 1111;
    private static final String TAG_RATING_FRAG = "ratingFragment";
    private static final String FRAG_IMAGE_VIEWER = "imageViewerFragment";
    private TextView nameTextView, numberTextView, locationTextView, typeTextView;
    private ImageButton editLocationImageButton;
    private FrameLayout ratingBarConatiner;
    private RatingBar ratingBar;
    private String userId;


    private LinearLayout deliveryAreasContainerLinearLayout, locationContainerLinearLayout;
    private TextView deliveryAreasTextView;
    private ChipGroup deliveryAreasChipGroup;
    private ImageButton editDeliverAreasImageButtons;
    private CircularImageView profileCircularImageView;
    private ImageButton editPictureImageButton;

    private LinearLayout transporterInfoLinearLayout;
    private ImageButton editTransporterImageButton;
    private TextView transporterNameTextView, transporterNumberTextView;


    //data
    private String name, number, locationAddress, type, profileImageUri;
    private LatLng latLng;
    private AlertDialog mProgressDialog;

    private ArrayList<CityModel> cityModelArrayList;
    private int RC_PICK_FROM_GALLERY = 12121;
    private boolean isOtherUser;
    private String transporterID, transporterName, transporterNumber, myId;
//    private ArrayList<String> providerIdArray;
    private String otherUserType;
    private float averageRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFields();
        isOtherUser = getIntent().getBooleanExtra(OTHER_USER, false);
        if (isOtherUser) {
            makeEditableFalse();
            userId = getIntent().getStringExtra(USER_ID);
            otherUserType = getIntent().getStringExtra(REQUEST_USER_TYPE);
//            if (otherUserType != null && otherUserType.equals(getResources().getString(R.string.provider))) {
//                providerIdArray = getIntent().getStringExtra(PROVIDER_ID_ARRAY);
//            }
        } else {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        attachListeners();
        loadDataFromFriebase();
    }


    private void initFields() {

        nameTextView = findViewById(R.id.tv_personal_info_name);
        numberTextView = findViewById(R.id.tv_personal_info_number);
        locationTextView = findViewById(R.id.tv_personal_info_location_name);
        typeTextView = findViewById(R.id.tv_personal_info_user_type);
        editLocationImageButton = findViewById(R.id.ib_personal_info_edit_location);
        ratingBarConatiner = findViewById(R.id.frame_personal_info_rb_conatiner);
        ratingBar = findViewById(R.id.rb_personal_info);

        deliveryAreasContainerLinearLayout = findViewById(R.id.ll_personal_info_delivery_areas_container);
        locationContainerLinearLayout = findViewById(R.id.ll_personal_info_location_container);
        deliveryAreasTextView = findViewById(R.id.tv_personal_info_delivery_areas);
        deliveryAreasChipGroup = findViewById(R.id.cg_personal_info_delivery_areas);
        editDeliverAreasImageButtons = findViewById(R.id.ib_personal_info_edit_delivery_areas);

        //transporter related for consumers
        transporterInfoLinearLayout = findViewById(R.id.ll_personal_info_transporter_container);
        transporterNameTextView = findViewById(R.id.tv_personal_info_transporter_name);
        transporterNumberTextView = findViewById(R.id.tv_personal_info_transporter_number);
        editTransporterImageButton = findViewById(R.id.ib_profile_edit_transporter);

        cityModelArrayList = new ArrayList<>();

        profileCircularImageView = findViewById(R.id.iv_personal_info_image);
        editPictureImageButton = findViewById(R.id.ib_personal_info_edit_image);

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    private void attachListeners() {
        editLocationImageButton.setOnClickListener(v -> showDialogforEditLocation());

        editDeliverAreasImageButtons.setOnClickListener(v -> {
            showEditDeliveryAreasDialog();
        });

        if (!isOtherUser) {
            editPictureImageButton.setOnClickListener(view -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");

                //                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                //                String[] mimeTypes = {"image/jpeg", "image/png"};
                //                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);

                startActivityForResult(galleryIntent
                        , RC_PICK_FROM_GALLERY);
                mProgressDialog.show();

            });
        }
        profileCircularImageView.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            ImageViewerFragment imageViewerFragment = (ImageViewerFragment) getSupportFragmentManager().findFragmentByTag(FRAG_IMAGE_VIEWER);
            if (imageViewerFragment != null) {
                fragmentTransaction.remove(imageViewerFragment);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String transitionName = ViewCompat.getTransitionName(profileCircularImageView);
                if (transitionName == null || transitionName.isEmpty()) {
                    profileCircularImageView.setTransitionName(userId);
                }
                fragmentTransaction.addSharedElement(profileCircularImageView, ViewCompat.getTransitionName(profileCircularImageView));
            }
            imageViewerFragment = ImageViewerFragment.newInstance(profileImageUri, ViewCompat.getTransitionName(profileCircularImageView));
            imageViewerFragment.show(fragmentTransaction, FRAG_IMAGE_VIEWER);

        });

        if (isOtherUser) {
            ratingBarConatiner.setOnClickListener(v -> {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                RatingFragment ratingFragment = (RatingFragment) getSupportFragmentManager().findFragmentByTag(TAG_RATING_FRAG);
                if (ratingFragment != null) {
                    fragmentTransaction.remove(ratingFragment);
                }
                ratingFragment = RatingFragment.newInstance(userId);
                ratingFragment.setCallbacks(this);
                ratingFragment.show(fragmentTransaction, TAG_RATING_FRAG);
            });
        }
        editTransporterImageButton.setOnClickListener(this::showPopup);
    }

    private void showPopup(View v) {

        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.edit_transporter_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.popup_personal_info_change_transporter) {

                Intent intent = new Intent(this, ProviderTransportersActivity.class);
                intent.putExtra(ProviderTransportersActivity.IS_FOR_SELECTION, true);
                startActivityForResult(intent, RC_SELECT_TRANSPORTER);
                return true;
            } else {
                return false;
            }

        });
    }


    private void uploadImageInFirebaseCloudStorage(Bitmap bitmap) {

        StorageReference rootStorageReference = FirebaseStorage.getInstance().getReference();
//String fileType = getContentResolver().getType(profileImageUri);
        final StorageReference profilePicRef = rootStorageReference.child("profilePictures/" + FirebaseAuth.getInstance().getCurrentUser().getUid()/*+ fileType*/);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = profilePicRef.putBytes(data);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getApplicationContext(), "Upload unsuccessful \n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
        }).addOnSuccessListener(taskSnapshot -> {
            /*Task<Uri> urlTask = */
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return profilePicRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadImageUrlInFireabseRealTimeDatabase(downloadUri);

                    // Toast.makeText(SettingsActivity.this, "Url is obtained", Toast.LENGTH_SHORT).show();
//                            Glide.with(getApplicationContext())
//                                    .load(downloadUri)
//                                    .into(profileCircularImageView);
                    mProgressDialog.dismiss();
                } else {
                    // Handle failures
                    // ...
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error obtaining url", Toast.LENGTH_SHORT).show();
                }
            });


        });

    }

    private void uploadImageUrlInFireabseRealTimeDatabase(final Uri downloadUri) {
        DatabaseReference curruntUserDataNode = rootRef.child("users")
                .child("" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        curruntUserDataNode.child("profile_image_uri").setValue(downloadUri.toString())
                .addOnCompleteListener(task -> {
                    setImageUriToAccountData(downloadUri);
                });

    }

    private void setImageUriToAccountData(Uri downloadUri) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri).build();
        if (currentUser != null) {
            currentUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(task -> {
                if (ProfileActivity.this != null) {
                    if (mProgressDialog != null && mProgressDialog.isShowing())//null senario can only accur here because this code can run even when the acivity is dead
                        mProgressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "image uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "profile updated failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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

    private void showEditDeliveryAreasDialog() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        EditCitiesFragment dialogFragmentPrev = (EditCitiesFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_EDIT_CITIES);
        if (dialogFragmentPrev != null) {
            fragmentTransaction.remove(dialogFragmentPrev);
        }
        EditCitiesFragment dialogFragment = EditCitiesFragment.getInmstance();
        dialogFragment.setCallback(this);
        dialogFragment.show(fragmentTransaction, DIALOG_EDIT_CITIES);
    }

    private void showDialogforEditLocation() {
        new AlertDialog.Builder(this)
                .setTitle("Edit Location")
                .setMessage("Specify your location on map, Tap Ok to continue..")
                .setPositiveButton("OK", (dialog, which) -> sendToPickLocationActivity()).setNegativeButton("CANCEL", null).show();
    }


    private void sendToPickLocationActivity() {
        Intent intent = new Intent(this, PickLocationMapsActivity.class);
        if (latLng != null) {
            intent.putExtra("lat", latLng.latitude);
            intent.putExtra("lng", latLng.longitude);
        }
        startActivityForResult(intent, RC_SET_DELIVERY_LOCATION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SET_DELIVERY_LOCATION) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra("lat", 0);
                double lng = data.getDoubleExtra("lng", 0);
                String address = data.getStringExtra("address");
                if (lat == 0 && lng == 0) {
                    Toast.makeText(this, "seems like something went wrong ", Toast.LENGTH_SHORT).show();
                } else {
                    latLng = new LatLng(lat, lng);
                    locationAddress = address;
                    locationTextView.setTextColor(Color.DKGRAY);
                    locationTextView.setText(locationAddress);
                    sendDataToFirebase(lat, lng, address);
                }
            }

        } else if (requestCode == RC_PICK_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();


                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Baat Cheet Cropped");
                if (!dir.exists()) {//if directory is not available
                    dir.mkdir();    //then create a new directory
                }

                //File file = new File(dir, currentChildCompleteInfoModel.getUserID()+".jpg");
                //final File localFile = File.createTempFile("" + currentChildCompleteInfoModel.getUserID(), ".jpg", dir);
                File localFile = new File(dir, "" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");


                UCrop.of(imageUri, Uri.fromFile(localFile))
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(960, 960)
                        .start(this);


            } else {
                mProgressDialog.dismiss();
                Toast.makeText(this, "Upload Cancelled", Toast.LENGTH_SHORT).show();

            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);

                Bitmap selectedBitmap = BitmapFactory.decodeFile(resultUri.getPath());
                uploadImageInFirebaseCloudStorage(selectedBitmap);

                profileCircularImageView.setImageBitmap(selectedBitmap);

                Toast.makeText(this, "Result is generated", Toast.LENGTH_SHORT).show();

            } else {
                mProgressDialog.dismiss();
                final Throwable cropError = UCrop.getError(data);
                Toast.makeText(this, "operation unsuccessfull" + cropError.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("errorCrop: " + cropError.getMessage());
            }
        } else if (requestCode == RC_SELECT_TRANSPORTER) {
            if (resultCode == RESULT_OK) {
                String transporterId = data.getStringExtra(getResources().getString(R.string.transporter_id_key));
                String name = data.getStringExtra(getResources().getString(R.string.transporter_name_key));
                String number = data.getStringExtra(getResources().getString(R.string.transporter_number_key));
                updateTransporter(transporterId, name, number);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }


    private void updateTransporter(String id, String name, String number) {
        HashMap<String, Object> transporterMap = new HashMap<>();
        transporterMap.put("transporter_id", id);
        transporterMap.put("transporter_name", name);
        transporterMap.put("transporter_number", number);
        rootRef.child("clients").child(myId)//provider id that is curruntly changing the transporter
                .child(userId)
                .updateChildren(transporterMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transporterID = id;
                        transporterName = name;
                        transporterNumber = number;
                        transporterNameTextView.setText(transporterName);
                        transporterNumberTextView.setText(transporterNumber);
                    }
                });
    }

    private void sendDataToFirebase(double lat, double lng, String address) {
        HashMap<String, Object> locationMap = new HashMap<>();
        locationMap.put("lat", "" + lat);
        locationMap.put("lng", "" + lng);
        locationMap.put("address", address);

        rootRef.child("users").child(userId).child("location").setValue(locationMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", lat + "");
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", lng + "");
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "address", address);
                        Toast.makeText(getApplicationContext(), "Delivery Location set Successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "unable to set Delivery Location", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadDataFromFriebase() {
        initializeDialog();
        this.mProgressDialog.show();


        rootRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ProfileActivity.this != null) {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChild("profile_image_uri")) {
                            profileImageUri = dataSnapshot.child("profile_image_uri").getValue(String.class);
                            Glide.with(getApplicationContext())
                                    .load(profileImageUri)
                                    .into(profileCircularImageView);
                        }
                        name = dataSnapshot.child("name").getValue(String.class);
                        nameTextView.setText(name);
                        number = dataSnapshot.child("number").getValue(String.class);
                        numberTextView.setText(number);
                        type = dataSnapshot.child("type").getValue(String.class);
                        typeTextView.setText(type);

                        if (dataSnapshot.hasChild("rating")) {
                            float overAllRating = dataSnapshot.child("rating").child("over_all").getValue(Float.class);
                            float numberOfRating = dataSnapshot.child("rating").child("total_number").getValue(Float.class);
                            averageRatings = overAllRating / numberOfRating;
                            ratingBar.setRating(averageRatings);
                        }


                        if (type.equals(getResources().getString(R.string.provider))) {
                            deliveryAreasContainerLinearLayout.setVisibility(View.VISIBLE);
                            if (dataSnapshot.hasChild("cities")) {
                                if (deliveryAreasTextView.getVisibility() == View.VISIBLE) {
                                    deliveryAreasTextView.setVisibility(View.GONE);
                                    deliveryAreasChipGroup.setVisibility(View.VISIBLE);
                                }
                                for (DataSnapshot countrySnap : dataSnapshot.child("cities").getChildren()) {
                                    String countryName = countrySnap.getKey();
                                    for (DataSnapshot citySnap : countrySnap.getChildren()) {
                                        String cityName = citySnap.getKey();
                                        CityModel cityModel = new CityModel(countryName, cityName);
                                        cityModelArrayList.add(cityModel);
                                    }
                                }

                                deliveryAreasChipGroup.removeAllViews();

                                for (CityModel cityModel : cityModelArrayList) {
                                    Chip chip = new Chip(ProfileActivity.this);
                                    ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(ProfileActivity.this,
                                            null,
                                            0,
                                            R.style.Widget_MaterialComponents_Chip_Action);
                                    chip.setChipDrawable(chipDrawable);

                                    chip.setText(String.format("%s, %s", cityModel.getCity(), cityModel.getCountry()));
                                    deliveryAreasChipGroup.addView(chip);
                                }
                            } else {
                                deliveryAreasTextView.setVisibility(View.VISIBLE);
                                deliveryAreasChipGroup.setVisibility(View.GONE);
                                deliveryAreasTextView.setText("Please Specify the delivery Areas else you will not be able to receive any requests. Tap on Edit button");
                                deliveryAreasTextView.setTextColor(Color.RED);
                            }

                        } else if (type.equals(getResources().getString(R.string.transporter))) {
                            locationContainerLinearLayout.setVisibility(View.GONE);

                        } else if (type.equals(getResources().getString(R.string.consumer))) {
//                            providerIdArray = getIntent().getStringArrayListExtra(PROVIDER_ID_ARRAY);

                            if (isOtherUser && otherUserType.equals(getResources().getString(R.string.provider))) {
                                transporterInfoLinearLayout.setVisibility(View.VISIBLE);
                                transporterNameTextView.setText("fetching data...");
                                fetchTransporterData();
                            }
                        }
                        if (dataSnapshot.hasChild("location")) {
                            double lat = Double.parseDouble(dataSnapshot.child("location").child("lat").getValue(String.class));
                            double lng = Double.parseDouble(dataSnapshot.child("location").child("lng").getValue(String.class));
                            String address = lat + "\n" + lng;
                            if (dataSnapshot.child("location").hasChild("address")) {
                                address = dataSnapshot.child("location").child("address").getValue(String.class);
                            }

                            locationTextView.setText(address);
                            locationTextView.setTextColor(Color.DKGRAY);


                            latLng = new LatLng(lat, lng);
//                            new GeoCoderAsyncTask(ProfileActivity.this) {
//                                @Override
//                                protected void onPostExecute(Address address) {
//                                    if (address != null) {
//                                        locationAddress = address.getAddressLine(0);
//                                        locationTextView.setText(locationAddress);
//                                    } else {
//                                        locationTextView.setText("problem in fetching the location");
//
//                                    }
//                                    locationTextView.setTextColor(Color.DKGRAY);
//                                    mProgressDialog.dismiss();
//                                }
//                            }.execute(latLng);
                        } else {
//                            Toast.makeText(getApplicationContext(), "location was not set", Toast.LENGTH_SHORT).show();
                            locationTextView.setText("Warning ! Location was not provided");
                            locationTextView.setTextColor(Color.RED);
                        }
                        mProgressDialog.dismiss();

                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong. Please restart the application", Toast.LENGTH_SHORT).show();
                        finish();
                        mProgressDialog.dismiss();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (ProfileActivity.this != null) {
                    Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void fetchTransporterData() {
//        if (providerIdArray == null || providerIdArray.isEmpty()) {
//            transporterInfoLinearLayout.setVisibility(View.GONE);
////            Toast.makeText(this, "No provoder is connected yet", Toast.LENGTH_SHORT).show();
//            return;
//        }
        rootRef.child("clients").child(myId)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (ProfileActivity.this != null) {
                            if (dataSnapshot.exists()) {
                                transporterID = dataSnapshot.child("transporter_id").getValue(String.class);
                                transporterName = dataSnapshot.child("transporter_name").getValue(String.class);
                                transporterNumber = dataSnapshot.child("transporter_number").getValue(String.class);
                                transporterNameTextView.setText(transporterName);
                                transporterNumberTextView.setText(transporterNumber);
                                if (isOtherUser && otherUserType.equals(getResources().getString(R.string.provider))) {
                                    editTransporterImageButton.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void makeEditableFalse() {
        editDeliverAreasImageButtons.setVisibility(View.GONE);
        editLocationImageButton.setVisibility(View.GONE);
        editPictureImageButton.setVisibility(View.GONE);

    }


    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mProgressDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    @Override
    public void onArrayListUpdated(ArrayList<CityModel> arrayList) {
        this.cityModelArrayList = arrayList;
        if (deliveryAreasTextView.getVisibility() == View.VISIBLE) {
            deliveryAreasTextView.setVisibility(View.GONE);
            deliveryAreasChipGroup.setVisibility(View.VISIBLE);
        }
        deliveryAreasTextView.setVisibility(View.GONE);

        deliveryAreasChipGroup.removeAllViews();

        for (CityModel cityModel : cityModelArrayList) {
            Chip chip = new Chip(ProfileActivity.this);
            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(ProfileActivity.this,
                    null,
                    0,
                    R.style.Widget_MaterialComponents_Chip_Action);
            chip.setChipDrawable(chipDrawable);

            chip.setText(String.format("%s, %s", cityModel.getCity(), cityModel.getCountry()));
            deliveryAreasChipGroup.addView(chip);
        }
    }

    @Override
    public void onRatingChanged(float averageRating) {
        this.averageRatings = averageRating;
        ratingBar.setRating(averageRating);
    }
}