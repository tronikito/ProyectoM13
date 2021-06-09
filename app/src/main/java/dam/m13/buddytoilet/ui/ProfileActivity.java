package dam.m13.buddytoilet.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dam.m13.buddytoilet.Adapter.UserWCPointAdapter;
import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;
import dam.m13.buddytoilet.models.WCPoint;

/**
 * Activitat encarregada de carregar les dades del usuari
 */
public class ProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView profileImage;
    private ArrayList<WCPoint> reviewList;
    private TextView textEmail;
    private TextView textUser;
    private TextView deleteAccount;
    private ObjectAnimator buttonEffect1;
    private ObjectAnimator buttonEffect2;
    private LinearLayout noEntry;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private ArrayList<WCPoint> listWCPoint = new ArrayList<>();
    private DatabaseReference databaseReference;
    private UserWCPointAdapter myAdapter;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //hooks
        noEntry = findViewById(R.id.noEntry);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        textEmail = findViewById(R.id.email);
        textUser = findViewById(R.id.username);
        deleteAccount = findViewById(R.id.deleteAccount);
        profileImage = findViewById(R.id.wcImageView);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        recyclerView = findViewById(R.id.recyclerView);
        reviewList = new ArrayList<>();


        if(retrieveIntent()) {
            myAdapter = new UserWCPointAdapter(listWCPoint, ProfileActivity.this, currentUser);
            recyclerView.setAdapter(myAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            if (firebaseUser != null) {
                getProfileImage();
                getProfileInfo();
                getWCinfo();
            }

            deleteAccount.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    bottonPushEffect(deleteAccount).addListener(new AnimatorListenerAdapter() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            deleteUser(firebaseUser);
                        }
                    });
                }
            });
        }

    }

    private void deleteUser(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            deleteUserFromRealtimeDB(firebaseUser); // delete user from Realtime DB
            firebaseUser.delete() // delete user from Authentication
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        }
                    });
        }
    }

    private void deleteUserFromRealtimeDB(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            // Read from the database
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(mAuth.getUid()).removeValue() // get the current userID and try to remove it
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Account delete FAILED", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /** Metode per aplicar animació als butons **/

    private AnimatorSet bottonPushEffect(View buttonEffect) {
        AnimatorSet animationSet = new AnimatorSet();
        float valueFrom = 1f;
        float valueTo = 0.9f;
        buttonEffect1 = ObjectAnimator.ofFloat(buttonEffect, "scaleX", valueFrom, valueTo);
        buttonEffect2 = ObjectAnimator.ofFloat(buttonEffect, "scaleY", valueFrom, valueTo);
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

    /**
     * Metode que recupera i carrega la foto de perfil del usuari, en cas de 
     * no tenir-ne cap de definida posa una per defecte
     */
    private void getProfileImage() {
        final String profileUrl = currentUser.getProfileImageUrl();
            Picasso.get().load(profileUrl.length() < 1 ? "empty" : profileUrl)
                    .placeholder(R.drawable.p_profileimage)
                    .fit().centerCrop()
                    .into(profileImage);
    }

    private void getProfileInfo() {
            textUser.setText(currentUser.getUsername());
            textEmail.setText(currentUser.getEmail());
    }

    private void getWCinfo() {
        recyclerView.setVisibility(View.GONE);
        for (String idWC : currentUser.getMyWCPoints()) {
            databaseReference = FirebaseDatabase.getInstance().getReference("WCPoints");
            databaseReference = databaseReference.child(idWC);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    noEntry.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    WCPoint wcPoint = snapshot.getValue(WCPoint.class);
                    if(wcPoint != null) {
                        listWCPoint.add(wcPoint);
                    }
                    myAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    /**
     * Metode per carregar les dades dels intents, retorna false en cas de no carregar-les
     */
    private boolean retrieveIntent() {
        Intent infoIntent = getIntent();
        if(infoIntent.hasExtra(getResources().getString(R.string.user_intent)))
            currentUser = (User) infoIntent.getSerializableExtra(getResources().getString(R.string.user_intent));
        return currentUser != null;
    }
}
