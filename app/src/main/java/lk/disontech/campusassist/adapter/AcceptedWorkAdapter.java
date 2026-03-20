package lk.disontech.campusassist.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AcceptedWorkModel;

public class AcceptedWorkAdapter extends RecyclerView.Adapter<AcceptedWorkAdapter.AcceptedWorkViewHolder> {

    public interface MyWorkActionListener {
        void onViewUpdateClick(AcceptedWorkModel model);

        void onCancelBidClick(AcceptedWorkModel model);
    }

    private final Context context;
    private final List<AcceptedWorkModel> acceptedWorkList;
    private final MyWorkActionListener listener;

    public AcceptedWorkAdapter(Context context,
                               List<AcceptedWorkModel> acceptedWorkList,
                               MyWorkActionListener listener) {
        this.context = context;
        this.acceptedWorkList = acceptedWorkList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AcceptedWorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_accepted_work, parent, false);
        return new AcceptedWorkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AcceptedWorkViewHolder holder, int position) {
        AcceptedWorkModel model = acceptedWorkList.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvSubject.setText(model.getSubject());
        holder.tvStudent.setText("Student: " + model.getStudentName());
        holder.tvDueDate.setText("Due: " + model.getDueDate());
        holder.tvStatus.setText(model.getStatus());

        if ("In Progress".equalsIgnoreCase(model.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_orange);
        } else if ("Approved".equalsIgnoreCase(model.getStatus())
                || "Completed".equalsIgnoreCase(model.getStatus())
                || "Pending Payment".equalsIgnoreCase(model.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_blue);
        } else if ("Bid Canceled".equalsIgnoreCase(model.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tvStatus.setTextColor(Color.DKGRAY);
        } else if ("Rejected".equalsIgnoreCase(model.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tvStatus.setTextColor(Color.RED);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tvStatus.setTextColor(Color.BLACK);
        }

        holder.btnCancelBid.setVisibility(model.canCancelBid() ? View.VISIBLE : View.GONE);
        holder.btnViewUpdate.setEnabled(model.canOpenDetails());
        holder.btnViewUpdate.setAlpha(model.canOpenDetails() ? 1f : 0.5f);

        holder.btnViewUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewUpdateClick(model);
            }
        });

        holder.btnCancelBid.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelBidClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return acceptedWorkList.size();
    }

    static class AcceptedWorkViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvStatus, tvSubject, tvStudent, tvDueDate;
        MaterialButton btnViewUpdate, btnCancelBid;

        public AcceptedWorkViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStudent = itemView.findViewById(R.id.tvStudent);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            btnViewUpdate = itemView.findViewById(R.id.btnViewUpdate);
            btnCancelBid = itemView.findViewById(R.id.btnCancelBid);
        }
    }
}