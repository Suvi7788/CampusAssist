package lk.disontech.campusassist.fragment.fragment.writer;

import android.content.Context;
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

    public interface WriterDashboardNavigator {
        void openBrowseAssignments();
        void openAcceptedWork();
        void openProfile();
        void openNotifications();
        void openDrawerOrMenu();
    }

    private WriterDashboardNavigator navigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof WriterDashboardNavigator) {
            navigator = (WriterDashboardNavigator) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    public WriterDashboardFragment() {}

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

        menuIcon.setOnClickListener(v -> {
            if (navigator != null) navigator.openDrawerOrMenu();
        });

        cardBrowse.setOnClickListener(v -> {
            if (navigator != null) navigator.openBrowseAssignments();
            else toast("Browse Assignments");
        });

        cardAccepted.setOnClickListener(v -> {
            if (navigator != null) navigator.openAcceptedWork();
            else toast("My Accepted Work");
        });

        cardProfile.setOnClickListener(v -> {
            if (navigator != null) navigator.openProfile();
            else toast("Profile");
        });

        cardNotifications.setOnClickListener(v -> {
            if (navigator != null) navigator.openNotifications();
            else toast("Notifications");
        });

        return view;
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}