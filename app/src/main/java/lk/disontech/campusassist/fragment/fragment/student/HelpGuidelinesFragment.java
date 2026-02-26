package lk.disontech.campusassist.fragment.fragment.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import lk.disontech.campusassist.R;

public class HelpGuidelinesFragment extends Fragment {

    private LinearLayout helpContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_guidelines, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        helpContainer = view.findViewById(R.id.helpContainer);

        addHelpCard(inflater,
                "How to Post an Assignment",
                "Learn how to create and submit assignment requests",
                "#1E3A8A",
                android.R.drawable.ic_menu_edit);

        addHelpCard(inflater,
                "Communicating with Writers",
                "Best practices for working with writers",
                "#10B981",
                android.R.drawable.ic_dialog_email);

        addHelpCard(inflater,
                "Safety & Guidelines",
                "Important rules and safety tips",
                "#F97316",
                android.R.drawable.ic_lock_lock);

        addHelpCard(inflater,
                "FAQ",
                "Frequently asked questions",
                "#8B5CF6",
                android.R.drawable.ic_menu_help);

        addNeedMoreHelpCard(inflater);

        return view;
    }

    private void addHelpCard(LayoutInflater inflater,
                             String title,
                             String desc,
                             String bgColor,
                             int iconRes) {

        View card = inflater.inflate(R.layout.item_help_card, helpContainer, false);

        View iconCircle = card.findViewById(R.id.iconCircle);
        ImageView imgIcon = card.findViewById(R.id.imgIcon);
        TextView tvTitle = card.findViewById(R.id.tvTitle);
        TextView tvDesc = card.findViewById(R.id.tvDesc);

        iconCircle.setBackgroundColor(Color.parseColor(bgColor));
        imgIcon.setImageResource(iconRes);

        tvTitle.setText(title);
        tvDesc.setText(desc);

        card.setOnClickListener(v ->
                Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show()
        );

        helpContainer.addView(card);
    }

    private void addNeedMoreHelpCard(LayoutInflater inflater) {

        // Create a custom card programmatically to match screenshot
        com.google.android.material.card.MaterialCardView card =
                new com.google.android.material.card.MaterialCardView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (14 * getResources().getDisplayMetrics().density);
        card.setLayoutParams(params);

        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(3f);
        card.setRadius(16f);
        card.setUseCompatPadding(true);

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(16, 16, 16, 16);

        TextView title = new TextView(requireContext());
        title.setText("Need More Help?");
        title.setTextColor(Color.parseColor("#111827"));
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView msg = new TextView(requireContext());
        msg.setText("Our support team is available 24/7 to assist you.");
        msg.setTextColor(Color.parseColor("#6B7280"));
        msg.setTextSize(12.5f);
        msg.setPadding(0, 10, 0, 0);

        TextView email = new TextView(requireContext());
        email.setText("Email: support@campusassist.com");
        email.setTextColor(Color.parseColor("#374151"));
        email.setTextSize(12.5f);
        email.setPadding(0, 16, 0, 0);

        TextView phone = new TextView(requireContext());
        phone.setText("Phone: +1 (555) 123-4567");
        phone.setTextColor(Color.parseColor("#374151"));
        phone.setTextSize(12.5f);
        phone.setPadding(0, 6, 0, 0);

        inner.addView(title);
        inner.addView(msg);
        inner.addView(email);
        inner.addView(phone);

        card.addView(inner);

        card.setOnClickListener(v ->
                Toast.makeText(getContext(), "Contact support", Toast.LENGTH_SHORT).show()
        );

        helpContainer.addView(card);
    }
}