package dam.m13.buddytoilet.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;

import static dam.m13.buddytoilet.R.id.loginForgot;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout loginButton;
    private TextInputEditText loginEmail;
    private TextInputEditText loginPassword;
    private TextView loginForgotButton;
    private TextView loginRegisterTextView;
    private FirebaseAuth mAuth;
    private User currentUser;
    private ProgressBar loginProgressBar;
    private FirebaseAuth mFirebaseAuth;
    private String email;
    private SharedPreferences loginSharedPreferences;
    private GoogleSignInClient signInClient;
    private static final int GOOGLE_SING_IN_CODE = 10005;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        signInClient = GoogleSignIn.getClient(this, gso);

        loginSharedPreferences = getSharedPreferences(getResources().getString(R.string.login_data), Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.registerUsername);
        loginPassword = findViewById(R.id.loginPassword);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        LinearLayout btGoogle = findViewById(R.id.btGoogle);
        btGoogle.setOnClickListener(this);

        loginForgotButton = findViewById(loginForgot);
        loginForgotButton.setOnClickListener(this);

        loginRegisterTextView = findViewById(R.id.loginRegister);
        loginRegisterTextView.setOnClickListener(this);

        loginProgressBar = findViewById(R.id.loginProgressBar);

        getNewRegisteredUserData();
        if (currentUser != null) {
            loginEmail.setText(currentUser.getEmail());
        } else {
            if (loginSharedPreferences.contains("email")) {

                email = loginSharedPreferences.getString("email", null);
                String password = loginSharedPreferences.getString("password", null);

                loginEmail.setText(email);
                loginPassword.setText(password);

                loginSharedPreferences.edit().clear();
                loginSharedPreferences.edit().apply();
                if (email != null && password != null) {
                    loginWithCredentials(email, password);
                }
            }
        }
    }

    private void getNewRegisteredUserData() {
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra(getResources().getString(R.string.user_intent));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                bottonPushEffect(loginButton).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        String email = loginEmail.getText().toString().trim();
                        String password = loginPassword.getText().toString().trim();
                        userLogin(email, password);
                    }
                });
                break;
            case loginForgot:
                bottonPushEffect(loginForgotButton).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startActivity(new Intent(LoginActivity.this, ForgotActivity.class));
                    }
                });
                break;
            case R.id.loginRegister:
                bottonPushEffect(loginRegisterTextView).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    }
                });
                break;
            case R.id.btGoogle:
                bottonPushEffect(v).addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Intent sign = signInClient.getSignInIntent();
                        startActivityForResult(sign, GOOGLE_SING_IN_CODE);
                    }
                });
        }
    }

    private void userLogin(String email, String password) {
        int passwordMinLength = 8;

        if (email.isEmpty()) {
            loginEmail.setError("Email is required"); //works like a tooltip, indicating an error message
            loginEmail.requestFocus(); //set the focus on this field
            return;
        }
        if (!email.matches("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) {
            loginEmail.setError("Valid email is required (username@server.[a-z]{2,64})"); //works like a tooltip, indicating an error message
            loginEmail.requestFocus(); //set the focus on this field
            return;
        }
        if (password.isEmpty()) {
            loginPassword.setError("Password is required"); //works like a tooltip, indicating an error message
            loginPassword.requestFocus(); //set the focus on this field
            return;
        }
        if (password.length() < passwordMinLength) {
            loginPassword.setError("Password requires at least " + String.valueOf(passwordMinLength) + " characters"); //works like a tooltip, indicating an error message
            loginPassword.requestFocus(); //set the focus on this field
            return;
        }

        loginProgressBar.setVisibility(View.VISIBLE);

        loginWithCredentials(email, password);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SING_IN_CODE) {
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount signInAcc = signInTask.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAcc.getIdToken(), null);
                mFirebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(getApplicationContext(), "Your google account is connected to our aplication", Toast.LENGTH_LONG).show();

                        /** check if user exist before **/
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference = databaseReference.child(mAuth.getUid());

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                User userProfile = snapshot.getValue(User.class);
                                if (userProfile != null) {
                                    Intent i = new Intent(LoginActivity.this, NavigationActivity.class);
                                    i.putExtra(getResources().getString(R.string.user_intent), userProfile);
                                    startActivity(i);
                                } else {

                                    /** make a user database **/
                                    userProfile = new User();
                                    userProfile.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                    userProfile.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    userProfile.setProfileImageUrl(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                                    userProfile.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                    userProfile.setSex(1);
                                    userProfile.setAdmin(false);

                                    User finalUserProfile = userProfile;
                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(userProfile)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {

                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent i = new Intent(LoginActivity.this, NavigationActivity.class);
                                                        i.putExtra(getResources().getString(R.string.user_intent), finalUserProfile);
                                                        startActivity(i);
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "Register process FAILED", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    /** end **/
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "User '" + email + "' NotFound IN DATABASE", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("No conecta");
                        showAlert();
                    }
                });
            } catch (ApiException e) {
                System.out.println(e);
                showAlert();
            }
        }
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("User or password does not exist.");
        builder.setTitle("ERROR");
        builder.setPositiveButton("Accept", null);
        builder.create();
        builder.show();
    }

    private void loginWithCredentials(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser loginUser = mAuth.getCurrentUser();
                    if (loginUser.isEmailVerified()) {
                        savedUserDataIntoSharedPreferences(email, password);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference = databaseReference.child(mAuth.getUid());

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                User userProfile = snapshot.getValue(User.class);
                                Intent i = new Intent(LoginActivity.this, NavigationActivity.class);
                                i.putExtra(getResources().getString(R.string.user_intent), userProfile);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "User '" + email + "' NotFound IN DATABASE", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "User '" + email + "' NOT VERIFIED", Toast.LENGTH_SHORT).show();
                    }
                }
                loginProgressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Login FAILED - BAD PASSWORD OR USER", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savedUserDataIntoSharedPreferences(String email, String password) {
        SharedPreferences.Editor sharedPreferencesEditor = loginSharedPreferences.edit();
        sharedPreferencesEditor.putString("email", email);
        sharedPreferencesEditor.putString("password", password);
        sharedPreferencesEditor.apply();
    }

    public static void cleanUserDataFromSharedPreferences(SharedPreferences loginSharedPreferences) {
        SharedPreferences.Editor sharedPreferencesEditor = loginSharedPreferences.edit();
        sharedPreferencesEditor.remove("email");
        sharedPreferencesEditor.remove("password");
        sharedPreferencesEditor.clear();
        sharedPreferencesEditor.apply();
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
}