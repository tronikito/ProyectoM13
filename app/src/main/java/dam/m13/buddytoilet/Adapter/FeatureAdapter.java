package dam.m13.buddytoilet.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dam.m13.buddytoilet.R;

/**
 * Aquest adapter carrega els trets caracteristics dels WCPoints en un recyclerView,
 * Si costa diners, es unisex, esta adaptat, te estacio per canviar bolquers...
 */
public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder>{
    private Context context;
    private List<Integer> featureIcons;

    public FeatureAdapter(List<Integer> featureIcons, Context context) {
        this.featureIcons = featureIcons;
        this.context = context;
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layout = LayoutInflater.from(context);
        View view = layout.inflate(R.layout.wcpoint_feature, parent, false);
        return new FeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        holder.featureImage.setImageResource(featureIcons.get(position));
    }

    @Override
    public int getItemCount() {
        return featureIcons.size();
    }

    public class FeatureViewHolder extends RecyclerView.ViewHolder {
        private ImageView featureImage;

        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            featureImage = itemView.findViewById(R.id.wcpointFeatureIcon);
        }
    }
}
