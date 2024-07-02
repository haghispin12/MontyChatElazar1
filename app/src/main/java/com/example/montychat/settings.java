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
import com.example.montychat.models.User;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class settings extends AppCompatActivity {


    private static final String CHANNEL_ID = "chanel_id";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    User receiverUser;
    com.makeramen.roundedimageview.RoundedImageView profile;
    EditText name, email;
    Button update;
    ImageView back;

    PreferenceManager preferenceManager;
    private String encodedImage;
    private chat_with_user_View_Model viewModel;

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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Channel Name";
//            String description = "Channel Description";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }

        getWindow().setStatusBarColor(ContextCompat.getColor(settings.this,R.color.input_back2));

        preferenceManager = new PreferenceManager(getApplicationContext());
        viewModel = new chat_with_user_View_Model();

        String nameP = (String) getIntent().getSerializableExtra(Constants.KEY_NAME);
        String imageP = (String) getIntent().getSerializableExtra(Constants.KEY_IMAGE);
        String emailP = (String) getIntent().getSerializableExtra(Constants.KEY_EMAIL);

        if (imageP != null) {
            profile.setImageBitmap(getUserImage_R(imageP));
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


            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(settings.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
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
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

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

                    // Update conversations with new details
                    viewModel.updateAllConversationsWithNewDetails(preferenceManager);

                    showToast("Details updated successfully!");

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
//        String message = "Your profile details have been updated successfully!";
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.designer2)
//                .setContentTitle("Profile Update")
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(1, builder.build());
    }



    private Bitmap getUserImage_R(String encoded) {
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
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

