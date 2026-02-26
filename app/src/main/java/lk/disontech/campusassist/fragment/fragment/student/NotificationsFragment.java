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

public class NotificationsFragment extends Fragment {

    private LinearLayout notificationsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        notificationsContainer = view.findViewById(R.id.notificationsContainer);

        loadDummyNotifications(inflater);

        return view;
    }

    private void loadDummyNotifications(LayoutInflater inflater) {
        addNotification(inflater,
                "Assignment Accepted",
                "Dr. Sarah Johnson accepted your assignment",
                "5 min ago",
                "success");

        addNotification(inflater,
                "New Message",
                "You have a new message from Prof. Chen",
                "1 hour ago",
                "message");

        addNotification(inflater,
                "Deadline Approaching",
                "Chemistry Lab Report due in 2 days",
                "3 hours ago",
                "warning");

        addNotification(inflater,
                "Payment Received",
                "Payment of $150 has been processed",
                "1 day ago",
                "success");
    }

    private void addNotification(LayoutInflater inflater,
                                 String title,
                                 String message,
                                 String time,
                                 String type) {

        View card = inflater.inflate(R.layout.item_notification_card, notificationsContainer, false);

        ImageView imgIcon = card.findViewById(R.id.imgIcon);
        TextView tvTitle = card.findViewById(R.id.tvTitle);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        TextView tvTime = card.findViewById(R.id.tvTime);

        tvTitle.setText(title);
        tvMessage.setText(message);
        tvTime.setText(time);

        // Icon + tint based on type
        switch (type) {
            case "success":
                imgIcon.setImageResource(android.R.drawable.checkbox_on_background);
                imgIcon.setColorFilter(Color.parseColor("#10B981"));
                break;

            case "message":
                imgIcon.setImageResource(android.R.drawable.ic_dialog_email);
                imgIcon.setColorFilter(Color.parseColor("#2563EB"));
                break;

            case "warning":
                imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                imgIcon.setColorFilter(Color.parseColor("#F97316"));
                break;

            default:
                imgIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                imgIcon.setColorFilter(Color.parseColor("#6B7280"));
                break;
        }

        card.setOnClickListener(v ->
                Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show()
        );

        notificationsContainer.addView(card);
    }
}