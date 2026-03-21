package lk.disontech.campusassist.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.WriterSubmissionItem;

public class WriterSubmissionAdapter extends RecyclerView.Adapter<WriterSubmissionAdapter.WriterSubmissionViewHolder> {

    public interface OnSubmissionActionListener {
        void onViewAssignmentClick(WriterSubmissionItem item);
    }

    private final Context context;
    private final List<WriterSubmissionItem> items;
    private final OnSubmissionActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public WriterSubmissionAdapter(Context context,
                                   List<WriterSubmissionItem> items,
                                   OnSubmissionActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WriterSubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_writer_submission, parent, false);
        return new WriterSubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WriterSubmissionViewHolder holder, int position) {
        WriterSubmissionItem item = items.get(position);
        String status = resolveStatusLabel(item.getAssignmentStatus());

        holder.tvSubmissionTitle.setText(safe(item.getTitle(), "Untitled assignment"));
        holder.tvSubmissionStatus.setText(status);
        bindStatusStyle(holder.tvSubmissionStatus, status);
        holder.tvSubmissionStudent.setText("Student: " + safe(item.getStudentName(), "Not available"));
        holder.tvSubmissionDate.setText("Submitted: " + formatSubmittedDate(item.getSubmittedAt()));
        holder.tvSubmissionPrice.setText("Price: " + safe(item.getPriceDisplay(), "Pending"));
        holder.btnViewAssignment.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewAssignmentClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatSubmittedDate(long submittedAt) {
        if (submittedAt <= 0L) {
            return "Not available";
        }
        return dateFormat.format(new Date(submittedAt));
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String resolveStatusLabel(String assignmentStatus) {
        if (TextUtils.isEmpty(assignmentStatus)) {
            return "Submitted";
        }
        return assignmentStatus.trim();
    }

    private void bindStatusStyle(TextView statusView, String status) {
        if (statusView == null) {
            return;
        }

        if ("Completed".equalsIgnoreCase(status)) {
            statusView.setBackgroundResource(R.drawable.bg_status_badge);
            statusView.setTextColor(Color.WHITE);
        } else if ("Pending Payment".equalsIgnoreCase(status)) {
            statusView.setBackgroundResource(R.drawable.bg_status_orange);
            statusView.setTextColor(Color.WHITE);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            statusView.setBackgroundResource(R.drawable.bg_status_blue);
            statusView.setTextColor(Color.WHITE);
        } else {
            statusView.setBackgroundResource(R.drawable.bg_status_blue);
            statusView.setTextColor(Color.WHITE);
        }
    }

    static class WriterSubmissionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSubmissionTitle;
        private final TextView tvSubmissionStatus;
        private final TextView tvSubmissionStudent;
        private final TextView tvSubmissionDate;
        private final TextView tvSubmissionPrice;
        private final MaterialButton btnViewAssignment;

        public WriterSubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubmissionTitle = itemView.findViewById(R.id.tvSubmissionTitle);
            tvSubmissionStatus = itemView.findViewById(R.id.tvSubmissionStatus);
            tvSubmissionStudent = itemView.findViewById(R.id.tvSubmissionStudent);
            tvSubmissionDate = itemView.findViewById(R.id.tvSubmissionDate);
            tvSubmissionPrice = itemView.findViewById(R.id.tvSubmissionPrice);
            btnViewAssignment = itemView.findViewById(R.id.btnViewAssignment);
        }
    }
}


