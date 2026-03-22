package lk.disontech.campusassist.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.HelpGuidelineItem;

public class HelpGuidelineAdapter extends RecyclerView.Adapter<HelpGuidelineAdapter.HelpViewHolder> {

    public interface OnHelpItemClickListener {
        void onItemClicked(HelpGuidelineItem item);
    }

    private final List<HelpGuidelineItem> items = new ArrayList<>();
    private final OnHelpItemClickListener listener;

    public HelpGuidelineAdapter(OnHelpItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<HelpGuidelineItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HelpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help_card, parent, false);
        return new HelpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HelpViewHolder holder, int position) {
        HelpGuidelineItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getShortDescription());
        holder.imgIcon.setImageResource(item.getIconResId());
        holder.iconCircle.setCardBackgroundColor(resolveCardColor(item.getAccentColorHex()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int resolveCardColor(String colorHex) {
        try {
            return Color.parseColor(colorHex);
        } catch (Exception ignored) {
            return Color.parseColor("#2563EB");
        }
    }

    static class HelpViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView iconCircle;
        private final ImageView imgIcon;
        private final TextView tvTitle;
        private final TextView tvDesc;

        public HelpViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCircle = itemView.findViewById(R.id.iconCircle);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }
}

