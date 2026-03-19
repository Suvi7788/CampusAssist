package lk.disontech.campusassist.fragment.fragment.writer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.AssignmentAdapter;
import lk.disontech.campusassist.fragment.fragment.student.AssignmentDetailsFragment;
import lk.disontech.campusassist.model.AssignmentModel;
import lk.disontech.campusassist.repository.AssignmentRepository;

public class BrowseAssignmentsFragment extends Fragment {

    private RecyclerView recyclerAssignments;
    private AssignmentAdapter assignmentAdapter;
    private List<AssignmentModel> assignmentList;
    private List<AssignmentModel> fullAssignmentList;
    private AssignmentRepository assignmentRepository;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private ChipGroup chipGroup;

    public BrowseAssignmentsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse_assignments, container, false);

        // Initialize views
        ImageView btnBack = view.findViewById(R.id.btnBack);
        recyclerAssignments = view.findViewById(R.id.recyclerAssignments);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoData = view.findViewById(R.id.tvNoData);
        chipGroup = view.findViewById(R.id.chipGroup);

        // Initialize repository and list
        assignmentRepository = new AssignmentRepository();
        assignmentList = new ArrayList<>();
        fullAssignmentList = new ArrayList<>();

        // Setup back button
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Setup RecyclerView
        recyclerAssignments.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter
        assignmentAdapter = new AssignmentAdapter(getContext(), assignmentList, model -> {
            Bundle b = new Bundle();
            b.putString("title", model.getTitle());
            b.putString("subject", model.getSubject());
            b.putString("deadline", model.getDeadline());
            b.putString("description", model.getDescription());
            b.putString("studentName", model.getStudentName());
            b.putString("status", "Accepted");

            AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
            fragment.setArguments(b);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.dashboardContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerAssignments.setAdapter(assignmentAdapter);

        // Load assignments from backend
        loadAssignments();

        return view;
    }

    private void loadAssignments() {
        showLoading(true);
        
        assignmentRepository.getAllAssignments(new AssignmentRepository.OnAssignmentsLoadedCallback() {
            @Override
            public void onAssignmentsLoaded(List<AssignmentModel> assignments) {
                showLoading(false);
                
                if (assignments != null && !assignments.isEmpty()) {
                    fullAssignmentList = assignments;
                    assignmentList.clear();
                    assignmentList.addAll(assignments);
                    assignmentAdapter.notifyDataSetChanged();
                    
                    // Generate dynamic chips based on available subjects
                    generateSubjectChips(assignments);
                    
                    showNoData(false);
                } else {
                    showNoData(true);
                    chipGroup.removeAllViews();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(getContext(), "Error loading assignments: " + errorMessage, 
                        Toast.LENGTH_SHORT).show();
                showNoData(true);
                chipGroup.removeAllViews();
            }
        });
    }

    private void generateSubjectChips(List<AssignmentModel> assignments) {
        // Clear existing chips
        chipGroup.removeAllViews();

        // Extract unique subjects from assignments
        Set<String> subjectsSet = new HashSet<>();
        for (AssignmentModel assignment : assignments) {
            if (assignment != null && assignment.getSubject() != null && !assignment.getSubject().isEmpty()) {
                subjectsSet.add(assignment.getSubject());
            }
        }

        // Convert to list for consistent ordering
        List<String> subjectsList = new ArrayList<>(subjectsSet);
        subjectsList.sort(String::compareTo); // Sort alphabetically

        // Add "All" chip first
        addChip("All", true);

        // Add chips for each subject
        for (String subject : subjectsList) {
            addChip(subject, false);
        }
    }

    private void addChip(String label, boolean isFirst) {
        Chip chip = new Chip(getContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChipCornerRadius(4);
        
        if (isFirst) {
            chip.setChecked(true);
            // Style for "All" chip
            chip.setChipBackgroundColorResource(R.color.md_theme_primary);
            chip.setTextColor(getResources().getColor(R.color.md_theme_onPrimary, getActivity().getTheme()));
        } else {
            // Style for subject chips
            chip.setChipBackgroundColorResource(R.color.md_theme_surfaceContainerLow);
            chip.setTextColor(getResources().getColor(R.color.md_theme_onSurface, getActivity().getTheme()));
        }

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                filterAssignments(label);
            }
        });

        // Add margin
        ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 4, 4, 4);
        chip.setLayoutParams(params);

        chipGroup.addView(chip);
    }

    private void filterAssignments(String category) {
        List<AssignmentModel> filteredList = new ArrayList<>();

        if (category == null || fullAssignmentList == null) {
            return;
        }

        if (category.equalsIgnoreCase("All")) {
            filteredList.addAll(fullAssignmentList);
        } else {
            for (AssignmentModel item : fullAssignmentList) {
                if (item != null && item.getSubject() != null 
                        && item.getSubject().equalsIgnoreCase(category)) {
                    filteredList.add(item);
                }
            }
        }

        assignmentList.clear();
        assignmentList.addAll(filteredList);
        assignmentAdapter.notifyDataSetChanged();
        
        showNoData(filteredList.isEmpty());
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showNoData(boolean show) {
        if (tvNoData != null) {
            tvNoData.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerAssignments != null) {
            recyclerAssignments.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}