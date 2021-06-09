package dam.m13.buddytoilet.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;
import dam.m13.buddytoilet.models.WCPoint;
import dam.m13.buddytoilet.ui.WCPointActivity;

public class UserWCPointAdapter extends RecyclerView.Adapter<UserWCPointAdapter.MyViewHolder> {
    private ArrayList<WCPoint> wcPointList;
    private Context context;
    private User user;

    public UserWCPointAdapter(ArrayList<WCPoint> wcPointList, Context context, User user) {
        this.context = context;
        this.wcPointList = wcPointList;
        this.user = user;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.review_row, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final WCPoint currentWCPoint = wcPointList.get(position);
        if(currentWCPoint != null) { 
            /**
             * Fa una cerca de tots els comentaris que conte el WCPoint actual, 
             * filtra els valors dels ratings, i els afegeix en un llistat de valors,
             * Despres mitjantçant aquests valors calcula la mitjana i la carrega al ratingBar
             */
            Iterator<String> iteratorId = currentWCPoint.getCommentList().iterator();
            List<Float> values = new ArrayList<>();
            while(iteratorId.hasNext()) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("WCComments");
                databaseReference.child(iteratorId.next()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("rating").getValue() != null) {
                            values.add(Float.parseFloat(snapshot.child("rating").getValue() + ""));
                        }

                        if(!iteratorId.hasNext()) {
                            if(!values.isEmpty()) {
                                final float valorF = values.stream().reduce((total, accumulator) -> total + accumulator).get();
                                holder.wcRating.setRating(valorF / currentWCPoint.getCommentList().size());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }

        Picasso.get().load(currentWCPoint.getPlaceImageUrl()).fit().centerCrop().into(holder.wcImage);
        holder.wcName.setText(currentWCPoint.getName());
        holder.wcLatitud.setText("lat:  " + currentWCPoint.getLat());
        holder.wcLongitud.setText("lng: " + currentWCPoint.getLng());
        holder.layoutRow.setOnClickListener(e -> {
            Intent intent = new Intent(context, WCPointActivity.class);
            intent.putExtra(context.getResources().getString(R.string.wcpoint_intent), currentWCPoint);
            intent.putExtra(context.getResources().getString(R.string.user_intent), user);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return wcPointList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView wcImage;
        private TextView wcName;
        private TextView wcLatitud;
        private TextView wcLongitud;
        private RatingBar wcRating;
        private ConstraintLayout layoutRow;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            wcImage = itemView.findViewById(R.id.wcImage);
            wcName = itemView.findViewById(R.id.wcName);
            wcLatitud = itemView.findViewById(R.id.wcLatitud);
            wcLongitud = itemView.findViewById(R.id.wcLongitud);
            wcRating = itemView.findViewById(R.id.wcRating);
            layoutRow = itemView.findViewById(R.id.rowLayout);
        }
    }
}
