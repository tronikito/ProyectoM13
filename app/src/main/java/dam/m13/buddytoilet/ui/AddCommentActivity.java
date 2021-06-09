package dam.m13.buddytoilet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.Comment;
import dam.m13.buddytoilet.models.User;
import dam.m13.buddytoilet.models.WCPoint;

/**
 * Activitat encarregada de publicar els comentaris dels WCPoints
 */
public class AddCommentActivity extends AppCompatActivity {

    private TextInputEditText reviewTitle;
    private TextInputEditText reviewText;
    private RatingBar reviewPoints;
    private WCPoint currentWCPoint;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        reviewPoints = findViewById(R.id.addCommentRating);
        reviewTitle = findViewById(R.id.commentReviewTitle);
        reviewText = findViewById(R.id.writtenReview);
        Button submitBtn = findViewById(R.id.addCommentSubmitBtn);

        if(retrieveIntent()) {
            submitBtn.setOnClickListener(e -> {
                publish();
                Intent i = new Intent(this, NavigationActivity.class);
                i.putExtra(getResources().getString(R.string.user_intent), currentUser);
                startActivity(i);
            });
        }
    }

    /**
     * Metode encarregat de publicar els comentaris,
     * els emmagatzema als WCPoints per aixi poder actualitzar les dades correctament,
     * despres torna a la vista del mapa
     */
    private void publish() {
        WCPoint updated = currentWCPoint;
        Comment comment = new Comment();
        comment.setUserId(currentUser.getId());
        comment.setMessage(reviewText.getText().toString());
        comment.setTitle(reviewTitle.getText().toString());
        comment.setWcpointId(currentWCPoint.getId());
        comment.setAuthor(currentUser.getUsername());
        comment.setId(comment.getCommentId(comment.getUserId()));
        comment.setRating(String.valueOf(reviewPoints.getRating()));

        updated.addComment(comment.getId());
        currentWCPoint.update(updated);
        comment.save();

        Intent i = new Intent(this, NavigationActivity.class);
        i.putExtra(getResources().getString(R.string.user_intent), currentUser);
        startActivity(i);
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
}
