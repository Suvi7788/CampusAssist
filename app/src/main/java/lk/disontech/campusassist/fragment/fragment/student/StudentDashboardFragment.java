package lk.disontech.campusassist.fragment.fragment.student;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import lk.disontech.campusassist.R;

public class StudentDashboardFragment extends Fragment {

    // Optional: host Activity can implement this to handle navigation
    public interface StudentDashboardNavigator {
        void openPostNewAssignment();
        void openMyAssignments();
        void openBrowseWriters();
        void openNotifications();
        void openHelpGuidelines();
        void openDrawerOrMenu(); // for hamburger icon
    }

    private StudentDashboardNavigator navigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof StudentDashboardNavigator) {
            navigator = (StudentDashboardNavigator) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_student_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);

        View cardPostNew = view.findViewById(R.id.cardPostNewAssignment);
        View cardMyAssignments = view.findViewById(R.id.cardMyAssignments);
        View cardBrowseWriters = view.findViewById(R.id.cardBrowseWriters);
        View cardNotifications = view.findViewById(R.id.cardNotifications);
        View cardHelp = view.findViewById(R.id.cardHelpGuidelines);

        // Hamburger click (host can open DrawerLayout)
        toolbar.setNavigationOnClickListener(v -> {
            if (navigator != null) {
                navigator.openDrawerOrMenu();
            } else {
                Toast.makeText(requireContext(), "Menu clicked (connect to Drawer later)", Toast.LENGTH_SHORT).show();
            }
        });

        cardPostNew.setOnClickListener(v -> {
            if (navigator != null) navigator.openPostNewAssignment();
            else showTodo("Post New Assignment");
        });

        cardMyAssignments.setOnClickListener(v -> {
            if (navigator != null) navigator.openMyAssignments();
            else showTodo("My Assignments");
        });

        cardBrowseWriters.setOnClickListener(v -> {
            if (navigator != null) navigator.openBrowseWriters();
            else showTodo("Browse Writers");
        });

        cardNotifications.setOnClickListener(v -> {
            if (navigator != null) navigator.openNotifications();
            else showTodo("Notifications");
        });

        cardHelp.setOnClickListener(v -> {
            if (navigator != null) navigator.openHelpGuidelines();
            else showTodo("Help / Guidelines");
        });
    }

    private void showTodo(String screenName) {
        Toast.makeText(requireContext(), "Open: " + screenName + " (create screen later)", Toast.LENGTH_SHORT).show();
    }
}