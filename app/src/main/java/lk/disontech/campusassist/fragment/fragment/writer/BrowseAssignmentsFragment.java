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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.AssignmentAdapter;
import lk.disontech.campusassist.fragment.fragment.student.AssignmentDetailsFragment;
import lk.disontech.campusassist.model.AssignmentModel;

public class BrowseAssignmentsFragment extends Fragment {

    private RecyclerView recyclerAssignments;
    private AssignmentAdapter assignmentAdapter;
    private List<AssignmentModel> assignmentList;

    public BrowseAssignmentsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse_assignments, container, false);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        recyclerAssignments = view.findViewById(R.id.recyclerAssignments);

        Chip chipAll = view.findViewById(R.id.chipAll);
        Chip chipMathematics = view.findViewById(R.id.chipMathematics);
        Chip chipScience = view.findViewById(R.id.chipScience);
        Chip chipLiterature = view.findViewById(R.id.chipLiterature);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        recyclerAssignments.setLayoutManager(new LinearLayoutManager(getContext()));

        assignmentList = new ArrayList<>();
        assignmentList.add(AssignmentModel.builder()
                .assignmentId("1")
                .studentId("student1")
                .studentName("John Doe")
                .studentEmail("john@university.edu")
                .title("Research Paper on Climate Change")
                .subject("Environmental Science")
                .description("Write a comprehensive research paper on climate change impacts")
                .deadline("Feb 28, 2026")
                .fileUrl("")
                .fileName("")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .status("Open")
                .build()
        );
        assignmentList.add(AssignmentModel.builder()
                .assignmentId("2")
                .studentId("student2")
                .studentName("Jane Smith")
                .studentEmail("jane@university.edu")
                .title("Calculus Problem Set")
                .subject("Mathematics")
                .description("Complete 50 calculus problems from chapters 5-8")
                .deadline("Feb 25, 2026")
                .fileUrl("")
                .fileName("")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .status("Open")
                .build()
        );
        assignmentList.add(AssignmentModel.builder()
                .assignmentId("3")
                .studentId("student3")
                .studentName("Bob Wilson")
                .studentEmail("bob@university.edu")
                .title("Machine Learning Project")
                .subject("Computer Science")
                .description("Build a neural network to classify images")
                .deadline("Mar 5, 2026")
                .fileUrl("")
                .fileName("")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .status("Open")
                .build()
        );

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

        chipAll.setOnClickListener(v -> filterAssignments("All"));
        chipMathematics.setOnClickListener(v -> filterAssignments("Mathematics"));
        chipScience.setOnClickListener(v -> filterAssignments("Science"));
        chipLiterature.setOnClickListener(v -> filterAssignments("Literature"));

        return view;
    }

    private void filterAssignments(String category) {
        List<AssignmentModel> filteredList = new ArrayList<>();

        if (category.equalsIgnoreCase("All")) {
            filteredList.addAll(assignmentList);
        } else if (category.equalsIgnoreCase("Mathematics")) {
            for (AssignmentModel item : assignmentList) {
                if (item.getSubject().equalsIgnoreCase("Mathematics")) {
                    filteredList.add(item);
                }
            }
        } else if (category.equalsIgnoreCase("Science")) {
            for (AssignmentModel item : assignmentList) {
                if (item.getSubject().equalsIgnoreCase("Environmental Science")
                        || item.getSubject().equalsIgnoreCase("Computer Science")
                        || item.getSubject().toLowerCase().contains("science")) {
                    filteredList.add(item);
                }
            }
        } else if (category.equalsIgnoreCase("Literature")) {
            for (AssignmentModel item : assignmentList) {
                if (item.getSubject().equalsIgnoreCase("Literature")) {
                    filteredList.add(item);
                }
            }
        }

        assignmentAdapter.updateList(filteredList);
    }
}