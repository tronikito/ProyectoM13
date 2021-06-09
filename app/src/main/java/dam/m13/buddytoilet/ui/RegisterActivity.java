package dam.m13.buddytoilet.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;
import timber.log.Timber;

public class RegisterActivity extends AppCompatActivity {
    private ObjectAnimator buttonEffect1;
    private ObjectAnimator buttonEffect2;
    public static final String PREF_FILE_NAME = "FilterOptions";
    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    private String currentPhotoPath;

    private ImageView registerImageView;
    private TextInputEditText registerFullName;
    private TextInputEditText registerEmail;
    private TextInputEditText registerPassword;
    private ProgressBar registerProgressBar;
    private ProgressBar imageProgressBar;
    private int sexChoice = 2;
    private FirebaseAuth mAuth;
    private Uri uriImageProfile;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //hooks
        mAuth = FirebaseAuth.getInstance();
        TextView loadCamera = findViewById(R.id.loadCamera);
        TextView loadImage = findViewById(R.id.loadImage);
        LinearLayout registerButton = findViewById(R.id.registerButton);
        registerImageView = findViewById(R.id.wcImageView);
        registerFullName = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerProgressBar = findViewById(R.id.registerProgressBar);
        imageProgressBar = findViewById(R.id.imageProgressBar);

        loadCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottonPushEffect(v).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        callCamera();
                    }

                });
            }
        });

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottonPushEffect(v).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        callGalery();
                    }

                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottonPushEffect(v).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        registerUser();
                    }
                });
            }
        });

        /** Button effects bindings **/

        SharedPreferences sharPrefs = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharPrefs.edit();

        Drawable backgroundNoSelected = getDrawable(getResources().getIdentifier("b_button1", "drawable", getPackageName()));
        Drawable backgroundSelected = getDrawable(getResources().getIdentifier("b_button2", "drawable", getPackageName()));

        //hooks
        String nameField = "Gender";
        int yesResource = getResources().getIdentifier("yes" + nameField, "id", getPackageName());
        TextView yes = findViewById(yesResource);
        int noResource = getResources().getIdentifier("no" + nameField, "id", getPackageName());
        TextView no = findViewById(noResource);

        //listeners
        yes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                sexChoice = 1;
                editor.putString(nameField, "true");
                yes.setBackground(backgroundSelected);
                no.setBackground(backgroundNoSelected);

                bottonPushEffect(yes);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                sexChoice = 0;
                editor.putString(nameField, "false");
                yes.setBackground(backgroundNoSelected);
                no.setBackground(backgroundSelected);

                bottonPushEffect(no);
            }
        });
    }

    private void callCamera() {
        askCameraPermission();
    }

    private void callGalery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose an image"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "No access granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,
                ".PNG",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "dam.m13.buddytoilet.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            uriImageProfile = data.getData();
            registerImageView.setImageURI(uriImageProfile);

        }

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                registerImageView.setImageURI(Uri.fromFile(f));
                Timber.d("AbsoluteURL: %s", Uri.fromFile(f));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                uriImageProfile = Uri.fromFile(f);
                mediaScanIntent.setData(uriImageProfile);
                this.sendBroadcast(mediaScanIntent);
            }
        }
    }

    /** Metode per aplicar animació als butons **/

    private AnimatorSet bottonPushEffect(View textEffect) {

        AnimatorSet animationSet = new AnimatorSet();
        float valueFrom = 1f;
        float valueTo = 0.9f;
        buttonEffect1 = ObjectAnimator.ofFloat(textEffect, "scaleX", valueFrom, valueTo);
        buttonEffect2 = ObjectAnimator.ofFloat(textEffect, "scaleY", valueFrom, valueTo);
        buttonEffect1.setDuration(250);
        buttonEffect2.setDuration(250);
        animationSet.playTogether(buttonEffect1, buttonEffect2);
        animationSet.start();

        animationSet = new AnimatorSet();
        valueFrom = 0.9f;
        valueTo = 1f;
        buttonEffect1 = ObjectAnimator.ofFloat(textEffect, "scaleX", valueFrom, valueTo);
        buttonEffect2 = ObjectAnimator.ofFloat(textEffect, "scaleY", valueFrom, valueTo);
        buttonEffect1.setStartDelay(250);
        buttonEffect1.setDuration(250);
        buttonEffect2.setStartDelay(250);
        buttonEffect2.setDuration(250);
        animationSet.playTogether(buttonEffect1, buttonEffect2);
        animationSet.start();

        return animationSet;
    }

    private void registerUser() {

        String fullName = registerFullName.getText().toString().trim();
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();

        int passwordMinLength = 8;

        // check data fields content
        if (fullName.isEmpty()) {
            registerFullName.setError("Full name is required"); //works like a tooltip, indicating an error message
            registerFullName.requestFocus(); //set the focus on this field
            return;
        }

        if (email.isEmpty()) {
            registerEmail.setError("Email is required"); //works like a tooltip, indicating an error message
            registerEmail.requestFocus(); //set the focus on this field
            return;
        }

        if (!email.matches("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) //pattern checks: <username>@<mail-server>.<mail-servertype | server-location>
        {
            registerEmail.setError("Valid email is required (username@server.[a-z]{2,64})"); //works like a tooltip, indicating an error message
            registerEmail.requestFocus(); //set the focus on this field
            return;
        }
        if (password.isEmpty()) {
            registerPassword.setError("Password is required"); //works like a tooltip, indicating an error message
            registerPassword.requestFocus(); //set the focus on this field
            return;
        }
        if (password.length() < passwordMinLength) {
            registerPassword.setError("Password requires at least " + String.valueOf(passwordMinLength) + " characters"); //works like a tooltip, indicating an error message
            registerPassword.requestFocus(); //set the focus on this field
            return;
        }

        if (sexChoice > 1) {
            Toast.makeText(RegisterActivity.this, "Register process FAILED Select Sex Choice", Toast.LENGTH_SHORT).show();
            return;
        }

        registerProgressBar.setVisibility(View.VISIBLE);
        continueRegistration(email, password, fullName);

    }

    private void continueRegistration(String email, String password, String fullName) {
        registerProgressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Timber.d(fullName + ":" + email + ":" + password + ":" + sexChoice + ":" + "");
                        if (task.isSuccessful()) {

                            FirebaseUser firebaseUserUser = mAuth.getCurrentUser();
                            User newUser = new User(firebaseUserUser.getUid(), fullName, email, password, sexChoice, "");

                            if (uriImageProfile != null) {
                                imageProgressBar.setVisibility(View.VISIBLE);
                                uploadImageProfile(uriImageProfile, firebaseUserUser, newUser);
                            } else {
                                registerUserData(firebaseUserUser, newUser);
                            }
                        } else {

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(RegisterActivity.this, "User's email '" + email + "' already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Register process FAILED", Toast.LENGTH_SHORT).show();
                            }
                            registerProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
        registerProgressBar.setVisibility(View.GONE);
    }

    private void uploadImageProfile(Uri contentUri, FirebaseUser firebaseUser, User newUser) {

        registerImageView.setImageURI(contentUri);
        registerImageView.setDrawingCacheEnabled(true);
        registerImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) registerImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataImage = baos.toByteArray();
        imageProgressBar.setVisibility(View.VISIBLE);

        StorageReference imageStorageRef = storageRef.child("profile_images/" + registerEmail.getText().toString().replace(".", "") + "/profileImage.PNG");
        UploadTask uploadTask = imageStorageRef.putBytes(dataImage);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                registerImageView.setImageResource(R.drawable.p_profileimage);
                registerUserData(firebaseUser, newUser);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageProgressBar.setVisibility(View.INVISIBLE);

                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {

                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                String userID = Objects.requireNonNull(firebaseUser.getEmail()).replace(".", "");
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images/");
                                storageReference = storageReference.child(userID);

                                storageReference.child("profileImage.PNG").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        newUser.setProfileImageUrl(uri.toString());
                                        registerImageView.setImageURI(uri);
                                        registerUserData(firebaseUser, newUser);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        registerImageView.setBackgroundResource(R.drawable.p_profileimage);
                                        registerUserData(firebaseUser, newUser);
                                    }
                                });
                            }
                        });
            }
        });
    }

    private void registerUserData(FirebaseUser firebaseUser, User newUser) {

        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseUser.sendEmailVerification();
                            Toast.makeText(RegisterActivity.this, "Link sent to email '" + firebaseUser.getEmail() + "'", Toast.LENGTH_SHORT).show();

                            backToLoginIntentAfterRegistration(newUser);

                        } else {
                            Toast.makeText(RegisterActivity.this, "Register process FAILED", Toast.LENGTH_SHORT).show();
                        }
                        registerProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void backToLoginIntentAfterRegistration(User newUser) {
        registerProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("user", newUser); // send the email to Login layout and load it
        startActivity(intent);
    }
}