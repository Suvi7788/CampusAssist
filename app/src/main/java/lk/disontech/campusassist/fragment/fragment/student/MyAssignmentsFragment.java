package lk.disontech.campusassist.fragment.fragment.student;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AssignmentModel;

public class MyAssignmentsFragment extends Fragment {

    LinearLayout assignmentContainer;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private TextInputEditText etSearchAssignments;
    private MaterialAutoCompleteTextView actvAssignmentStatusFilter;
    private final List<AssignmentModel> fullAssignments = new ArrayList<>();
    private String selectedStatusFilter = "All";

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
        etSearchAssignments = view.findViewById(R.id.etSearchAssignments);
        actvAssignmentStatusFilter = view.findViewById(R.id.actvAssignmentStatusFilter);

        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // Load assignments for the logged-in student
        setupSearchAndFilter();
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

                    fullAssignments.clear();
                    fullAssignments.addAll(assignments);
                    applyFilters(inflater);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error loading assignments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearchAndFilter() {
        String[] statusOptions = new String[]{
                "All", "Open", "Assigned", "In Progress", "Completed", "Pending Payment", "Rejected"
        };

        Bundle args = getArguments();
        if (args != null) {
            String initialFilter = safe(args.getString("initialStatusFilter"));
            for (String option : statusOptions) {
                if (option.equalsIgnoreCase(initialFilter)) {
                    selectedStatusFilter = option;
                    break;
                }
            }
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );
        actvAssignmentStatusFilter.setAdapter(statusAdapter);
        actvAssignmentStatusFilter.setText(selectedStatusFilter, false);

        actvAssignmentStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatusFilter = statusOptions[position];
            applyFilters(LayoutInflater.from(requireContext()));
        });

        etSearchAssignments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(LayoutInflater.from(requireContext()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applyFilters(LayoutInflater inflater) {
        assignmentContainer.removeAllViews();

        String query = etSearchAssignments.getText() == null
                ? ""
                : etSearchAssignments.getText().toString().trim().toLowerCase(Locale.US);

        int visibleCount = 0;
        for (AssignmentModel assignment : fullAssignments) {
            String status = safe(assignment.getStatus());
            boolean matchesStatus = "All".equalsIgnoreCase(selectedStatusFilter)
                    || status.equalsIgnoreCase(selectedStatusFilter);
            boolean matchesSearch = query.isEmpty()
                    || safe(assignment.getTitle()).toLowerCase(Locale.US).contains(query)
                    || safe(assignment.getSubject()).toLowerCase(Locale.US).contains(query)
                    || safe(assignment.getDeadline()).toLowerCase(Locale.US).contains(query);

            if (matchesStatus && matchesSearch) {
                addAssignmentCard(inflater, assignment);
                visibleCount++;
            }
        }

        if (visibleCount == 0) {
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("No assignments found for current filters");
            emptyMessage.setTextSize(16);
            emptyMessage.setPadding(32, 32, 32, 32);
            assignmentContainer.addView(emptyMessage);
        }
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
        TextView tvDueDate = card.findViewById(R.id.tvDueDate);
        TextView tvWriterName = card.findViewById(R.id.tvWriterName);
        LinearLayout layoutWriterSection = card.findViewById(R.id.layoutWriterSection);
        MaterialButton btnViewWriter = card.findViewById(R.id.btnViewWriter);
        MaterialButton btnView = card.findViewById(R.id.btnViewDetails);

        tvTitle.setText(title);
        tvSubject.setText(subject);
        tvStatus.setText(safe(status));
        tvDueDate.setText("Due: " + (TextUtils.isEmpty(assignment.getDeadline()) ? "-" : assignment.getDeadline()));
        applyStatusBadgeStyle(tvStatus, status);

        boolean shouldShowWriter = shouldShowWriterSection(status);
        layoutWriterSection.setVisibility(shouldShowWriter ? View.VISIBLE : View.GONE);
        if (shouldShowWriter) {
            String writerName = safe(assignment.getAssignedWriterName());
            tvWriterName.setText("Writer: " + (writerName.isEmpty() ? "Not assigned yet" : writerName));

            String writerId = safe(assignment.getAssignedWriterId());
            btnViewWriter.setEnabled(!writerId.isEmpty());
            btnViewWriter.setOnClickListener(v -> {
                if (writerId.isEmpty()) {
                    Toast.makeText(requireContext(), "Writer details not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("writerId", writerId);
                WriterProfileFragment fragment = new WriterProfileFragment();
                fragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            });
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

    private void applyStatusBadgeStyle(TextView tvStatus, String status) {
        int color;
        switch (safe(status).toLowerCase(Locale.US)) {
            case "open":
                color = Color.parseColor("#0EA5E9");
                break;
            case "assigned":
                color = Color.parseColor("#10B981");
                break;
            case "in progress":
                color = Color.parseColor("#F59E0B");
                break;
            case "completed":
                color = Color.parseColor("#1E3A8A");
                break;
            case "pending payment":
                color = Color.parseColor("#8B5CF6");
                break;
            case "rejected":
                color = Color.parseColor("#EF4444");
                break;
            default:
                color = Color.parseColor("#6B7280");
                break;
        }

        Drawable background = tvStatus.getBackground();
        if (background != null) {
            background = background.mutate();
            background.setTint(color);
            tvStatus.setBackground(background);
        }
    }

    private boolean shouldShowWriterSection(String status) {
        String s = safe(status).toLowerCase(Locale.US);
        return s.equals("assigned")
                || s.equals("in progress")
                || s.equals("completed")
                || s.equals("pending payment");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}