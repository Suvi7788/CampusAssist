package lk.disontech.campusassist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.BidModel;

public class BidWriterAdapter extends RecyclerView.Adapter<BidWriterAdapter.BidViewHolder> {

    public interface OnCallWriterClickListener {
        void onCallWriter(BidModel bidModel);
    }

    private final List<BidModel> bidList = new ArrayList<>();
    private final OnCallWriterClickListener onCallWriterClickListener;

    public BidWriterAdapter(OnCallWriterClickListener onCallWriterClickListener) {
        this.onCallWriterClickListener = onCallWriterClickListener;
    }

    public void submitList(List<BidModel> newList) {
        bidList.clear();
        if (newList != null) {
            bidList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bid_writer_card, parent, false);
        return new BidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        BidModel bid = bidList.get(position);
        holder.tvWriterName.setText(bid.getWriterName());
        holder.tvWriterEmail.setText(bid.getWriterEmail());
        holder.tvWriterMobile.setText(bid.getWriterMobile());

        holder.btnCallWriter.setOnClickListener(v -> {
            if (onCallWriterClickListener != null) {
                onCallWriterClickListener.onCallWriter(bid);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bidList.size();
    }

    static class BidViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvWriterName;
        private final TextView tvWriterEmail;
        private final TextView tvWriterMobile;
        private final MaterialButton btnCallWriter;

        public BidViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWriterName = itemView.findViewById(R.id.tvWriterName);
            tvWriterEmail = itemView.findViewById(R.id.tvWriterEmail);
            tvWriterMobile = itemView.findViewById(R.id.tvWriterMobile);
            btnCallWriter = itemView.findViewById(R.id.btnCallWriter);
        }
    }
}

