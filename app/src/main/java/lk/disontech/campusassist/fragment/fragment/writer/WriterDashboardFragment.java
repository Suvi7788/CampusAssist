package lk.disontech.campusassist.fragment.fragment.writer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lk.disontech.campusassist.R;

public class WriterDashboardFragment extends Fragment {

    public WriterDashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_writer_dashboard, container, false);

        View cardBrowse = view.findViewById(R.id.card_browse);
        View cardAccepted = view.findViewById(R.id.card_accepted);
        View cardProfile = view.findViewById(R.id.card_profile);
        View cardNotifications = view.findViewById(R.id.card_notifications);

        ImageView menuIcon = view.findViewById(R.id.ivMenu);

        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> toast("Menu clicked"));
        }

        cardBrowse.setOnClickListener(v -> toast("Browse Assignments"));
        cardAccepted.setOnClickListener(v -> toast("My Accepted Work"));
        cardProfile.setOnClickListener(v -> toast("Profile"));
        cardNotifications.setOnClickListener(v -> toast("Notifications"));

        return view;
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}