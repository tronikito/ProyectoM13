package dam.m13.buddytoilet.models;

import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable {
    private String id;
    private String email;
    private String username;
    private String password;
    private int sex;
    private String profileImageUrl;
    private boolean admin;
    private FirebaseDatabase db;

    private ArrayList<String> myWCPoints = new ArrayList<String>();

    public User(){
    };

    public User(String id, String username, String email, String password, int sex, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sex = sex;
        this.profileImageUrl = profileImageUrl;
        this.admin = false;

    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public boolean isAdmin() { return admin; }

    public void setAdmin(boolean admin) { this.admin = admin; }

    public ArrayList<String> getMyWCPoints() { return myWCPoints; }

    public void setMyWCPoints(ArrayList<String> myWCPoints) { this.myWCPoints = myWCPoints; }

    @Override
    public @NotNull String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", sex=" + sex +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", admin=" + admin +
                '}';
    }

    /**
     * Desa el usuari a la base de dades
     */
    public void save() {
        db = FirebaseDatabase.getInstance();
        db.getReference().child("Users").child(this.getId()).setValue(this)
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    /**
     * Elimina el usuari de la base de dades
     */
    public void delete() {
        db = FirebaseDatabase.getInstance();
        db.getReference().child("Users").child(this.getId()).removeValue()
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    /**
     * Actualitza les dades del usuari a la base de dades
     */
    public void update(User updated) {
        db = FirebaseDatabase.getInstance();
        final HashMap<String, Object> update = new HashMap<>();
        update.put(this.getId(), updated);
        db.getReference().child("Users").updateChildren(update)
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }
}


