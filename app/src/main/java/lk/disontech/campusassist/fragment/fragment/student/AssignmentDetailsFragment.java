package lk.disontech.campusassist.fragment.fragment.student;

import android.content.Intent;
import android.net.Uri;
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
    private String fileUrl = "";

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

        // Get data from Bundle arguments passed from MyAssignmentsFragment
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("title", "");
            String subject = args.getString("subject", "");
            String deadline = args.getString("deadline", "");
            String description = args.getString("description", "");
            String fileName = args.getString("fileName", "");
            fileUrl = args.getString("fileUrl", "");

            // Set the UI with actual data
            tvTitle.setText(title);
            tvSubject.setText(subject);
            tvDeadline.setText(deadline);
            tvDescription.setText(description);

            // Handle attachment display
            if (fileName != null && !fileName.isEmpty()) {
                tvAttachmentName.setText(fileName);
                attachmentRow.setVisibility(View.VISIBLE);
            } else {
                attachmentRow.setVisibility(View.GONE);
            }
        } else {
            // Show placeholder if no data provided
            tvTitle.setText("Assignment Details");
            tvDescription.setText("No assignment data available");
            attachmentRow.setVisibility(View.GONE);
        }

        // Handle attachment row click
        attachmentRow.setOnClickListener(v -> openAttachment());

        // Handle view button click
        tvView.setOnClickListener(v -> openAttachment());

        return view;
    }

    private void openAttachment() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(getContext(), "No file attached to this assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Open the file URL in a browser or default app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(fileUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}