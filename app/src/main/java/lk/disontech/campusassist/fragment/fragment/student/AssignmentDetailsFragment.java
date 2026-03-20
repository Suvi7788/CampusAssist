package lk.disontech.campusassist.fragment.fragment.student;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.BidWriterAdapter;
import lk.disontech.campusassist.model.BidModel;
import lk.disontech.campusassist.model.User;
import lk.disontech.campusassist.repository.BidRepository;

public class AssignmentDetailsFragment extends Fragment {

    private TextView tvTitle, tvSubject, tvDeadline, tvStudent, tvDescription, tvAttachmentName, tvView;
    private TextInputEditText etTitle, etDescription, etDeadline;
    private MaterialAutoCompleteTextView actvSubject;
    private TextView etAttachmentName;
    private MaterialButton btnChangeFile;
    private LinearLayout attachmentRow;
    private LinearLayout detailsViewLayout, editModeLayout;
    private LinearLayout attachmentEditRow;
    private MaterialCardView studentBidsSection;
    private MaterialCardView writerWorkSection;
    private RecyclerView recyclerBids;
    private TextView tvNoBids, tvWriterWorkStatus, tvSubmissionFileName;
    private MaterialButton btnEdit, btnDelete, btnSave, btnCancel;
    private MaterialButton btnBidForAssignment, btnStartWork, btnPickSubmissionFile, btnSubmitCompletedWork;
    private TextInputEditText etSubmissionNotes;
    private LinearLayout completeWorkSection;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private BidRepository bidRepository;
    private BidWriterAdapter bidWriterAdapter;
    
