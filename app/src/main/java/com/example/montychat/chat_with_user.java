package com.example.montychat;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.montychat.ViewModels.chat_with_user_View_Model;
import com.example.montychat.adapters.chatAdapter;
import com.example.montychat.models.User;
import com.example.montychat.models.chatMessage;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class chat_with_user extends AppCompatActivity {

    private User receiverUser;
    private List<chatMessage> chatMessages;
    private chatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;

    TextView textName;
    androidx.appcompat.widget.AppCompatImageView imageBack;
    androidx.appcompat.widget.AppCompatImageView infoButton;
    androidx.appcompat.widget.AppCompatImageView imageProfile;
    androidx.recyclerview.widget.RecyclerView adapter;
    ImageButton CameraButton;
    EditText inputMessage;
    FrameLayout layoutSend;
    ProgressBar progsesBar_Chat;
    private String capturedImage;
    View view_back;
    View ViewPass;
    String imageUrl;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 1888;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri filePath;


    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/monty-chat-9bf8e/messages:send";
    private static final String SERVER_KEY = "162a74b472d90c645f7670a729bb6c7e50812e7d";

    chat_with_user_View_Model vm;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_user);

        textName = findViewById(R.id.textName_chat);
        imageBack = findViewById(R.id.image_back);
        adapter = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.input_message);
        layoutSend = findViewById(R.id.layout_send);
        progsesBar_Chat = findViewById(R.id.progsesBar_Chat);
        CameraButton = findViewById(R.id.CameraButton);
        infoButton = findViewById(R.id.image_info);
        view_back = findViewById(R.id.view_back);
        ViewPass = findViewById(R.id.ViewPass);

        getWindow().setStatusBarColor(ContextCompat.getColor(chat_with_user.this, R.color.dark));

        vm = new ViewModelProvider(this).get(chat_with_user_View_Model.class);

        setListeners();
        loadReceiverDetails();
        init();

        vm.listenMessages(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id, eventListener);
    }

    private void init() {
        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new chatAdapter(this, chatMessages);
        adapter.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    chatMessage chatMessage = new chatMessage();


                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.ImageMessageChat = documentChange.getDocument().getString(Constants.KEY_IMAGE_MESSAGE);

                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if (chatAdapter.getItemCount() == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                adapter.smoothScrollToPosition(chatMessages.size());
            }
            adapter.setVisibility(View.VISIBLE);
        }
        progsesBar_Chat.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    };

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        textName.setText(receiverUser.name);
    }

    private void setListeners() {
        layoutSend.setOnClickListener(view -> {
            vm.sendMessage(inputMessage.getText().toString(), capturedImage, receiverUser, conversionId, preferenceManager);
            inputMessage.setText("");
        });

        imageBack.setOnClickListener(v -> {

//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
            finish();
        });

        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(chat_with_user.this, showUser.class);
            intent.putExtra(Constants.KEY_USER, receiverUser);
            startActivity(intent);
        });
        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // בדיקת גישה להרשאת המצלמה
                if (ContextCompat.checkSelfPermission(chat_with_user.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    // אם יש גישה למצלמה, פתח אותה
                    openCamera();
                } else {
                    // אם אין גישה, בקש הרשאה
                    ActivityCompat.requestPermissions(chat_with_user.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            }
        });

    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open camera method
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }


    // Handle camera capture result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            uploadImageToFirebase(photo);
        }
    }


//    // Convert Bitmap to Uri
//    private Uri getImageUri(Bitmap bitmap) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
//        return Uri.parse(path);
//    }

    // Upload image to Firebase Storage
    private void uploadImageToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + fileName);

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // התמונה הועלתה בהצלחה
            Toast.makeText(chat_with_user.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            // לאחר ההעלאה, קבל את קישור התמונה
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrl = uri.toString();
                vm.sendMessage(null,uri.toString(),receiverUser,conversionId,preferenceManager);

                // כאן תוכל לעשות משהו עם ה-URL של התמונה, לדוגמה לשמור אותו במסד הנתונים של Firebase Firestore
                // או לשלוח אותו לפונקציה שתשתמש בו למשימה נוספת
            }).addOnFailureListener(exception -> {
                // טיפול בשגיאה אם נכשל בקבלת קישור התמונה
                Toast.makeText(chat_with_user.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e -> {
            // טיפול בשגיאה אם נכשלה ההעלאה
            Toast.makeText(chat_with_user.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }


}
