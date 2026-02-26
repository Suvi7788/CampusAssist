package lk.disontech.campusassist.fragment.fragment.student;

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

import lk.disontech.campusassist.R;

public class AssignmentDetailsFragment extends Fragment {

    private TextView tvTitle, tvSubject, tvDeadline, tvStudent, tvDescription, tvAttachmentName, tvView;
    private LinearLayout attachmentRow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignment_details, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvDeadline = view.findViewById(R.id.tvDeadline);
        tvStudent = view.findViewById(R.id.tvStudent);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName);
        tvView = view.findViewById(R.id.tvView);
        attachmentRow = view.findViewById(R.id.attachmentRow);

        // Dummy data (later you can pass arguments from MyAssignmentsFragment)
        tvTitle.setText("Research Paper on Climate Change");
        tvSubject.setText("Environmental Science");
        tvDeadline.setText("Feb 28, 2026");
        tvStudent.setText("John Doe");

        tvDescription.setText(
                "Write a comprehensive research paper on the impact of climate change on global ecosystems.\n\n" +
                        "The paper should be 10–15 pages long, APA format, with at least 10 scholarly sources. " +
                        "Cover topics including greenhouse gases, biodiversity loss, and potential mitigation strategies."
        );

        tvAttachmentName.setText("assignment_guidelines.pdf");

        attachmentRow.setOnClickListener(v ->
                Toast.makeText(getContext(), "Open attachment: assignment_guidelines.pdf", Toast.LENGTH_SHORT).show()
        );

        tvView.setOnClickListener(v ->
                Toast.makeText(getContext(), "Viewing attachment...", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}