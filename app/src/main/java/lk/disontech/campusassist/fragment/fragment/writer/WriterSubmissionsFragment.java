package lk.disontech.campusassist.fragment.fragment.writer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.WriterSubmissionAdapter;
import lk.disontech.campusassist.fragment.fragment.student.AssignmentDetailsFragment;
import lk.disontech.campusassist.model.WriterSubmissionItem;

public class WriterSubmissionsFragment extends Fragment {

    private RecyclerView recyclerWriterSubmissions;
    private ProgressBar progressWriterSubmissions;
    private TextView tvWriterSubmissionsLoading;
    private TextView tvWriterSubmissionsEmpty;
    private TextInputEditText etSearchSubmissions;
    private MaterialAutoCompleteTextView actvSubmissionStatusFilter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private WriterSubmissionAdapter writerSubmissionAdapter;
    private final List<WriterSubmissionItem> submissionItems = new ArrayList<>();
    private final List<WriterSubmissionItem> fullSubmissionItems = new ArrayList<>();
    private String selectedStatusFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_writer_submissions, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerWriterSubmissions = view.findViewById(R.id.recyclerWriterSubmissions);
        progressWriterSubmissions = view.findViewById(R.id.progressWriterSubmissions);
        tvWriterSubmissionsLoading = view.findViewById(R.id.tvWriterSubmissionsLoading);
        tvWriterSubmissionsEmpty = view.findViewById(R.id.tvWriterSubmissionsEmpty);
        etSearchSubmissions = view.findViewById(R.id.etSearchSubmissions);
        actvSubmissionStatusFilter = view.findViewById(R.id.actvSubmissionStatusFilter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        recyclerWriterSubmissions.setLayoutManager(new LinearLayoutManager(getContext()));
        writerSubmissionAdapter = new WriterSubmissionAdapter(requireContext(), submissionItems,
                this::openAssignmentDetails);
        recyclerWriterSubmissions.setAdapter(writerSubmissionAdapter);
        setupFilters();

        loadSubmittedAssignments();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerWriterSubmissions != null) {
            loadSubmittedAssignments();
        }
    }

    private void loadSubmittedAssignments() {
        if (firebaseAuth.getCurrentUser() == null) {
            showLoadingState(false, "");
            fullSubmissionItems.clear();
            submissionItems.clear();
            if (writerSubmissionAdapter != null) {
                writerSubmissionAdapter.notifyDataSetChanged();
            }
            updateEmptyState();
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();
        showLoadingState(true, "Loading submissions...");

        firebaseFirestore.collection("Assignments")
                .whereEqualTo("assignedWriterId", writerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WriterSubmissionItem> loadedItems = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        if (!isSubmittedAssignment(documentSnapshot, writerId)) {
                            continue;
                        }

                        loadedItems.add(new WriterSubmissionItem(
                                documentSnapshot.getId(),
                                safe(documentSnapshot.getString("title")),
                                safe(documentSnapshot.getString("subject")),
                                safe(documentSnapshot.getString("studentId")),
                                resolveStudentName(documentSnapshot),
                                safe(documentSnapshot.getString("deadline")),
                                safe(documentSnapshot.getString("description")),
                                safe(documentSnapshot.getString("fileUrl")),
                                safe(documentSnapshot.getString("fileName")),
                                safe(documentSnapshot.getString("status")),
                                resolveSubmittedAt(documentSnapshot),
                                resolvePriceDisplay(documentSnapshot)
                        ));
                    }

                    Collections.sort(loadedItems, (left, right) ->
                            Long.compare(right.getSubmittedAt(), left.getSubmittedAt()));

                    fullSubmissionItems.clear();
                    fullSubmissionItems.addAll(loadedItems);
                    applyFilters();
                    showLoadingState(false, "");
                })
                .addOnFailureListener(e -> {
                    showLoadingState(false, "");
                    fullSubmissionItems.clear();
                    submissionItems.clear();
                    if (writerSubmissionAdapter != null) {
                        writerSubmissionAdapter.notifyDataSetChanged();
                    }
                    updateEmptyState();
                    Toast.makeText(getContext(), "Failed to load submissions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isSubmittedAssignment(DocumentSnapshot documentSnapshot, String writerId) {
        String assignedWriterId = safe(documentSnapshot.getString("assignedWriterId"));
        String submissionWriterId = safe(documentSnapshot.getString("submissionWriterId"));
        String status = safe(documentSnapshot.getString("status"));
        String submissionFileUrl = safe(documentSnapshot.getString("submissionFileUrl"));
        String submissionFileName = safe(documentSnapshot.getString("submissionFileName"));
        long submissionAt = resolveSubmittedAt(documentSnapshot);

        boolean hasSubmissionData = submissionAt > 0L
                || !TextUtils.isEmpty(submissionFileUrl)
                || !TextUtils.isEmpty(submissionFileName);
        boolean allowedStatus = "Pending Payment".equalsIgnoreCase(status)
                || "Completed".equalsIgnoreCase(status);
        boolean submittedByLoggedInWriter = writerId.equals(submissionWriterId)
                || (writerId.equals(assignedWriterId) && hasSubmissionData);

        return submittedByLoggedInWriter && hasSubmissionData && allowedStatus;
    }

    private void setupFilters() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Pending Payment", "Completed"}
        );
        actvSubmissionStatusFilter.setAdapter(statusAdapter);
        actvSubmissionStatusFilter.setText(selectedStatusFilter, false);
        actvSubmissionStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatusFilter = String.valueOf(parent.getItemAtPosition(position));
            applyFilters();
        });

        etSearchSubmissions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applyFilters() {
        String query = etSearchSubmissions != null && etSearchSubmissions.getText() != null
                ? etSearchSubmissions.getText().toString().trim().toLowerCase(Locale.US)
                : "";

        submissionItems.clear();
        for (WriterSubmissionItem item : fullSubmissionItems) {
            if (item == null) {
                continue;
            }

            String status = safe(item.getAssignmentStatus());
            boolean matchesStatus = "All".equalsIgnoreCase(selectedStatusFilter)
                    || status.equalsIgnoreCase(selectedStatusFilter);

            boolean matchesSearch = query.isEmpty()
                    || safe(item.getTitle()).toLowerCase(Locale.US).contains(query)
                    || safe(item.getSubject()).toLowerCase(Locale.US).contains(query)
                    || safe(item.getStudentName()).toLowerCase(Locale.US).contains(query)
                    || status.toLowerCase(Locale.US).contains(query);

            if (matchesStatus && matchesSearch) {
                submissionItems.add(item);
            }
        }

        if (writerSubmissionAdapter != null) {
            writerSubmissionAdapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    private long resolveSubmittedAt(DocumentSnapshot documentSnapshot) {
        Long submissionAt = documentSnapshot.getLong("submissionAt");
        if (submissionAt != null && submissionAt > 0L) {
            return submissionAt;
        }

        Long updatedAt = documentSnapshot.getLong("updatedAt");
        return updatedAt != null ? updatedAt : 0L;
    }

    private String resolveStudentName(DocumentSnapshot documentSnapshot) {
        String studentName = safe(documentSnapshot.getString("studentName"));
        if (!studentName.isEmpty()) {
            return studentName;
        }

        String studentEmail = safe(documentSnapshot.getString("studentEmail"));
        return studentEmail.isEmpty() ? "Student" : studentEmail;
    }

    private String resolvePriceDisplay(DocumentSnapshot documentSnapshot) {
        Object rawPrice = documentSnapshot.get("paymentAmount");
        if (rawPrice == null) {
            rawPrice = documentSnapshot.get("price");
        }
        if (rawPrice == null) {
            rawPrice = documentSnapshot.get("amount");
        }

        double value = 0d;
        if (rawPrice instanceof Number) {
            value = ((Number) rawPrice).doubleValue();
        } else if (rawPrice instanceof String) {
            try {
                value = Double.parseDouble(((String) rawPrice).trim());
            } catch (NumberFormatException ignored) {
                value = 0d;
            }
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return "LKR " + numberFormat.format(value);
    }

    private void updateEmptyState() {
        boolean isEmpty = submissionItems.isEmpty();
        if (tvWriterSubmissionsEmpty != null) {
            tvWriterSubmissionsEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerWriterSubmissions != null) {
            recyclerWriterSubmissions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showLoadingState(boolean isLoading, String message) {
        if (progressWriterSubmissions != null) {
            progressWriterSubmissions.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (tvWriterSubmissionsLoading != null) {
            tvWriterSubmissionsLoading.setText(TextUtils.isEmpty(message) ? "Loading..." : message);
            tvWriterSubmissionsLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (isLoading && tvWriterSubmissionsEmpty != null) {
            tvWriterSubmissionsEmpty.setVisibility(View.GONE);
        }
    }

    private void openAssignmentDetails(WriterSubmissionItem item) {
        if (item == null || TextUtils.isEmpty(item.getAssignmentId())) {
            Toast.makeText(getContext(), "Assignment details are not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("title", item.getTitle());
        bundle.putString("subject", item.getSubject());
        bundle.putString("studentName", item.getStudentName());
        bundle.putString("deadline", item.getDeadline());
        bundle.putString("status", item.getAssignmentStatus());
        bundle.putString("description", item.getDescription());
        bundle.putString("fileUrl", item.getFileUrl());
        bundle.putString("fileName", item.getFileName());
        bundle.putString("assignmentId", item.getAssignmentId());
        bundle.putString("studentId", item.getStudentId());
        bundle.putBoolean("isWriterView", true);
        bundle.putBoolean("fromMyWork", true);
        bundle.putString("writerWorkStatus", item.getAssignmentStatus());
        bundle.putString("writerBidStatus", "Approved");

        AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

