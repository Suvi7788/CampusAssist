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

    public interface BidActionListener {
        void onViewProfile(BidModel bidModel);

        void onAcceptBid(BidModel bidModel);

        void onRejectBid(BidModel bidModel);
    }

    private final List<BidModel> bidList = new ArrayList<>();
    private final BidActionListener bidActionListener;

    public BidWriterAdapter(BidActionListener bidActionListener) {
        this.bidActionListener = bidActionListener;
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
        holder.tvCompletedProjects.setText(String.valueOf(bid.getCompletedProjectsCount()));
        holder.tvBidStatus.setText(getStatusLabel(bid.getStatus()));

        boolean isPending = bid.getStatus() == null || bid.getStatus().trim().isEmpty()
                || "Pending".equalsIgnoreCase(bid.getStatus());

        holder.layoutBidActions.setVisibility(View.VISIBLE);
        holder.btnAcceptBid.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnRejectBid.setVisibility(isPending ? View.VISIBLE : View.GONE);

        holder.btnViewProfile.setOnClickListener(v -> {
            if (bidActionListener != null) {
                bidActionListener.onViewProfile(bid);
            }
        });

        holder.btnAcceptBid.setOnClickListener(v -> {
            if (bidActionListener != null) {
                bidActionListener.onAcceptBid(bid);
            }
        });

        holder.btnRejectBid.setOnClickListener(v -> {
            if (bidActionListener != null) {
                bidActionListener.onRejectBid(bid);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bidList.size();
    }

    static class BidViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvWriterName;
        private final TextView tvCompletedProjects;
        private final TextView tvBidStatus;
        private final View layoutBidActions;
        private final MaterialButton btnViewProfile;
        private final MaterialButton btnAcceptBid;
        private final MaterialButton btnRejectBid;

        public BidViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWriterName = itemView.findViewById(R.id.tvWriterName);
            tvCompletedProjects = itemView.findViewById(R.id.tvCompletedProjects);
            tvBidStatus = itemView.findViewById(R.id.tvBidStatus);
            layoutBidActions = itemView.findViewById(R.id.layoutBidActions);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnAcceptBid = itemView.findViewById(R.id.btnAcceptBid);
            btnRejectBid = itemView.findViewById(R.id.btnRejectBid);
        }
    }

    private String getStatusLabel(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Pending";
        }
        return status;
    }
}
