package lk.disontech.campusassist.fragment.fragment.writer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lk.disontech.campusassist.R;

public class WriterDashboardFragment extends Fragment {

    public WriterDashboardFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_writer_dashboard, container, false);

        // Card clicks (simple for now)
        view.findViewById(R.id.card_browse).setOnClickListener(v ->
                toast("Browse Assignments")
        );

        view.findViewById(R.id.card_accepted).setOnClickListener(v ->
                toast("My Accepted Work")
        );

        view.findViewById(R.id.card_profile).setOnClickListener(v ->
                toast("Profile")
        );

        view.findViewById(R.id.card_notifications).setOnClickListener(v ->
                toast("Notifications")
        );

        return view;
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}