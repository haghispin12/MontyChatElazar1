package com.example.montychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;


public class Sign_Up extends AppCompatActivity {

    EditText user_name;
    EditText email;
    EditText pass;
    EditText rePass;
    Button sign_up;
    TextView sign_in;

    ProgressBar progressBar;
    com.makeramen.roundedimageview.RoundedImageView imageProfile;
    TextView textAddImage;
    FrameLayout frameLayout;
    private String encodedImage;
    private PreferenceManager preferenceManager;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        preferenceManager = new PreferenceManager(getApplicationContext());

         user_name = findViewById(R.id.inputName);
         email = findViewById(R.id.inputEmail_C);
         pass = findViewById(R.id.input_pass_C);
         rePass = findViewById(R.id.input_confirm_pass_C);
         sign_up = findViewById(R.id.Sidn_Up_Button);
         sign_in = findViewById(R.id.Sign_In_text);
         progressBar= findViewById(R.id.progresBar1);
         imageProfile = findViewById(R.id.imageProfile_sign_up);
         textAddImage = findViewById(R.id.textAddImage);
         frameLayout = findViewById(R.id.layout_Image);


         sign_up.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(isValidSignUpDetails())
                    signUp();
             }
         });
         sign_in.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Sign_Up.this,log_In.class);
                 startActivity(intent);
                 finish();
             }
         });
         frameLayout.setOnClickListener(v ->{
             Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
             intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
             pickImage.launch(intent);
         });
    }

    private void showToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
        private void signUp(){
            loading(true);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String,Object> user = new HashMap<>();
            user.put(Constants.KEY_NAME,user_name.getText().toString());
            user.put(Constants.KEY_EMAIL,email.getText().toString());
            user.put(Constants.KEY_PASSWORD,pass.getText().toString());
            user.put(Constants.KEY_IMAGE,encodedImage);
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        loading(false);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNE_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                        preferenceManager.putString(Constants.KEY_NAME,user_name.getText().toString());
                        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                        preferenceManager.putString(Constants.KEY_EMAIL,email.getText().toString());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(exception ->{
                        loading(false);
                        showToast(exception.getMessage());
                    });
        }

        private String encodeImage(Bitmap bitmap){
            int previewWidth = 150;
            int previewHeigh = bitmap.getHeight()*previewWidth/bitmap.getWidth();
            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeigh,false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
            byte [] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes,Base64.DEFAULT);
        }
        private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        if(result.getData() != null){
                            Uri imageUri = result.getData().getData();
                            try {
                                InputStream  inputStream = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                imageProfile.setImageBitmap(bitmap);
                                textAddImage.setVisibility(View.GONE);
                                encodedImage = encodeImage(bitmap);

                            }catch (FileNotFoundException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        private Boolean isValidSignUpDetails(){
            if(encodedImage == null ){
                showToast("Choose a picture!");
                return false;
            }
            else if (user_name==null){
                showToast("Enter name!");
                return false;
            }
            else if(email.getText().toString().trim().isEmpty()){
                showToast("Enter email!");
                return false;
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                showToast("Enter Valid email!");
                return false;
            }
            else if(pass.length()<6){
                showToast("password need to be longer then6 character!");
                return false;
            }
            else if (rePass == null) {
                showToast("pleas conform your password!");
                return false;

            } else if(!pass.getText().toString().equals(rePass.getText().toString())){
                showToast("the password are not the same!");
                return false;
            }
            else return true;
        }

        private void loading (Boolean is_loading){
            if(is_loading){
                sign_up.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }else{
                progressBar.setVisibility(View.INVISIBLE);
                sign_up.setVisibility(View.VISIBLE);
            }
        }

}