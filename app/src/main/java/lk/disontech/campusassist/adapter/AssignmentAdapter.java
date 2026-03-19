package lk.disontech.campusassist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AssignmentModel;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    public interface OnAcceptClickListener {
        void onAcceptClick(AssignmentModel model);
    }

    private Context context;
    private List<AssignmentModel> assignmentList;
    private OnAcceptClickListener listener;

    public AssignmentAdapter(Context context, List<AssignmentModel> assignmentList, OnAcceptClickListener listener) {
        this.context = context;
        this.assignmentList = assignmentList;
        this.listener = listener;
    }

    public void updateList(List<AssignmentModel> newList) {
        this.assignmentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        AssignmentModel model = assignmentList.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvSubject.setText(model.getSubject());
        holder.tvDeadline.setText("Deadline: " + model.getDeadline());
        holder.tvBudget.setText("Posted by: " + model.getStudentName());

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList != null ? assignmentList.size() : 0;
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvSubject, tvDeadline, tvBudget;
        MaterialButton btnAccept;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}