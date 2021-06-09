package dam.m13.buddytoilet.models;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Comment {
    private String id;
    private String userId;
    private String wcpointId;
    private String formattedDate;
    private String title;
    private String message;
    private String rating;
    private String author;
    private final Date date;
    private FirebaseDatabase db;

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", wcpointId='" + wcpointId + '\'' +
                ", formattedDate='" + formattedDate + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", rating='" + rating + '\'' +
                ", author='" + author + '\'' +
                ", db=" + db +
                '}';
    }


    public Comment(){
        date = Calendar.getInstance().getTime();
        initialize();
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Comment(String userId, String wcpointId, String title, String message, String rating) {
        date = Calendar.getInstance().getTime();
        this.userId = userId;
        this.wcpointId = wcpointId;
        this.title = title;
        this.message = message;
        this.rating = rating;
        initialize();
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getWcpointId() { return wcpointId; }

    public void setWcpointId(String wcpointId) { this.wcpointId = wcpointId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    /**
     * Desa el comentari a la base de dades
     */
    public  void save() {
        db = FirebaseDatabase.getInstance();
        db.getReference().child("WCComments").child(this.id).setValue(this)
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    /**
     * Actualitza el comentari a la base de dades
     */
    public void update(Comment updated) {
        db = FirebaseDatabase.getInstance();
        final HashMap<String, Object> update = new HashMap<>();
        update.put(this.getId(), updated);
        db.getReference().child("WCComents").updateChildren(update)
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

   /**
    * Inicialitza els valors de data ( de quan es fa el comentari)
    * i crea un id de comentari en funcio del seu valor
    */
    @SuppressLint("SimpleDateFormat")
    private void initialize() {
        this.formattedDate = new SimpleDateFormat("dd-MM-YYYY hh:mm:ss").format(date);
        this.id = this.userId + new SimpleDateFormat("ddMMyyyyHHmmss").format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public String getCommentId(String userId) {
        return userId + new SimpleDateFormat("ddMMyyyyHHmmss").format(date);
    }
}
