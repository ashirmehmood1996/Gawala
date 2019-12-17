package com.android.example.gawala.Provider.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;


public class ProducerAddServiceActivity extends AppCompatActivity {
    private static final int RC_PICK_FROM_GALLERY = 122;
    private EditText nameEditText, descriptionEditText, typeEditText, priceEditText;
    private Button addProductButton;
    private ImageView addPictureImageView;

    private AlertDialog mProgressDialog;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_add_service);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFields();
        attachListeners();
    }

    private void initFields() {


        nameEditText = findViewById(R.id.et_add_service_name);
        descriptionEditText = findViewById(R.id.et_add_service_desc);
        priceEditText = findViewById(R.id.et_add_service_price);
        typeEditText = findViewById(R.id.et_add_service_type);
        addProductButton = findViewById(R.id.bt_add_service);
        addPictureImageView = findViewById(R.id.iv_add_service_image);
        initializeDialog();
        mBitmap = null;
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mProgressDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void attachListeners() {
        addProductButton.setOnClickListener(v -> showDialog());

        addPictureImageView.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");

            //                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
            //                String[] mimeTypes = {"image/jpeg", "image/png"};
            //                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);

            startActivityForResult(galleryIntent
                    , RC_PICK_FROM_GALLERY);
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_PICK_FROM_GALLERY) {
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
            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                mBitmap = BitmapFactory.decodeFile(resultUri.getPath());
                Glide.with(this).load(mBitmap).into(addPictureImageView);

            } else {
                final Throwable cropError = UCrop.getError(data);
                Toast.makeText(this, "operation unsuccessfull" + cropError.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("errorCrop: " + cropError.getMessage());
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImageInFireBaseCloudStorage(Bitmap bitmap, String good_id) {

        StorageReference rootStorageReference = FirebaseStorage.getInstance().getReference();
        //String fileType = getContentResolver().getType(profileImageUri);
        final StorageReference servicePicRef = rootStorageReference.child("servicesPictures/" + good_id/*+ fileType*/);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = servicePicRef.putBytes(data);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getApplicationContext(), "Upload unsuccessful \n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
        }).addOnSuccessListener(taskSnapshot -> {
            /*Task<Uri> urlTask = */
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    mProgressDialog.dismiss();
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return servicePicRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadImageUrlInFireabseRealTimeDatabase(downloadUri, good_id);
                    // Toast.makeText(SettingsActivity.this, "Url is obtained", Toast.LENGTH_SHORT).show();
//                            Glide.with(getApplicationContext())
//                                    .load(downloadUri)
//                                    .into(profileCircularImageView);
                } else {
                    // Handle failures
                    // ...
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error obtaining url", Toast.LENGTH_SHORT).show();
                }
            });


        });

    }

    private void uploadImageUrlInFireabseRealTimeDatabase(final Uri downloadUri, String good_id) {

        rootRef.child("goods")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(good_id).child("image_uri").setValue(downloadUri.toString())
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "data uploaded successfully", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                    finish();
                });

    }

    private void showDialog() {
        String name = nameEditText.getText().toString();
        String desc = descriptionEditText.getText().toString();
        String price = priceEditText.getText().toString();
        String type = typeEditText.getText().toString();


        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc)
                || TextUtils.isEmpty(price) || TextUtils.isEmpty(type)) {
            Toast.makeText(this, "plaese fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        final GoodModel goodModel = new GoodModel(name, desc, price, type,"");

        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure?")
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addNewGoodToFirebase(goodModel);

                    }
                })
                .setNegativeButton("cancel", null).show();
    }

    private void addNewGoodToFirebase(GoodModel goodModel) {
        mProgressDialog.show();
        DatabaseReference currentGoogRef = rootRef.child("goods")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .push();
        String good_id = currentGoogRef.getKey();
        goodModel.setId(good_id);
        currentGoogRef.setValue(goodModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (mBitmap != null) {
                    uploadImageInFireBaseCloudStorage(mBitmap, good_id);
                } else {
                    mProgressDialog.dismiss();
                }

            }
        });
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
