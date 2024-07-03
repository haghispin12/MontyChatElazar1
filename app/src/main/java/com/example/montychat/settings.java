package com.example.montychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.montychat.ViewModels.chat_with_user_View_Model;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class settings extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    com.makeramen.roundedimageview.RoundedImageView profile;
    EditText name, email;
    Button update;
    ImageView back;

    PreferenceManager preferenceManager;
    private String encodedImage;
    chat_with_user_View_Model chatWithUserViewModel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profile = findViewById(R.id.imageProfile_S);
        name = findViewById(R.id.edit_user_name_s);
        email = findViewById(R.id.edit_user_email_s);
        update = findViewById(R.id.button_up_date_s);
        back = findViewById(R.id.backS);

        getWindow().setStatusBarColor(ContextCompat.getColor(settings.this, R.color.input_back2));

        preferenceManager = new PreferenceManager(getApplicationContext());
        chatWithUserViewModel = new chat_with_user_View_Model();

        String nameP = getIntent().getStringExtra(Constants.KEY_NAME);
        String imageP = getIntent().getStringExtra(Constants.KEY_IMAGE);
        String emailP = getIntent().getStringExtra(Constants.KEY_EMAIL);

        if (imageP != null) {
            String imageUrl = preferenceManager.getString(Constants.KEY_IMAGE);
            Picasso.get().load(imageUrl).into(profile);
            encodedImage = imageP;
        }
        if (nameP != null) {
            name.setText(nameP);
        }
        if (emailP != null) {
            email.setText(emailP);
        }

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDetails();
                chatWithUserViewModel.updateAllConversationsWithNewDetails(preferenceManager);
            }
        });

        back.setOnClickListener(v -> {
            Intent intent = new Intent(settings.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            profile.setImageBitmap(bitmap);
                            uploadImageToFirebaseStorage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        StorageReference imageRef = storageReference.child("profile_images/" + userId + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            encodedImage = uri.toString();
            showToast("Image uploaded successfully!");
            preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
        })).addOnFailureListener(e -> showToast("Failed to upload image: " + e.getMessage()));
    }

    private void updateUserDetails() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        boolean emailExists = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.getId().equals(userId)) {
                                emailExists = true;
                                break;
                            }
                        }
                        if (emailExists) {
                            showToast("Email already in use!");
                        } else {
                            updateUserInDatabase(database, userId);
                        }
                    } else {
                        updateUserInDatabase(database, userId);
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void updateUserInDatabase(FirebaseFirestore database, String userId) {
        HashMap<String, Object> updatedUser = new HashMap<>();
        updatedUser.put(Constants.KEY_NAME, name.getText().toString());
        updatedUser.put(Constants.KEY_EMAIL, email.getText().toString());
        updatedUser.put(Constants.KEY_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .update(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.putString(Constants.KEY_NAME, name.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, email.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    showToast("Details updated successfully!");

                     // Update all conversations

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                    } else {
                        showUpdateNotification();
                    }

                    Intent intent = new Intent(settings.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(exception -> showToast(exception.getMessage()));
    }


    private void showUpdateNotification() {
        // Notification code goes here
    }

    private Bitmap getUserImageFromEncodedString(String encoded) {
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showUpdateNotification();
            } else {
                showToast("Notification permission denied. Unable to show update notification.");
            }
        }
    }
}
