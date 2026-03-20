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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AssignmentModel;

public class MyAssignmentsFragment extends Fragment {

    LinearLayout assignmentContainer;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_assignments, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        assignmentContainer = view.findViewById(R.id.assignmentContainer);

        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // Load assignments for the logged-in student
        loadStudentAssignments(inflater);

        return view;
    }

    private void loadStudentAssignments(LayoutInflater inflater) {
        // Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = firebaseAuth.getCurrentUser().getUid();

        // Query Firestore for assignments where studentId matches the current user
        firebaseFirestore.collection("Assignments")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AssignmentModel> assignments = queryDocumentSnapshots.toObjects(AssignmentModel.class);

                    if (assignments.isEmpty()) {
                        // Show message if no assignments
                        TextView emptyMessage = new TextView(requireContext());
                        emptyMessage.setText("No assignments yet. Create one to get started!");
                        emptyMessage.setTextSize(16);
                        emptyMessage.setPadding(32, 32, 32, 32);
                        assignmentContainer.addView(emptyMessage);
                    } else {
                        // Add each assignment to the container
                        for (AssignmentModel assignment : assignments) {
                            addAssignmentCard(inflater, assignment);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error loading assignments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addAssignmentCard(LayoutInflater inflater, AssignmentModel assignment) {
        addAssignment(inflater,
                assignment.getTitle(),
                assignment.getSubject(),
                assignment.getStatus(),
                assignment);
    }

    private void addAssignment(LayoutInflater inflater,
                               String title,
                               String subject,
                               String status,
                               AssignmentModel assignment) {

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
            case "Open":
                tvStatus.setBackgroundColor(Color.parseColor("#10B981"));
                break;
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
                    Bundle b = new Bundle();
                    b.putString("title", assignment.getTitle());
                    b.putString("subject", assignment.getSubject());
                    b.putString("status", assignment.getStatus());
                    b.putString("description", assignment.getDescription());
                    b.putString("deadline", assignment.getDeadline());
                    b.putString("fileUrl", assignment.getFileUrl());
                    b.putString("fileName", assignment.getFileName());
                    b.putString("assignmentId", assignment.getAssignmentId());
                    b.putString("studentId", assignment.getStudentId());
                    b.putString("studentName", assignment.getStudentName());

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