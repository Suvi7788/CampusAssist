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

    public interface OnViewUpdateClickListener {
        void onViewUpdateClick(AcceptedWorkModel model);
    }

    private final Context context;
    private final List<AcceptedWorkModel> acceptedWorkList;
    private final OnViewUpdateClickListener listener;

    public AcceptedWorkAdapter(Context context,
                               List<AcceptedWorkModel> acceptedWorkList,
                               OnViewUpdateClickListener listener) {
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
        } else if ("Under Review".equalsIgnoreCase(model.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_blue);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tvStatus.setTextColor(Color.BLACK);
        }

        holder.btnViewUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewUpdateClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return acceptedWorkList.size();
    }

    static class AcceptedWorkViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvStatus, tvSubject, tvStudent, tvDueDate;
        MaterialButton btnViewUpdate;

        public AcceptedWorkViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStudent = itemView.findViewById(R.id.tvStudent);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            btnViewUpdate = itemView.findViewById(R.id.btnViewUpdate);
        }
    }
}