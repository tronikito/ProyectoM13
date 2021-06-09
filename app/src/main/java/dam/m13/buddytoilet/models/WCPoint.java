package dam.m13.buddytoilet.models;

import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WCPoint implements Serializable {
    private Double lat;
    private Double lng;
    private String id;
    private String name;
    private String placeImageUrl = "empty";
    private float averageRating = 0;
    private boolean free;
    private boolean unisex;
    private boolean adapted;
    private boolean diaperChanger;
    private boolean validated;
    private final List<String> commentList = new ArrayList<>();

    private FirebaseDatabase db;

    public WCPoint(){}

    public WCPoint(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
        this.id = createID(String.valueOf(lat),String.valueOf(lng));
    }

    public WCPoint(Double lat, Double lng, String name, String placeImageUrl, float averageRating, boolean free, boolean unisex, boolean adapted, boolean diaperChanger) {
        this.lat = lat;
        this.lng = lng;
        this.id = createID(String.valueOf(lat),String.valueOf(lng));
        this.name = name;
        this.placeImageUrl = placeImageUrl;
        this.averageRating = averageRating;
        this.free = free;
        this.unisex = unisex;
        this.adapted = adapted;
        this.diaperChanger = diaperChanger;
        this.validated = false;
    }

    /**
     * Afegeix un comentari del WCPoint i actualitza les dades a la base de dades
     */
    public void addComment(String comment) {
        WCPoint updatedWCPoint = this;
        updatedWCPoint.commentList.add(comment);
        this.update(updatedWCPoint);
    }

    /**
     * Elimina un comentari del WCPoint i actualitza les dades a la base de dades
     */
    public void deleteComment(String commentId) {
        WCPoint updatedWCPoint = this;
        updatedWCPoint.commentList.remove(commentId);
        this.update(updatedWCPoint);
    }

    /**
     * Desa un WCPoint a la base de dades
     */
    public  void save() {
        db = FirebaseDatabase.getInstance();
        db.getReference().child("WCPoints").child(this.getId()).setValue(this)
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    /**
     * Actualitza un WCPoint a la base de dades
     */
    public void update(WCPoint updated) {
        db = FirebaseDatabase.getInstance();
        final HashMap<String, Object> update = new HashMap<>();
        update.put(this.getId(), updated);
        db.getReference().child("WCPoints").updateChildren(update)
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    /**
     * Elimina un WCPoint de la base de dades
     */
    public void delete() {
        db = FirebaseDatabase.getInstance();
        db = FirebaseDatabase.getInstance();
        db.getReference().child("WCPoints").child(this.getId()).removeValue()
                .addOnCompleteListener(task -> {
                })
                .addOnFailureListener(error -> {
                });
    }

    public Double getLat() { return lat; }

    public List<String> getCommentList() {
       return this.commentList;
    }

    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }

    public void setLng(Double lng) { this.lng = lng; }

    public String getId() {
        return id;
    }

    public void setId(Double lat, Double lng) { this.id = createID(String.valueOf(lat),String.valueOf(lng)); }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceImageUrl() {
        return placeImageUrl;
    }

    public void setPlaceImageUrl(String placeImageUrl) {
        this.placeImageUrl = placeImageUrl;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public boolean isFree() { return free; }

    public void setFree(boolean free) { this.free = free; }

    public boolean isUnisex() {
        return unisex;
    }

    public void setUnisex(boolean unisex) {
        this.unisex = unisex;
    }

    public boolean isAdapted() {
        return adapted;
    }

    public void setAdapted(boolean adapted) {
        this.adapted = adapted;
    }

    public boolean isDiaperChanger() {
        return diaperChanger;
    }

    public void setDiaperChanger(boolean diaperChanger) {
        this.diaperChanger = diaperChanger;
    }

    public boolean isValidated() { return validated; }

    public void setValidated(boolean validated) { this.validated = validated; }

    public String createID(String lat, String lng){
        String id = "";
        id += lat.replace(".","");
        id += lng.replace(".","");
        return id;
    }

    @Override
    public String toString() {
        return "WCPoint{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", placeImageUrl='" + placeImageUrl + '\'' +
                ", averageRating=" + averageRating +
                ", free=" + free +
                ", unisex=" + unisex +
                ", adapted=" + adapted +
                ", diaperChanger=" + diaperChanger +
                ", validated=" + validated +
                ", commentList=" + commentList +
                ", db=" + db +
                '}';
    }
}