    private String fileUrl = "";
    private String assignmentId = "";
    private String studentId = "";
    private String currentFileName = "";
    private String assignmentStatus = "";
    private String writerBidStatus = "";
    private String writerWorkStatus = "";
    private String newFileUrl = "";
    private String newFileName = "";
    private Uri selectedFileUri = null;
    private Uri submissionFileUri = null;
    private String submissionFileName = "";
    private boolean isEditMode = false;
    private boolean isWriterView = false;
    private boolean fromMyWork = false;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    newFileName = getFileName(uri);
                    etAttachmentName.setText("Selected: " + newFileName);
                }
            });

    private final ActivityResultLauncher<String> submissionFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    submissionFileUri = uri;
                    submissionFileName = getFileName(uri);
                    tvSubmissionFileName.setText(submissionFileName == null || submissionFileName.isEmpty()
                            ? "No file selected" : submissionFileName);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignment_details, container, false);

        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("assignments");
        firebaseAuth = FirebaseAuth.getInstance();
        bidRepository = new BidRepository();

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Updating Assignment");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Initialize view mode elements
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvDeadline = view.findViewById(R.id.tvDeadline);
        tvStudent = view.findViewById(R.id.tvStudent);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName);
        tvView = view.findViewById(R.id.tvView);
        attachmentRow = view.findViewById(R.id.attachmentRow);
        detailsViewLayout = view.findViewById(R.id.detailsViewLayout);

        // Initialize edit mode elements
        etTitle = view.findViewById(R.id.etTitle);
        actvSubject = view.findViewById(R.id.actvSubject);
        etDeadline = view.findViewById(R.id.etDeadline);
        etDescription = view.findViewById(R.id.etDescription);
        etAttachmentName = view.findViewById(R.id.etAttachmentName);
        btnChangeFile = view.findViewById(R.id.btnChangeFile);
        attachmentEditRow = view.findViewById(R.id.attachmentEditRow);
        editModeLayout = view.findViewById(R.id.editModeLayout);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnBidForAssignment = view.findViewById(R.id.btnBidForAssignment);
        studentBidsSection = view.findViewById(R.id.studentBidsSection);
        writerWorkSection = view.findViewById(R.id.writerWorkSection);
        recyclerBids = view.findViewById(R.id.recyclerBids);
        tvNoBids = view.findViewById(R.id.tvNoBids);
        tvWriterWorkStatus = view.findViewById(R.id.tvWriterWorkStatus);
        tvSubmissionFileName = view.findViewById(R.id.tvSubmissionFileName);
        btnStartWork = view.findViewById(R.id.btnStartWork);
        btnPickSubmissionFile = view.findViewById(R.id.btnPickSubmissionFile);
        btnSubmitCompletedWork = view.findViewById(R.id.btnSubmitCompletedWork);
        etSubmissionNotes = view.findViewById(R.id.etSubmissionNotes);
        completeWorkSection = view.findViewById(R.id.completeWorkSection);

        bidWriterAdapter = new BidWriterAdapter(new BidWriterAdapter.BidActionListener() {
            @Override
            public void onViewProfile(BidModel bidModel) {
                openWriterProfile(bidModel);
            }

            @Override
            public void onAcceptBid(BidModel bidModel) {
                confirmAcceptBid(bidModel);
            }

            @Override
            public void onRejectBid(BidModel bidModel) {
                confirmRejectBid(bidModel);
            }
        });
        recyclerBids.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBids.setAdapter(bidWriterAdapter);
        recyclerBids.setNestedScrollingEnabled(false);

        // Setup subject dropdown
        setupSubjectDropdown();

        // Get data from Bundle arguments passed from MyAssignmentsFragment
        Bundle args = getArguments();
        if (args != null) {
            isWriterView = args.getBoolean("isWriterView", false);
            String title = args.getString("title", "");
            String subject = args.getString("subject", "");
            assignmentStatus = args.getString("status", "");
            writerBidStatus = args.getString("writerBidStatus", "");
            writerWorkStatus = args.getString("writerWorkStatus", "");
            fromMyWork = args.getBoolean("fromMyWork", false);
            String deadline = args.getString("deadline", "");
            String description = args.getString("description", "");
            String studentName = args.getString("studentName", args.getString("student", ""));
            String fileName = args.getString("fileName", "");
            fileUrl = args.getString("fileUrl", "");
            assignmentId = args.getString("assignmentId", "");
            studentId = args.getString("studentId", "");

            currentFileName = fileName;
            newFileName = fileName;

            // Set the UI with actual data
            tvTitle.setText(title);
            tvSubject.setText(subject);
            tvDeadline.setText(deadline);
            tvDescription.setText(description);
            tvStudent.setText(studentName);

            // Set edit fields with same data
            etTitle.setText(title);
            actvSubject.setText(subject, false);
            etDeadline.setText(deadline);
            etDescription.setText(description);

            // Handle attachment display
            if (fileName != null && !fileName.isEmpty()) {
                tvAttachmentName.setText(fileName);
                attachmentRow.setVisibility(View.VISIBLE);
                etAttachmentName.setText(fileName);
            } else {
                attachmentRow.setVisibility(View.GONE);
                etAttachmentName.setText("No file selected");
            }
        } else {
            // Show placeholder if no data provided
            tvTitle.setText("Assignment Details");
            tvDescription.setText("No assignment data available");
            attachmentRow.setVisibility(View.GONE);
        }

        // Fallback to role from Dashboard intent when not explicitly passed in arguments.
        if (!isWriterView && getActivity() != null && getActivity().getIntent() != null) {
            String role = getActivity().getIntent().getStringExtra("role");
            isWriterView = role != null && role.equalsIgnoreCase("writer");
        }

        applyRoleBasedUi();

        // Handle attachment row click
        attachmentRow.setOnClickListener(v -> openAttachment());

        // Handle view button click
        tvView.setOnClickListener(v -> openAttachment());

        // Handle deadline date picker
        etDeadline.setOnClickListener(v -> openDatePicker());

        // Handle file change button
        btnChangeFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        // Handle edit button click
        btnEdit.setOnClickListener(v -> enableEditMode());

        // Handle delete button click
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        // Handle save button click
        btnSave.setOnClickListener(v -> saveChanges());

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> disableEditMode());

        // Handle writer bid action
        btnBidForAssignment.setOnClickListener(v -> placeBidForAssignment());
        btnStartWork.setOnClickListener(v -> startWork());
        btnPickSubmissionFile.setOnClickListener(v -> submissionFilePickerLauncher.launch("*/*"));
        btnSubmitCompletedWork.setOnClickListener(v -> submitCompletedWork());

        return view;
    }

    private void applyRoleBasedUi() {
        if (studentBidsSection == null) {
            return;
        }

        if (isWriterView) {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            studentBidsSection.setVisibility(View.GONE);

            if (fromMyWork) {
                btnBidForAssignment.setVisibility(View.GONE);
                writerWorkSection.setVisibility(View.VISIBLE);
                setupWriterWorkSection();
            } else {
                writerWorkSection.setVisibility(View.GONE);
                if (assignmentId != null && !assignmentId.isEmpty()) {
                    btnBidForAssignment.setVisibility(View.VISIBLE);
                } else {
                    btnBidForAssignment.setVisibility(View.GONE);
                }
            }
        } else {
            btnBidForAssignment.setVisibility(View.GONE);
            writerWorkSection.setVisibility(View.GONE);
            studentBidsSection.setVisibility(View.VISIBLE);
            loadBidsForAssignment();
        }
    }

    private void setupWriterWorkSection() {
        String statusToUse = !TextUtils.isEmpty(writerWorkStatus) ? writerWorkStatus : mapWriterStatusFromCurrentData();

        tvWriterWorkStatus.setText("Status: " + statusToUse);
        btnStartWork.setVisibility("Approved".equalsIgnoreCase(statusToUse) ? View.VISIBLE : View.GONE);

        if ("In Progress".equalsIgnoreCase(statusToUse)) {
            completeWorkSection.setVisibility(View.VISIBLE);
        } else {
            completeWorkSection.setVisibility(View.GONE);
            submissionFileUri = null;
            submissionFileName = "";
            tvSubmissionFileName.setText("No file selected");
            etSubmissionNotes.setText("");
        }
    }

    private String mapWriterStatusFromCurrentData() {
        if (writerBidStatus != null && (writerBidStatus.equalsIgnoreCase("Rejected")
                || writerBidStatus.equalsIgnoreCase("Cancelled"))) {
            return "Rejected";
        }
        if (writerBidStatus == null || writerBidStatus.isEmpty() || writerBidStatus.equalsIgnoreCase("Pending")) {
            return "Pending Confirmation";
        }
        if (assignmentStatus.equalsIgnoreCase("Assigned")) {
            return "Approved";
        }
        if (assignmentStatus.equalsIgnoreCase("In Progress")) {
            return "In Progress";
        }
        if (assignmentStatus.equalsIgnoreCase("Completed")) {
            return "Completed";
        }
        if (assignmentStatus.equalsIgnoreCase("Pending Payment")) {
            return "Pending Payment";
        }
        return "Approved";
    }

    private void startWork() {
        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Starting Work");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        firebaseFirestore.collection("Assignments").document(assignmentId)
                .update("status", "In Progress", "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    assignmentStatus = "In Progress";
                    writerWorkStatus = "In Progress";
                    setupWriterWorkSection();
                    Toast.makeText(getContext(), "Work started", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to start work: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void submitCompletedWork() {
        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }
        if (submissionFileUri == null) {
            Toast.makeText(getContext(), "Please choose a file", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = etSubmissionNotes.getText() == null ? "" : etSubmissionNotes.getText().toString().trim();

        progressDialog.setTitle("Submitting Work");
        progressDialog.setMessage("Uploading file...");
        progressDialog.show();

        String extension = getFileExtension(submissionFileUri);
        String storedName = "Submission_" + UUID.randomUUID() + extension;
        StorageReference submissionRef = firebaseStorage.getReference()
                .child("submissions")
                .child(assignmentId)
                .child(storedName);

        submissionRef.putFile(submissionFileUri)
                .addOnSuccessListener(taskSnapshot -> submissionRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("status", "Pending Payment");
                            updates.put("submissionFileUrl", uri.toString());
                            updates.put("submissionFileName", submissionFileName);
                            updates.put("submissionNotes", notes);
                            updates.put("submissionWriterId", firebaseAuth.getCurrentUser() != null
                                    ? firebaseAuth.getCurrentUser().getUid() : "");
                            updates.put("submissionAt", System.currentTimeMillis());
                            updates.put("updatedAt", System.currentTimeMillis());

                            firebaseFirestore.collection("Assignments").document(assignmentId)
                                    .update(updates)
                                    .addOnSuccessListener(unused -> {
                                        progressDialog.dismiss();
                                        assignmentStatus = "Pending Payment";
                                        writerWorkStatus = "Pending Payment";
                                        setupWriterWorkSection();
                                        Toast.makeText(getContext(), "Work submitted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Failed to save submission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to get file url: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void placeBidForAssignment() {
        if (assignmentId == null || assignmentId.isEmpty()) {
            Toast.makeText(getContext(), "Assignment ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();
        btnBidForAssignment.setEnabled(false);

        firebaseFirestore.collection("Users").document(writerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        btnBidForAssignment.setEnabled(true);
                        Toast.makeText(getContext(), "Writer profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User writer = documentSnapshot.toObject(User.class);
                    String firstName = writer != null && writer.getFirstName() != null ? writer.getFirstName().trim() : "";
                    String lastName = writer != null && writer.getLastName() != null ? writer.getLastName().trim() : "";
                    String writerName = (firstName + " " + lastName).trim();
                    if (writerName.isEmpty()) {
                        writerName = writer != null && writer.getEmail() != null ? writer.getEmail() : "Writer";
                    }

                    BidModel bidModel = BidModel.builder()
                            .assignmentId(assignmentId)
                            .studentId(studentId)
                            .writerId(writerId)
                            .writerName(writerName)
                            .writerEmail(writer != null ? writer.getEmail() : "")
                            .writerMobile(writer != null ? writer.getMobile() : "")
                            .createdAt(System.currentTimeMillis())
                            .status("Pending")
                            .build();

                    bidRepository.createBid(bidModel, new BidRepository.OnBidActionCallback() {
                        @Override
                        public void onSuccess() {
                            btnBidForAssignment.setText("Bid Submitted");
                            btnBidForAssignment.setEnabled(false);
                            Toast.makeText(getContext(), "Bid submitted successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            btnBidForAssignment.setEnabled(true);
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    btnBidForAssignment.setEnabled(true);
                    Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBidsForAssignment() {
        if (assignmentId == null || assignmentId.isEmpty()) {
            tvNoBids.setVisibility(View.VISIBLE);
            recyclerBids.setVisibility(View.GONE);
            tvNoBids.setText("No bids available for this assignment");
            return;
        }

        bidRepository.getBidsByAssignment(assignmentId, new BidRepository.OnBidsLoadedCallback() {
            @Override
            public void onBidsLoaded(java.util.List<BidModel> bids) {
                if (bids == null || bids.isEmpty()) {
                    tvNoBids.setVisibility(View.VISIBLE);
                    recyclerBids.setVisibility(View.GONE);
                    return;
                }

                enrichBidsForStudentView(bids);
            }

            @Override
            public void onError(String errorMessage) {
                tvNoBids.setVisibility(View.VISIBLE);
                recyclerBids.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading bids: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrichBidsForStudentView(java.util.List<BidModel> bids) {
        if (bids == null || bids.isEmpty()) {
            tvNoBids.setVisibility(View.VISIBLE);
            recyclerBids.setVisibility(View.GONE);
            return;
        }

        final int[] remaining = {bids.size()};
        for (BidModel bid : bids) {
            populateBidMetadata(bid, () -> {
                remaining[0]--;
                if (remaining[0] <= 0) {
                    tvNoBids.setVisibility(View.GONE);
                    recyclerBids.setVisibility(View.VISIBLE);
                    bidWriterAdapter.submitList(bids);
                }
            });
        }
    }

    private void populateBidMetadata(BidModel bid, Runnable onComplete) {
        if (bid == null || bid.getWriterId() == null || bid.getWriterId().trim().isEmpty()) {
            onComplete.run();
            return;
        }

        final int[] pendingTasks = {2};
        Runnable finishTask = () -> {
            pendingTasks[0]--;
            if (pendingTasks[0] <= 0) {
                onComplete.run();
            }
        };

        firebaseFirestore.collection("Users").document(bid.getWriterId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Double rating = documentSnapshot.getDouble("rating");
                    if (rating != null) {
                        bid.setRating(rating);
                    }

                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String fullName = ((firstName != null ? firstName.trim() : "") + " "
                            + (lastName != null ? lastName.trim() : "")).trim();
                    if (!fullName.isEmpty()) {
                        bid.setWriterName(fullName);
                    }
                    finishTask.run();
                })
                .addOnFailureListener(e -> finishTask.run());

        firebaseFirestore.collection("Assignments")
                .whereEqualTo("assignedWriterId", bid.getWriterId())
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bid.setCompletedProjectsCount(queryDocumentSnapshots.size());
                    finishTask.run();
                })
                .addOnFailureListener(e -> finishTask.run());
    }

    private void openWriterProfile(BidModel bidModel) {
        if (bidModel == null || bidModel.getWriterId() == null || bidModel.getWriterId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Writer profile not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("writerId", bidModel.getWriterId());

        WriterProfileFragment fragment = new WriterProfileFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void confirmAcceptBid(BidModel bidModel) {
        if (bidModel == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Accept Bid")
                .setMessage("Accept this writer for the assignment? Other pending bids will be rejected.")
                .setPositiveButton("Accept", (dialog, which) -> acceptBid(bidModel))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void acceptBid(BidModel bidModel) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid bid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bidModel.getWriterId() == null || bidModel.getWriterId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid writer", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Accepting Bid");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        bidRepository.acceptBid(
                assignmentId,
                bidModel.getBidId(),
                bidModel.getWriterId().trim(),
                bidModel.getWriterName() != null ? bidModel.getWriterName() : "Writer",
                new BidRepository.OnBidActionCallback() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Bid accepted successfully", Toast.LENGTH_SHORT).show();
                        loadBidsForAssignment();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void confirmRejectBid(BidModel bidModel) {
        if (bidModel == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Bid")
                .setMessage("Reject this writer's bid?")
                .setPositiveButton("Reject", (dialog, which) -> rejectBid(bidModel))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void rejectBid(BidModel bidModel) {
        if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid bid", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Rejecting Bid");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        bidRepository.updateBidStatus(bidModel.getBidId(), "Rejected", new BidRepository.OnBidActionCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Bid rejected", Toast.LENGTH_SHORT).show();
                loadBidsForAssignment();
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSubjectDropdown() {
        String[] subjects = new String[]{
                "Mathematics", "Science", "English", "History", "ICT", "Business"
        };
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                subjects
        );
        actvSubject.setAdapter(subjectAdapter);
        actvSubject.setThreshold(0); // Show dropdown even with no input
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

    private void enableEditMode() {
        isEditMode = true;
        detailsViewLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        selectedFileUri = null;
        newFileUrl = "";
        // Re-setup dropdown to ensure items are visible
        setupSubjectDropdown();
    }

    private void disableEditMode() {
        if (!isEditMode) return; // Prevent unintended calls
        isEditMode = false;
        detailsViewLayout.setVisibility(View.VISIBLE);
        editModeLayout.setVisibility(View.GONE);
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
        selectedFileUri = null;
        newFileName = currentFileName;
        etAttachmentName.setText(currentFileName != null && !currentFileName.isEmpty() ? currentFileName : "No file selected");
    }

    private void saveChanges() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String subject = actvSubject.getText() != null ? actvSubject.getText().toString().trim() : "";
        String deadline = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(subject)) {
            actvSubject.setError("Subject is required");
            return;
        }
        if (TextUtils.isEmpty(deadline)) {
            etDeadline.setError("Deadline is required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            return;
        }

        // If file is selected, upload it first
        if (selectedFileUri != null) {
            uploadFileAndUpdateAssignment(title, subject, deadline, description);
        } else {
            // Update without file
            updateAssignmentInFirestore(title, subject, deadline, description, fileUrl, currentFileName);
        }
    }

    private void uploadFileAndUpdateAssignment(String title, String subject, String deadline, String description) {
        progressDialog.show();

        String fileExtension = getFileExtension(selectedFileUri);
        String fileName = "Assignment_" + UUID.randomUUID() + fileExtension;

        StorageReference fileReference = storageReference.child(studentId).child(fileName);

        fileReference.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        newFileUrl = uri.toString();
                        updateAssignmentInFirestore(title, subject, deadline, description, newFileUrl, newFileName);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error getting file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    long progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading... " + progress + "%");
                });
    }

    private void updateAssignmentInFirestore(String title, String subject, String deadline, String description,
                                            String fileUrl, String fileName) {
        // Update in Firestore
        firebaseFirestore.collection("Assignments").document(assignmentId)
                .update(
                        "title", title,
                        "subject", subject,
                        "deadline", deadline,
                        "description", description,
                        "fileUrl", fileUrl,
                        "fileName", fileName,
                        "updatedAt", System.currentTimeMillis()
                )
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Assignment updated successfully! ✅", Toast.LENGTH_SHORT).show();

                    // Update view mode UI
                    tvTitle.setText(title);
                    tvSubject.setText(subject);
                    tvDeadline.setText(deadline);
                    tvDescription.setText(description);

                    // Update file display
                    if (fileName != null && !fileName.isEmpty()) {
                        tvAttachmentName.setText(fileName);
                        attachmentRow.setVisibility(View.VISIBLE);
                    } else {
                        attachmentRow.setVisibility(View.GONE);
                    }

                    currentFileName = fileName;
                    AssignmentDetailsFragment.this.fileUrl = fileUrl;

                    // Disable edit mode
                    disableEditMode();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error updating assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAssignment())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteAssignment() {
        firebaseFirestore.collection("Assignments").document(assignmentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Assignment deleted successfully! ✅", Toast.LENGTH_SHORT).show();
                    // Navigate back to my assignments
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error deleting assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver cursor = requireContext().getContentResolver();
            try {
                String[] projection = {"_display_name"};
                android.database.Cursor queryCursor = cursor.query(uri, projection, null, null, null);
                if (queryCursor != null && queryCursor.moveToFirst()) {
                    result = queryCursor.getString(queryCursor.getColumnIndexOrThrow("_display_name"));
                    queryCursor.close();
                }
            } catch (Exception e) {
                result = "document";
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

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        android.webkit.MimeTypeMap mimeTypeMap = android.webkit.MimeTypeMap.getSingleton();
        return "." + mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}