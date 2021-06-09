package dam.m13.buddytoilet.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;

public class IntroActivity extends AppCompatActivity {

    private ProgressBar loginProgressBar;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    public static final String PREF_FILE_NAME = "LoginOptions";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        loginProgressBar = findViewById(R.id.loginProgressBar);
        mAuth = FirebaseAuth.getInstance();
        loginProgressBar.setVisibility(View.VISIBLE);
        sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        View view = findViewById(R.id.layoutIntro);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        }, 3000);
    }

    public void initialize() {
        if (sharedPreferences.contains("email") && sharedPreferences.contains("password")) {
            String email = sharedPreferences.getString("email", null);
            String password = sharedPreferences.getString("password", null);
            loginWithCredentials(email, password);
        }
        else {
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        }
    }

    private void loginWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser loginUser = mAuth.getCurrentUser();
                    if (loginUser.isEmailVerified()) {
                        cleanUserDataFromSharedPreferences(sharedPreferences);
                        savedUserDataIntoSharedPreferences(email, password);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference = databaseReference.child(mAuth.getUid());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User userProfile = snapshot.getValue(User.class);
                                Intent i = new Intent(IntroActivity.this, NavigationActivity.class);
                                i.putExtra(getResources().getString(R.string.user_intent), userProfile);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(IntroActivity.this, "User '" + email + "' NotFound IN DATABASE", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        Toast.makeText(IntroActivity.this, "User '" + email + "' NOT VERIFIED", Toast.LENGTH_SHORT).show();
                    }
                }
                loginProgressBar.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(IntroActivity.this, "Login with '" + email + "' FAILED", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            }
        });
    }

    private void savedUserDataIntoSharedPreferences(String email, String password) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("email", email);
        sharedPreferencesEditor.putString("password", password);
        sharedPreferencesEditor.apply();
    }

    public static void cleanUserDataFromSharedPreferences(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.remove("email");
        sharedPreferencesEditor.remove("password");
        sharedPreferencesEditor.apply();
    }
}