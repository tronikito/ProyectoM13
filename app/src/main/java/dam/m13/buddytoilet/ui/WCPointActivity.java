package dam.m13.buddytoilet.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dam.m13.buddytoilet.Adapter.FeatureAdapter;
import dam.m13.buddytoilet.Adapter.WCPointCommentAdapter;
import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.Comment;
import dam.m13.buddytoilet.models.User;
import dam.m13.buddytoilet.models.WCPoint;

/**
 * Activitat encarregada de carregar les dades dels WCPoints
 */
public class WCPointActivity extends AppCompatActivity {
    private RatingBar wcPointRatingBar;
    private WCPointCommentAdapter reviewAdapter;
    private List<Integer> featureIcons;
    private List<Comment> comments = new ArrayList<Comment>();
    private ArrayList commentsID = new ArrayList<String>();
    private float average = 0;

    private WCPoint currentWCPoint;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wcpoint);

        ImageView wcpointImage = findViewById(R.id.wcpointImage);
        TextView wcpointTitle = findViewById(R.id.wcpointNameEditText);
        wcPointRatingBar = findViewById(R.id.wcpointRatingBar);
        RecyclerView featureRecycler = findViewById(R.id.wcpointFeatureRecycler);
        RecyclerView commentRecycler = findViewById(R.id.wcpointCommentRecycler);
        Button reviewBtn = findViewById(R.id.wcpointAddReviewBtn);

        if(retrieveIntent()) {
            loadWCPointData();

            wcpointTitle.setText(currentWCPoint.getName());
            Picasso.get()
                    .load(currentWCPoint.getPlaceImageUrl())
                    .fit()
                    .centerCrop()
                    .into(wcpointImage);

            FeatureAdapter featureAdapter = new FeatureAdapter(featureIcons, this);
            featureRecycler.setAdapter(featureAdapter);
            featureRecycler.setLayoutManager(new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false));

            commentsID = (ArrayList<String>) currentWCPoint.getCommentList();
            comments = new ArrayList<Comment>();

            reviewAdapter = new WCPointCommentAdapter(this, comments);
            commentRecycler.setAdapter(reviewAdapter);
            commentRecycler.setLayoutManager(new LinearLayoutManager(this));

            for (String idComment : currentWCPoint.getCommentList()) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("WCComments");
                databaseReference = databaseReference.child(idComment);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Comment newComment = new Comment(
                                (snapshot.child("userId").getValue() != null
                                        ? snapshot.child("userId").getValue() + ""
                                        : "username"),
                                (snapshot.child("wcpointId").getValue() != null
                                        ? snapshot.child("wcpointId").getValue() + ""
                                        : "wcpointId"),
                                (snapshot.child("title").getValue() != null
                                        ? snapshot.child("title").getValue() + ""
                                        : "title"),
                                (snapshot.child("message").getValue() != null
                                        ? snapshot.child("message").getValue() + ""
                                        : "message"),
                                (snapshot.child("rating").getValue() != null
                                        ? snapshot.child("rating").getValue() + ""
                                        : "5.0"));

                        newComment.setAuthor(currentUser.getUsername());
                        newComment.setId(newComment.getCommentId(currentUser.getId()));

                        if(snapshot.child("formattedDate").getValue() != null)
                            newComment.setFormattedDate(snapshot.child("formattedDate").getValue() + "");

                        comments.add(newComment);
                        reviewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            retrieveRating();

            reviewBtn.setOnClickListener(e -> {
                Intent i = new Intent(this, AddCommentActivity.class);
                i.putExtra(getResources().getString(R.string.user_intent), currentUser);
                i.putExtra(getResources().getString(R.string.wcpoint_intent), currentWCPoint);
                startActivity(i);
            });
        }
    }

    /**
     * Metode encarregat de carregar les dades del WCPoint en les views corresponents
     */
    private void loadWCPointData() {
        commentsID = (ArrayList) currentWCPoint.getCommentList();
        featureIcons = new ArrayList<>();
        if(currentWCPoint.isAdapted()) featureIcons.add(R.drawable.wc_adapted_signal);
        if(currentWCPoint.isFree()) featureIcons.add(R.drawable.freeicon);
        if(currentWCPoint.isUnisex()) featureIcons.add(R.drawable.wc_genders_signal);
        if(currentWCPoint.isDiaperChanger()) featureIcons.add(R.drawable.wc_diaper_changer_signal);
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
     * Metode encarregat de filtrar les valoracions dels comentaris del WCPoint per carregar-les
     * i afegir-les previament al ratingBar
     */
    private void retrieveRating() {
        Iterator<String> iterator = currentWCPoint.getCommentList().iterator();
        while(iterator.hasNext()) {
            final String commentId = iterator.next();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("WCComments");
            databaseReference = databaseReference.child(commentId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final String str = ((snapshot.child("rating").getValue() != null ? (String) snapshot.child("rating").getValue() : "5"));
                    updateTotal(Float.parseFloat(str));
                    if(!iterator.hasNext()) wcPointRatingBar.setRating(average / commentsID.size());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateTotal(float average) {
        this.average += average;
    }
}

