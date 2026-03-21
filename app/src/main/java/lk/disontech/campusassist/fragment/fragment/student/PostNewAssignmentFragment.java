package lk.disontech.campusassist.fragment.fragment.student;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AssignmentModel;
import lk.disontech.campusassist.model.User;

public class PostNewAssignmentFragment extends Fragment {

    private TextInputEditText etTitle, etDescription, etDeadline, etPaymentAmount;
    private MaterialAutoCompleteTextView actvSubject;
    private MaterialButton btnAttach, btnSubmit, btnSelectLocation;
    private TextView tvAttachmentName, tvSelectedLocation;
    private ProgressBar progressPostAssignment;

    private Uri selectedFileUri = null;
    private String selectedFileName = "";
    private Double selectedLatitude;
    private Double selectedLongitude;
    private String selectedAddress = "";

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    selectedFileName = getFileName(uri);
                    tvAttachmentName.setVisibility(View.VISIBLE);
                    tvAttachmentName.setText("Selected: " + selectedFileName);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_new_assignment, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("assignments");

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading Assignment");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        etTitle = view.findViewById(R.id.etTitle);
        actvSubject = view.findViewById(R.id.actvSubject);
        etDescription = view.findViewById(R.id.etDescription);
        etDeadline = view.findViewById(R.id.etDeadline);
        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        btnAttach = view.findViewById(R.id.btnAttach);
        btnSelectLocation = view.findViewById(R.id.btnSelectLocation);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName);
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        progressPostAssignment = view.findViewById(R.id.progressPostAssignment);

        // Back button
        topAppBar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Subject dropdown items
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

        // File attachment
        btnAttach.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnSelectLocation.setOnClickListener(v -> openLocationPicker());

        getParentFragmentManager().setFragmentResultListener(
                LocationPickerFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    if (!result.containsKey(LocationPickerFragment.KEY_LATITUDE)
                            || !result.containsKey(LocationPickerFragment.KEY_LONGITUDE)) {
                        return;
                    }
                    selectedLatitude = result.getDouble(LocationPickerFragment.KEY_LATITUDE);
                    selectedLongitude = result.getDouble(LocationPickerFragment.KEY_LONGITUDE);
                    selectedAddress = result.getString(LocationPickerFragment.KEY_ADDRESS, "");

                    if (TextUtils.isEmpty(selectedAddress)) {
                        selectedAddress = "Lat: " + selectedLatitude + ", Lng: " + selectedLongitude;
                    }
                    tvSelectedLocation.setText(selectedAddress);
                }
        );

        // Submit
        btnSubmit.setOnClickListener(v -> submit());

        return view;
    }

    private void openLocationPicker() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new LocationPickerFragment())
                .addToBackStack(null)
                .commit();
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
        String paymentAmountText = etPaymentAmount.getText() != null ? etPaymentAmount.getText().toString().trim() : "";
        double paymentAmount;

        // Validation
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
        if (TextUtils.isEmpty(paymentAmountText)) {
            etPaymentAmount.setError("Payment amount is required");
            etPaymentAmount.requestFocus();
            return;
        }
        try {
            paymentAmount = Double.parseDouble(paymentAmountText);
        } catch (NumberFormatException e) {
            etPaymentAmount.setError("Enter a valid numeric amount");
            etPaymentAmount.requestFocus();
            return;
        }
        if (paymentAmount <= 0) {
            etPaymentAmount.setError("Amount must be greater than 0");
            etPaymentAmount.requestFocus();
            return;
        }
        etPaymentAmount.setError(null);
        if (selectedLatitude == null || selectedLongitude == null || TextUtils.isEmpty(selectedAddress)) {
            Toast.makeText(requireContext(), "Please select delivery address on map", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user info
        String studentId = firebaseAuth.getCurrentUser().getUid();

        setSubmitLoading(true);

        // Fetch user data from Firestore
        firebaseFirestore.collection("Users").document(studentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        String studentName = user.getFirstName() + " " + user.getLastName();
                        String studentEmail = user.getEmail();

                        // Upload file if selected
                        if (selectedFileUri != null) {
                            uploadFileAndSaveAssignment(
                                    studentId,
                                    studentName,
                                    studentEmail,
                                    title,
                                    subject,
                                    desc,
                                    deadline,
                                    paymentAmount,
                                    selectedAddress,
                                    selectedLatitude,
                                    selectedLongitude
                            );
                        } else {
                            // Save assignment without file
                            saveAssignmentToFirestore(
                                    studentId,
                                    studentName,
                                    studentEmail,
                                    title,
                                    subject,
                                    desc,
                                    deadline,
                                    paymentAmount,
                                    "",
                                    "",
                                    selectedAddress,
                                    selectedLatitude,
                                    selectedLongitude
                            );
                        }
                    } else {
                        setSubmitLoading(false);
                        Toast.makeText(requireContext(), "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setSubmitLoading(false);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadFileAndSaveAssignment(String studentId, String studentName, String studentEmail,
                                           String title, String subject, String desc, String deadline,
                                           double paymentAmount,
                                           String deliveryAddress, Double deliveryLatitude, Double deliveryLongitude) {
        progressDialog.show();

        // Create unique file name with timestamp
        String fileExtension = getFileExtension(selectedFileUri);
        String fileName = "Assignment_" + UUID.randomUUID() + fileExtension;

        // Upload file to Firebase Storage
        StorageReference fileReference = storageReference.child(studentId).child(fileName);

        fileReference.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String fileUrl = uri.toString();
                        saveAssignmentToFirestore(
                                studentId,
                                studentName,
                                studentEmail,
                                title,
                                subject,
                                desc,
                                deadline,
                                paymentAmount,
                                fileUrl,
                                selectedFileName,
                                deliveryAddress,
                                deliveryLatitude,
                                deliveryLongitude
                        );
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        setSubmitLoading(false);
                        Toast.makeText(requireContext(), "Error getting file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    setSubmitLoading(false);
                    Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    long progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading... " + progress + "%");
                });
    }

    private void saveAssignmentToFirestore(String studentId, String studentName, String studentEmail,
                                          String title, String subject, String desc, String deadline,
                                          double paymentAmount,
                                          String fileUrl, String fileName,
                                          String deliveryAddress, Double deliveryLatitude, Double deliveryLongitude) {
        // Create assignment ID
        String assignmentId = firebaseFirestore.collection("Assignments").document().getId();

        // Create assignment object
        AssignmentModel assignment = AssignmentModel.builder()
                .assignmentId(assignmentId)
                .studentId(studentId)
                .studentName(studentName)
                .studentEmail(studentEmail)
                .title(title)
                .subject(subject)
                .description(desc)
                .deadline(deadline)
                .paymentAmount(paymentAmount)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .deliveryAddress(deliveryAddress)
                .deliveryLatitude(deliveryLatitude)
                .deliveryLongitude(deliveryLongitude)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .status("Open")
                .build();

        // Save to Firestore
        firebaseFirestore.collection("Assignments").document(assignmentId).set(assignment)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    setSubmitLoading(false);
                    Toast.makeText(requireContext(), "Assignment Posted Successfully! ✅", Toast.LENGTH_SHORT).show();

                    // Clear form
                    clearForm();

                    // Optionally navigate back
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    setSubmitLoading(false);
                    Toast.makeText(requireContext(), "Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        etTitle.setText("");
        actvSubject.setText("");
        etDescription.setText("");
        etDeadline.setText("");
        etPaymentAmount.setText("");
        selectedFileUri = null;
        selectedFileName = "";
        tvAttachmentName.setText("");
        tvAttachmentName.setVisibility(View.GONE);
        selectedLatitude = null;
        selectedLongitude = null;
        selectedAddress = "";
        tvSelectedLocation.setText("No address selected");
        setSubmitLoading(false);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        String mimeType = contentResolver.getType(uri);

        if (mimeType == null) {
            return ".bin";
        }

        switch (mimeType) {
            case "application/pdf":
                return ".pdf";
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "application/vnd.ms-excel":
                return ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ".xlsx";
            case "text/plain":
                return ".txt";
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            default:
                return "";
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void setSubmitLoading(boolean isLoading) {
        if (progressPostAssignment != null) {
            progressPostAssignment.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!isLoading);
            btnSubmit.setText(isLoading ? "Saving..." : "Submit Assignment");
        }
        if (btnAttach != null) {
            btnAttach.setEnabled(!isLoading);
        }
        if (btnSelectLocation != null) {
            btnSelectLocation.setEnabled(!isLoading);
        }
    }
}