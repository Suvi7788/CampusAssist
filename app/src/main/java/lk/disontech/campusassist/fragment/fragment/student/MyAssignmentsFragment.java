package lk.disontech.campusassist.fragment.fragment.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import lk.disontech.campusassist.fragment.fragment.student.AssignmentDetailsFragment;
import lk.disontech.campusassist.R;

public class MyAssignmentsFragment extends Fragment {

    LinearLayout assignmentContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_assignments, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        assignmentContainer = view.findViewById(R.id.assignmentContainer);

        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        loadDummyAssignments(inflater);

        return view;
    }

    private void loadDummyAssignments(LayoutInflater inflater) {

        addAssignment(inflater, "Research Paper on Climate Change",
                "Environmental Science", "Accepted");

        addAssignment(inflater, "Calculus Problem Set",
                "Mathematics", "Pending");

        addAssignment(inflater, "Essay on Shakespeare",
                "Literature", "Completed");

        addAssignment(inflater, "Data Structures Assignment",
                "Computer Science", "Accepted");

        addAssignment(inflater, "Chemistry Lab Report",
                "Chemistry", "Pending");
    }

    private void addAssignment(LayoutInflater inflater,
                               String title,
                               String subject,
                               String status) {

        View card = inflater.inflate(R.layout.item_assignment_card, assignmentContainer, false);

        TextView tvTitle = card.findViewById(R.id.tvTitle);
        TextView tvSubject = card.findViewById(R.id.tvSubject);
        TextView tvStatus = card.findViewById(R.id.tvStatus);
        MaterialButton btnView = card.findViewById(R.id.btnViewDetails);

        tvTitle.setText(title);
        tvSubject.setText(subject);
        tvStatus.setText(status);

        // Change badge color based on status
        switch (status) {
            case "Accepted":
                tvStatus.setBackgroundColor(Color.parseColor("#10B981"));
                break;
            case "Pending":
                tvStatus.setBackgroundColor(Color.parseColor("#F97316"));
                break;
            case "Completed":
                tvStatus.setBackgroundColor(Color.parseColor("#1E3A8A"));
                break;
        }

        btnView.setOnClickListener(v -> {
//                Toast.makeText(getContext(),
//                        "Viewing: " + title,
//                        Toast.LENGTH_SHORT).show()
                    Bundle b = new Bundle();
                    b.putString("title", title);
                    b.putString("subject", subject);
                    b.putString("status", status);

                    AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
                    fragment.setArguments(b);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.dashboardContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                }
        );

        assignmentContainer.addView(card);
    }
}