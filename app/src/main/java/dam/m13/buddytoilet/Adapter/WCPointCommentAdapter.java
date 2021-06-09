package dam.m13.buddytoilet.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.Comment;

/**
 * Adapter per carregar els comentaris al recyclerView del WCPoint
 */
public class WCPointCommentAdapter extends RecyclerView.Adapter<WCPointCommentAdapter.WCPointCommentViewHolder> {
    private Context context;
    private List<Comment> commentList;

    public WCPointCommentAdapter(Context context, List<Comment> commentList) {
        this.context =  context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public WCPointCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layout = LayoutInflater.from(context);
        View view = layout.inflate(R.layout.wcpoint_comment, parent, false);
        return new WCPointCommentAdapter.WCPointCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WCPointCommentViewHolder holder, int position) {
        final Comment currentComment = commentList.get(position);
        holder.username.setText(currentComment.getAuthor());
        holder.title.setText(currentComment.getTitle());
        holder.comment.setText(currentComment.getMessage());
        holder.rating.setRating(Float.parseFloat(currentComment.getRating()));
        holder.commentDate.setText(currentComment.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class WCPointCommentViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private TextView title;
        private TextView comment;
        private TextView commentDate;
        private RatingBar rating;

        public WCPointCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.commentUserName);
            title = itemView.findViewById(R.id.commentTitle);
            comment = itemView.findViewById(R.id.commentText);
            rating = itemView.findViewById(R.id.commentRating);
            commentDate = itemView.findViewById(R.id.commentDate);
        }
    }
}
