package lk.disontech.campusassist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.WriterEarningItem;

public class WriterEarningsAdapter extends RecyclerView.Adapter<WriterEarningsAdapter.WriterEarningViewHolder> {

    private final List<WriterEarningItem> items = new ArrayList<>();
    private final NumberFormat amountFormat;
    private final SimpleDateFormat dateFormat;

    public WriterEarningsAdapter() {
        amountFormat = NumberFormat.getNumberInstance(Locale.US);
        amountFormat.setMinimumFractionDigits(2);
        amountFormat.setMaximumFractionDigits(2);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    public void submitList(List<WriterEarningItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WriterEarningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_writer_earning, parent, false);
        return new WriterEarningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WriterEarningViewHolder holder, int position) {
        WriterEarningItem item = items.get(position);
        holder.tvTitle.setText(item.getAssignmentTitle());
        holder.tvStudent.setText("Student: " + item.getStudentName());
        holder.tvPaidDate.setText(item.getPaidAtMillis() > 0L
                ? "Paid On: " + dateFormat.format(new Date(item.getPaidAtMillis()))
                : "Paid On: N/A");
        holder.tvStatus.setText("Status: " + item.getPaymentStatus());
        holder.tvAmount.setText("LKR " + amountFormat.format(item.getAmount()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WriterEarningViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvStudent;
        final TextView tvPaidDate;
        final TextView tvStatus;
        final TextView tvAmount;

        WriterEarningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStudent = itemView.findViewById(R.id.tvStudent);
            tvPaidDate = itemView.findViewById(R.id.tvPaidDate);
            tvStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}

