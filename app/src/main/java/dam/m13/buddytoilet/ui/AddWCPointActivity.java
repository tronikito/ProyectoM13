package dam.m13.buddytoilet.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.List;

import java.util.Date;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;
import dam.m13.buddytoilet.models.WCPoint;
import timber.log.Timber;

/**
 * Activitat encarregada de crear WCPoints
 */
public class AddWCPointActivity extends AppCompatActivity {

    private WCPoint currentWCPoint;
    private User currentUser;
    private TextInputEditText wcpointName;
    private ImageView wcImageView;
    private Uri uriImageProfile = null;
    private String currentPhotoPath;

    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    @SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wcpoint);

        currentWCPoint = new WCPoint();
        currentUser = (User) getIntent().getExtras().get(getResources().getString(R.string.user_intent));
        LinearLayout submitBtn = findViewById(R.id.submitBtn);
        TextView loadCamera = findViewById(R.id.loadCamera);
        TextView loadImage = findViewById(R.id.loadImage);
        wcImageView = findViewById(R.id.wcImageView);
        wcpointName = findViewById(R.id.wcpointNameEditText);
        List<String> listFields = Arrays.asList(getResources().getStringArray(R.array.filter_values));

        if(retrieveIntent()) {
            loadTitle();
            loadCamera.setOnClickListener(e -> {
                bottonPushEffect(e).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        callCamera();
                    }
                });
            });

            loadImage.setOnClickListener(e -> {
                bottonPushEffect(e).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        callGalery();
                    }
                });
            });

            submitBtn.setOnClickListener(e -> {
                if(currentUser.isAdmin()) {
                    registerWCPoint();
                } else {
                    requestWCPoint();
                }
                Intent intent = new Intent(this, NavigationActivity.class);
                startActivity(intent);
            });

            listFields.forEach(field -> {
                final Drawable notSelected = getDrawable(getResources().getIdentifier("b_button1", "drawable", getPackageName()));
                final Drawable selected = getDrawable(getResources().getIdentifier("b_button2", "drawable", getPackageName()));
                final TextView selectBtn = findViewById(getResources().getIdentifier("yes" + field, "id", getPackageName()));
                final TextView deselectBtn = findViewById(getResources().getIdentifier("no" + field, "id", getPackageName()));
                selectBtn.setOnClickListener(e -> {
                    selectBtn.setBackground(selected);
                    deselectBtn.setBackground(notSelected);
                    bottonPushEffect(selectBtn);
                    if(field.equalsIgnoreCase(listFields.get(0))) currentWCPoint.setFree(true);
                    if(field.equalsIgnoreCase(listFields.get(1))) currentWCPoint.setUnisex(true);
                    if(field.equalsIgnoreCase(listFields.get(2))) currentWCPoint.setAdapted(true);
                    if(field.equalsIgnoreCase(listFields.get(3))) currentWCPoint.setDiaperChanger(true);
                });
                deselectBtn.setOnClickListener(e -> {
                    deselectBtn.setBackground(selected);
                    selectBtn.setBackground(notSelected);
                    bottonPushEffect(deselectBtn);
                    if(field.equalsIgnoreCase(listFields.get(0))) currentWCPoint.setFree(false);
                    if(field.equalsIgnoreCase(listFields.get(1))) currentWCPoint.setUnisex(false);
                    if(field.equalsIgnoreCase(listFields.get(2))) currentWCPoint.setAdapted(false);
                    if(field.equalsIgnoreCase(listFields.get(3))) currentWCPoint.setDiaperChanger(false);
                });
            });
        }
    }

    private void callGalery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose an image"), GALLERY_REQUEST_CODE);
    }

    private AnimatorSet bottonPushEffect(View buttonEffect) {
        AnimatorSet animationSet = new AnimatorSet();
        float valueFrom = 1f;
        float valueTo = 0.9f;
        ObjectAnimator buttonEffect1 = ObjectAnimator.ofFloat(buttonEffect, "scaleX", valueFrom, valueTo);
        ObjectAnimator buttonEffect2 = ObjectAnimator.ofFloat(buttonEffect, "scaleY", valueFrom, valueTo);
        buttonEffect1.setDuration(250);
        buttonEffect2.setDuration(250);
        animationSet.playTogether(buttonEffect1, buttonEffect2);
        animationSet.start();
        animationSet = new AnimatorSet();
        valueFrom = 0.9f;
        valueTo = 1f;
        buttonEffect1 = ObjectAnimator.ofFloat(buttonEffect, "scaleX", valueFrom, valueTo);
        buttonEffect2 = ObjectAnimator.ofFloat(buttonEffect, "scaleY", valueFrom, valueTo);
        buttonEffect1.setStartDelay(250);
        buttonEffect1.setDuration(250);
        buttonEffect2.setStartDelay(250);
        buttonEffect2.setDuration(250);
        animationSet.playTogether(buttonEffect1, buttonEffect2);
        animationSet.start();
        return animationSet;
    }

    private void callCamera() {
        askCameraPermission();
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
            wcImageView.setImageURI(uriImageProfile);
        }

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                wcImageView.setImageURI(Uri.fromFile(f));
                Timber.d("AbsoluteURL: " + Uri.fromFile(f));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                uriImageProfile = Uri.fromFile(f);
                mediaScanIntent.setData(uriImageProfile);
                this.sendBroadcast(mediaScanIntent);
            }
        }
    }

    private void uploadImageWC(Uri contentUri) {
        wcImageView.setImageURI(contentUri);
        wcImageView.setDrawingCacheEnabled(true);
        wcImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) wcImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataImage = baos.toByteArray();

        StorageReference imageStorageRef = storageRef.child("wc_images/" +  currentWCPoint.getId() + ".PNG");
        UploadTask uploadTask = imageStorageRef.putBytes(dataImage);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                wcImageView.setImageResource(R.drawable.p_wc);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {

                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                StorageReference imageStorageRef = storageRef.child("wc_images/" + currentWCPoint.getId() + ".PNG");
                                imageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                    @Override
                                    public void onSuccess(Uri uri) {
                                        currentWCPoint.setPlaceImageUrl(uri.toString());
                                        wcImageView.setImageURI(uri);
                                        currentUser.getMyWCPoints().add(currentWCPoint.getId());
                                        currentWCPoint.save();
                                        currentUser.save();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {

                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        wcImageView.setBackgroundResource(R.drawable.p_wc);
                                        currentWCPoint.save();
                                    }
                                });
                            }
                        });
            }
        });
    }

    /**
     * Metode per desar WCPoints (admin)
     */
    public void registerWCPoint() {
        currentWCPoint.setName(wcpointName.getText().toString());
        if(currentUser.isAdmin()){
            currentWCPoint.setValidated(true);
        }

        if (uriImageProfile != null) {
            uploadImageWC(uriImageProfile);
        }
        else {
            currentUser.getMyWCPoints().add(currentWCPoint.getId());
            currentUser.save();
            currentWCPoint.save();
        }
    }

    /**
     * Metode per fer solicitud de WCPoint (User)
     */
    public void requestWCPoint() {
        if (uriImageProfile != null) {
            uploadImageWC(uriImageProfile);
        }
        else {
            currentUser.getMyWCPoints().add(currentWCPoint.getId());
            currentUser.save();
            currentWCPoint.save();
        }

    }

    /**
     * Metode per carregar les dades dels intents, retorna false en cas de no carregar-les
     */
    private boolean retrieveIntent() {
        Intent infoIntent = getIntent();
        if(infoIntent.hasExtra(getResources().getString(R.string.user_intent)))
            currentUser = (User) infoIntent.getSerializableExtra(getResources().getString(R.string.user_intent));

        if(infoIntent.hasExtra(getResources().getString(R.string.wcpoint_intent)))
            currentWCPoint = (WCPoint) infoIntent.getSerializableExtra(getResources().getString(R.string.wcpoint_intent));
        return currentWCPoint != null && currentUser != null;
    }

    /**
     * Metode per carregar el titol de l'Activitat en funcio de si el usuari es admin
     * si es admin el titol sera Add WCPoint 
     * si es un usuari comu sera WCPoint Request 
     */
    private void loadTitle() {
        TextView title = findViewById(R.id.addWCPointTitle);
        title.setText(currentUser.isAdmin()
                ? getResources().getString(R.string.add_wcpoint_title_admin)
                : getResources().getString(R.string.add_wcpoint_title)
        );
    }

}
