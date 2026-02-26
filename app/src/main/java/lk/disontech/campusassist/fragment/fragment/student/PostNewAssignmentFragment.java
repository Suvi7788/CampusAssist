package lk.disontech.campusassist.fragment.fragment.student;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import lk.disontech.campusassist.R;

public class PostNewAssignmentFragment extends Fragment {

    private TextInputEditText etTitle, etDescription, etDeadline;
    private MaterialAutoCompleteTextView actvSubject;
    private MaterialButton btnAttach, btnSubmit;
    private TextView tvAttachmentName;

    private Uri selectedFileUri = null;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    tvAttachmentName.setVisibility(View.VISIBLE);
                    tvAttachmentName.setText("Selected: " + uri.toString());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_new_assignment, container, false);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        etTitle = view.findViewById(R.id.etTitle);
        actvSubject = view.findViewById(R.id.actvSubject);
        etDescription = view.findViewById(R.id.etDescription);
        etDeadline = view.findViewById(R.id.etDeadline);
        btnAttach = view.findViewById(R.id.btnAttach);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName);

        // Back button (same style as your dashboard)
        topAppBar.setNavigationOnClickListener(v -> {
            // If you use back stack navigation:
            requireActivity().onBackPressed();
        });

        // Subject dropdown items (edit as you want)
        String[] subjects = new String[]{
                "Mathematics", "Science", "English", "History", "ICT", "Business"
        };
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                subjects
        );
        actvSubject.setAdapter(subjectAdapter);

        // Deadline picker
        View.OnClickListener dateClick = v -> openDatePicker();
        etDeadline.setOnClickListener(dateClick);
        // Also allow clicking the end icon by clicking the whole field
        // (Material end icon tap is not directly exposed here without til reference)

        // File attachment
        btnAttach.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        // Submit
        btnSubmit.setOnClickListener(v -> submit());

        return view;
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, day);

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    etDeadline.setText(sdf.format(picked.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void submit() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String subject = actvSubject.getText() != null ? actvSubject.getText().toString().trim() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String deadline = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(subject)) {
            actvSubject.setError("Select a subject");
            actvSubject.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(deadline)) {
            etDeadline.setError("Deadline is required");
            etDeadline.requestFocus();
            return;
        }

        // For now just show success. Later you can save to DB / Firebase / API.
        String fileInfo = (selectedFileUri != null) ? "\nAttachment: " + selectedFileUri : "\nAttachment: none";
        Toast.makeText(requireContext(),
                "Assignment Submitted ✅\nTitle: " + title + "\nSubject: " + subject + "\nDeadline: " + deadline + fileInfo,
                Toast.LENGTH_LONG).show();

        // Optional: clear form
        etTitle.setText("");
        actvSubject.setText("");
        etDescription.setText("");
        etDeadline.setText("");
        selectedFileUri = null;
        tvAttachmentName.setText("");
        tvAttachmentName.setVisibility(View.GONE);
    }
}