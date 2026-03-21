package lk.disontech.campusassist.fragment.fragment.student;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.BidWriterAdapter;
import lk.disontech.campusassist.model.BidModel;
import lk.disontech.campusassist.model.NotificationModel;
import lk.disontech.campusassist.model.User;
import lk.disontech.campusassist.repository.BidRepository;
import lk.disontech.campusassist.repository.NotificationRepository;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.Address;
import lk.payhere.androidsdk.model.Customer;
import lk.payhere.androidsdk.model.InitRequest;

public class AssignmentDetailsFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvTitle, tvSubject, tvDeadline, tvStudent, tvDescription, tvAttachmentName, tvView;
    private TextView tvPaymentAmount, tvDeliveryAddress;
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
    private TextView tvNoBids, tvWriterWorkStatus, tvSubmissionFileName, tvEditDeleteRestrictionMessage;
    private MaterialButton btnEdit, btnDelete, btnSave, btnCancel;
    private MaterialButton btnBidForAssignment, btnStartWork, btnPickSubmissionFile, btnSubmitCompletedWork;
    private MaterialButton btnPayNow;
    private MaterialButton btnOpenDeliveryMap;
    private ProgressBar progressAssignmentDetails;
    private TextInputEditText etSubmissionNotes;
    private LinearLayout completeWorkSection;
    private MaterialCardView paymentSuccessBanner;
    private MaterialCardView submittedWorkCard;
    private MaterialCardView deliveryMapCard;
    private TextView tvSubmittedFileNameStudent;
    private MaterialButton btnViewSubmittedFile;
    private TextView tvWriterNoteStudent;
    private TextView tvMapPreviewHint;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private BidRepository bidRepository;
    private NotificationRepository notificationRepository;
    private BidWriterAdapter bidWriterAdapter;
    private final List<BidModel> currentBidList = new ArrayList<>();
    
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
    private String assignedWriterId = "";
    private String submissionFileUrlForStudent = "";
    private Double paymentAmount = null;
    private String deliveryAddressText = "";
    private Double deliveryLatitude = null;
    private Double deliveryLongitude = null;

    // Edit-mode fields for payment and location
    private TextInputEditText etEditPaymentAmount;
    private MaterialButton btnEditSelectLocation;
    private TextView tvEditSelectedLocation;
    private Double editSelectedLatitude = null;
    private Double editSelectedLongitude = null;
    private String editSelectedAddress = "";

    private GoogleMap deliveryPreviewMap;
    private Marker deliveryLocationMarker;
    private boolean isEditMode = false;
    private boolean isWriterView = false;
    private boolean fromMyWork = false;
    private ProgressDialog progressDialog;
    private static final String PAYHERE_SANDBOX_MERCHANT_ID = "1226330";
    private static final String PAYHERE_SANDBOX_MERCHANT_SECRET = "MTg4NjMxOTM2NTIwNzQ4NjMyNTAzNDkxNTAxNzc3Mzk2MDE2MTk4OA==";

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

    private final ActivityResultLauncher<Intent> payHereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    int statusCode = (data != null)
                            ? data.getIntExtra(PHConstants.INTENT_EXTRA_STATUS, -1)
                            : -1;
                    String payMessage = (data != null)
                            ? data.getStringExtra(PHConstants.INTENT_EXTRA_MESSAGE)
                            : null;

                    // Status 2 = Authorized/Completed; -1 means no explicit status (treat as success)
                    if (statusCode == 2 || statusCode == -1) {
                        handlePaymentSuccess();
                    } else if (statusCode == 1) {
                        Toast.makeText(getContext(),
                                "Payment is pending confirmation." + (payMessage != null ? " " + payMessage : ""),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Payment was not completed." + (payMessage != null ? " " + payMessage : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Payment cancelled.", Toast.LENGTH_SHORT).show();
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
        notificationRepository = new NotificationRepository();

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
        tvPaymentAmount = view.findViewById(R.id.tvPaymentAmount);
        tvDeliveryAddress = view.findViewById(R.id.tvDeliveryAddress);
        btnOpenDeliveryMap = view.findViewById(R.id.btnOpenDeliveryMap);
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
        tvEditDeleteRestrictionMessage = view.findViewById(R.id.tvEditDeleteRestrictionMessage);
        tvWriterWorkStatus = view.findViewById(R.id.tvWriterWorkStatus);
        tvSubmissionFileName = view.findViewById(R.id.tvSubmissionFileName);
        btnStartWork = view.findViewById(R.id.btnStartWork);
        btnPickSubmissionFile = view.findViewById(R.id.btnPickSubmissionFile);
        btnSubmitCompletedWork = view.findViewById(R.id.btnSubmitCompletedWork);
        etSubmissionNotes = view.findViewById(R.id.etSubmissionNotes);
        completeWorkSection = view.findViewById(R.id.completeWorkSection);
        btnPayNow = view.findViewById(R.id.btnPayNow);
        progressAssignmentDetails = view.findViewById(R.id.progressAssignmentDetails);
        paymentSuccessBanner = view.findViewById(R.id.paymentSuccessBanner);
        submittedWorkCard = view.findViewById(R.id.submittedWorkCard);
        deliveryMapCard = view.findViewById(R.id.deliveryMapCard);

        // Edit-mode payment and location fields
        etEditPaymentAmount = view.findViewById(R.id.etEditPaymentAmount);
        btnEditSelectLocation = view.findViewById(R.id.btnEditSelectLocation);
        tvEditSelectedLocation = view.findViewById(R.id.tvEditSelectedLocation);
        tvSubmittedFileNameStudent = view.findViewById(R.id.tvSubmittedFileNameStudent);
        btnViewSubmittedFile = view.findViewById(R.id.btnViewSubmittedFile);
        tvWriterNoteStudent = view.findViewById(R.id.tvWriterNoteStudent);
        tvMapPreviewHint = view.findViewById(R.id.tvMapPreviewHint);

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
            deliveryAddressText = args.getString("deliveryAddress", "");

            if (args.containsKey("paymentAmount")) {
                paymentAmount = args.getDouble("paymentAmount");
            }
            if (args.containsKey("deliveryLatitude")) {
                deliveryLatitude = args.getDouble("deliveryLatitude");
            }
            if (args.containsKey("deliveryLongitude")) {
                deliveryLongitude = args.getDouble("deliveryLongitude");
            }

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
            renderPaymentAndLocation();

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
            renderPaymentAndLocation();
        }

        // Fallback to role from Dashboard intent when not explicitly passed in arguments.
        if (!isWriterView && getActivity() != null && getActivity().getIntent() != null) {
            String role = getActivity().getIntent().getStringExtra("role");
            isWriterView = role != null && role.equalsIgnoreCase("writer");
        }

        applyRoleBasedUi();
        fetchAssignmentDetails();

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

        // Handle edit-mode location picker
        btnEditSelectLocation.setOnClickListener(v -> openLocationPickerForEdit());

        // Listen for location picker result (used by both PostNew and Edit modes)
        getParentFragmentManager().setFragmentResultListener(
                LocationPickerFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    if (result.containsKey(LocationPickerFragment.KEY_LATITUDE)
                            && result.containsKey(LocationPickerFragment.KEY_LONGITUDE)) {
                        editSelectedLatitude = result.getDouble(LocationPickerFragment.KEY_LATITUDE);
                        editSelectedLongitude = result.getDouble(LocationPickerFragment.KEY_LONGITUDE);
                        editSelectedAddress = result.getString(LocationPickerFragment.KEY_ADDRESS, "");
                        if (TextUtils.isEmpty(editSelectedAddress)) {
                            editSelectedAddress = "Lat: " + editSelectedLatitude + ", Lng: " + editSelectedLongitude;
                        }
                        if (tvEditSelectedLocation != null) {
                            tvEditSelectedLocation.setText(editSelectedAddress);
                        }
                    }
                }
        );

        // Restore edit mode layout if returning from location picker
        if (isEditMode) {
            detailsViewLayout.setVisibility(View.GONE);
            editModeLayout.setVisibility(View.VISIBLE);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
            setupSubjectDropdown();
            prefillEditFields();
        }

        // Handle writer bid action
        btnBidForAssignment.setOnClickListener(v -> placeBidForAssignment());
        btnStartWork.setOnClickListener(v -> startWork());
        btnPickSubmissionFile.setOnClickListener(v -> submissionFilePickerLauncher.launch("*/*"));
        btnSubmitCompletedWork.setOnClickListener(v -> submitCompletedWork());
        btnPayNow.setOnClickListener(v -> onPayNowClicked());
        btnOpenDeliveryMap.setOnClickListener(v -> openDeliveryLocationOnMap());

        return view;
    }

    private void applyRoleBasedUi() {
        if (studentBidsSection == null) {
            return;
        }

        if (isWriterView) {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            if (tvEditDeleteRestrictionMessage != null) {
                tvEditDeleteRestrictionMessage.setVisibility(View.GONE);
            }
            studentBidsSection.setVisibility(View.GONE);
            btnPayNow.setVisibility(View.GONE);

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
            applyStudentEditDeleteState();
            studentBidsSection.setVisibility(View.VISIBLE);
            loadBidsForAssignment();

            // Show Pay Now button only when payment is pending
            boolean isPendingPayment = "Pending Payment".equalsIgnoreCase(assignmentStatus);
            boolean isCompleted = "Completed".equalsIgnoreCase(assignmentStatus);

            btnPayNow.setVisibility(isPendingPayment ? View.VISIBLE : View.GONE);

            // Show payment success banner and submitted work when Completed
            if (paymentSuccessBanner != null) {
                paymentSuccessBanner.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
            }
            if (submittedWorkCard != null) {
                submittedWorkCard.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
            }

            // Wire up the "View" button for submitted file
            if (btnViewSubmittedFile != null) {
                btnViewSubmittedFile.setOnClickListener(v -> openSubmittedFile());
            }
        }
    }

    private void fetchAssignmentDetails() {
        if (TextUtils.isEmpty(assignmentId)) {
            return;
        }

        setDetailsLoading(true);
        firebaseFirestore.collection("Assignments").document(assignmentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        setDetailsLoading(false);
                        return;
                    }

                    lk.disontech.campusassist.model.AssignmentModel assignment =
                            documentSnapshot.toObject(lk.disontech.campusassist.model.AssignmentModel.class);
                    if (assignment != null) {
                        bindAssignmentFromBackend(assignment);
                        applyRoleBasedUi();
                    }
                    setDetailsLoading(false);
                })
                .addOnFailureListener(e -> {
                    setDetailsLoading(false);
                    Toast.makeText(getContext(), "Failed to load assignment details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindAssignmentFromBackend(lk.disontech.campusassist.model.AssignmentModel assignment) {
        assignmentStatus = safe(assignment.getStatus());
        assignmentId = safe(assignment.getAssignmentId());
        studentId = safe(assignment.getStudentId());
        fileUrl = safe(assignment.getFileUrl());
        assignedWriterId = safe(assignment.getAssignedWriterId());
        submissionFileUrlForStudent = safe(assignment.getSubmissionFileUrl());
        paymentAmount = assignment.getPaymentAmount();
        deliveryAddressText = safe(assignment.getDeliveryAddress());
        deliveryLatitude = assignment.getDeliveryLatitude();
        deliveryLongitude = assignment.getDeliveryLongitude();

        String title = safe(assignment.getTitle());
        String subject = safe(assignment.getSubject());
        String deadline = safe(assignment.getDeadline());
        String description = safe(assignment.getDescription());
        String studentName = safe(assignment.getStudentName());
        String fileName = safe(assignment.getFileName());

        tvTitle.setText(title);
        tvSubject.setText(subject);
        tvDeadline.setText(deadline);
        tvDescription.setText(description);
        tvStudent.setText(studentName);

        etTitle.setText(title);
        actvSubject.setText(subject, false);
        etDeadline.setText(deadline);
        etDescription.setText(description);
        renderPaymentAndLocation();

        currentFileName = fileName;
        if (!TextUtils.isEmpty(fileName)) {
            tvAttachmentName.setText(fileName);
            etAttachmentName.setText(fileName);
            attachmentRow.setVisibility(View.VISIBLE);
        } else {
            attachmentRow.setVisibility(View.GONE);
            etAttachmentName.setText("No file selected");
        }

        // Writer's work submission fields (writer-side inputs)
        String writerSubmissionFileName = safe(assignment.getSubmissionFileName());
        String writerSubmissionNotes = safe(assignment.getSubmissionNotes());
        submissionFileName = writerSubmissionFileName;
        tvSubmissionFileName.setText(TextUtils.isEmpty(writerSubmissionFileName)
                ? "No file selected" : writerSubmissionFileName);
        etSubmissionNotes.setText(writerSubmissionNotes);

        // Student-facing submitted work section
        String subFileName = safe(assignment.getSubmissionFileName());
        String subNotes = safe(assignment.getSubmissionNotes());
        if (tvSubmittedFileNameStudent != null) {
            tvSubmittedFileNameStudent.setText(
                    TextUtils.isEmpty(subFileName) ? "No file available" : subFileName);
        }
        if (tvWriterNoteStudent != null) {
            tvWriterNoteStudent.setText(
                    TextUtils.isEmpty(subNotes) ? "No notes from writer." : subNotes);
        }
    }

    private void renderPaymentAndLocation() {
        if (tvPaymentAmount != null) {
            if (paymentAmount != null && paymentAmount > 0) {
                tvPaymentAmount.setText(String.format(Locale.US, "LKR %,.2f", paymentAmount));
            } else {
                tvPaymentAmount.setText("Not set");
            }
        }

        if (tvDeliveryAddress != null) {
            tvDeliveryAddress.setText(TextUtils.isEmpty(deliveryAddressText) ? "Not provided" : deliveryAddressText);
        }

        if (btnOpenDeliveryMap != null) {
            boolean hasCoordinates = deliveryLatitude != null && deliveryLongitude != null;
            boolean hasAddress = !TextUtils.isEmpty(deliveryAddressText);
            btnOpenDeliveryMap.setVisibility((hasCoordinates || hasAddress) ? View.VISIBLE : View.GONE);
        }

        boolean hasCoordinates = deliveryLatitude != null && deliveryLongitude != null;
        if (deliveryMapCard != null) {
            deliveryMapCard.setVisibility(hasCoordinates ? View.VISIBLE : View.GONE);
        }
        if (hasCoordinates) {
            ensureDeliveryMapPreview();
            updateDeliveryMapPreview();
            if (tvMapPreviewHint != null) {
                tvMapPreviewHint.setText(TextUtils.isEmpty(deliveryAddressText)
                        ? "Delivery location preview"
                        : deliveryAddressText);
            }
        }
    }

    private void ensureDeliveryMapPreview() {
        if (deliveryLatitude == null || deliveryLongitude == null || !isAdded()) {
            return;
        }

        FragmentManager childManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) childManager.findFragmentById(R.id.deliveryMapContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            childManager.beginTransaction()
                    .replace(R.id.deliveryMapContainer, mapFragment, "delivery_preview_map")
                    .commitNowAllowingStateLoss();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        deliveryPreviewMap = googleMap;
        deliveryPreviewMap.getUiSettings().setMapToolbarEnabled(false);
        deliveryPreviewMap.getUiSettings().setAllGesturesEnabled(false);
        updateDeliveryMapPreview();
    }

    private void updateDeliveryMapPreview() {
        if (deliveryPreviewMap == null || deliveryLatitude == null || deliveryLongitude == null) {
            return;
        }

        LatLng deliveryLatLng = new LatLng(deliveryLatitude, deliveryLongitude);
        if (deliveryLocationMarker == null) {
            deliveryLocationMarker = deliveryPreviewMap.addMarker(
                    new MarkerOptions().position(deliveryLatLng).title("Delivery Location")
            );
        } else {
            deliveryLocationMarker.setPosition(deliveryLatLng);
        }
        deliveryPreviewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLatLng, 15f));
    }

    private void setDetailsLoading(boolean isLoading) {
        if (progressAssignmentDetails != null) {
            progressAssignmentDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnEdit != null) {
            btnEdit.setEnabled(!isLoading);
        }
        if (btnDelete != null) {
            btnDelete.setEnabled(!isLoading);
        }
        if (btnBidForAssignment != null) {
            btnBidForAssignment.setEnabled(!isLoading);
        }
        if (btnStartWork != null) {
            btnStartWork.setEnabled(!isLoading);
        }
        if (btnSubmitCompletedWork != null) {
            btnSubmitCompletedWork.setEnabled(!isLoading);
        }
        if (btnPayNow != null) {
            btnPayNow.setEnabled(!isLoading);
        }
    }

    private void applyStudentEditDeleteState() {
        if (btnEdit == null || btnDelete == null) {
            return;
        }

        boolean canEditDelete = isStudentEditDeleteAllowed();
        btnEdit.setVisibility(canEditDelete ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(canEditDelete ? View.VISIBLE : View.GONE);

        if (tvEditDeleteRestrictionMessage != null) {
            if (canEditDelete) {
                tvEditDeleteRestrictionMessage.setVisibility(View.GONE);
            } else {
                String statusLabel = TextUtils.isEmpty(assignmentStatus) ? "This" : assignmentStatus;
                tvEditDeleteRestrictionMessage.setText(statusLabel + " assignments cannot be edited or deleted.");
                tvEditDeleteRestrictionMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isStudentEditDeleteAllowed() {
        return !isWriterView && !TextUtils.isEmpty(assignmentStatus) && assignmentStatus.equalsIgnoreCase("Open");
    }

    private void setupWriterWorkSection() {
        String statusToUse = !TextUtils.isEmpty(writerWorkStatus) ? writerWorkStatus : mapWriterStatusFromCurrentData();
        boolean isInProgress = "In Progress".equalsIgnoreCase(statusToUse);
        boolean isPendingPayment = "Pending Payment".equalsIgnoreCase(statusToUse);
        boolean isCompleted = "Completed".equalsIgnoreCase(statusToUse);

        tvWriterWorkStatus.setText("Status: " + statusToUse);
        btnStartWork.setVisibility("Approved".equalsIgnoreCase(statusToUse) ? View.VISIBLE : View.GONE);

        // Keep submitted details visible after work is submitted.
        if (isInProgress || isPendingPayment || isCompleted) {
            completeWorkSection.setVisibility(View.VISIBLE);

            if (isInProgress) {
                btnPickSubmissionFile.setVisibility(View.VISIBLE);
                btnSubmitCompletedWork.setVisibility(View.VISIBLE);
                etSubmissionNotes.setEnabled(true);
                tvSubmissionFileName.setOnClickListener(null);
                tvSubmissionFileName.setClickable(false);
            } else {
                // Post-submission states are read-only for writer.
                btnPickSubmissionFile.setVisibility(View.GONE);
                btnSubmitCompletedWork.setVisibility(View.GONE);
                etSubmissionNotes.setEnabled(false);

                if (!TextUtils.isEmpty(submissionFileUrlForStudent)) {
                    tvSubmissionFileName.setOnClickListener(v -> openSubmittedFile());
                    tvSubmissionFileName.setClickable(true);
                } else {
                    tvSubmissionFileName.setOnClickListener(null);
                    tvSubmissionFileName.setClickable(false);
                }
            }
        } else {
            completeWorkSection.setVisibility(View.GONE);
            submissionFileUri = null;
            submissionFileName = "";
            tvSubmissionFileName.setText("No file selected");
            etSubmissionNotes.setText("");
            tvSubmissionFileName.setOnClickListener(null);
            tvSubmissionFileName.setClickable(false);
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
                    sendNotification(
                            studentId,
                            "student",
                            "Assignment In Progress",
                            "Work has started on your assignment " + getAssignmentTitleText() + ".",
                            assignmentId,
                            getAssignmentTitleText(),
                            "In Progress",
                            ""
                    );
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
                                        submissionFileUrlForStudent = uri.toString();
                                        assignmentStatus = "Pending Payment";
                                        writerWorkStatus = "Pending Payment";
                                        setupWriterWorkSection();
                                        sendNotification(
                                                studentId,
                                                "student",
                                                "Work Submitted",
                                                "Work for your assignment " + getAssignmentTitleText() + " was submitted and is now Pending Payment.",
                                                assignmentId,
                                                getAssignmentTitleText(),
                                                "Pending Payment",
                                                ""
                                        );
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
                    String firstName = documentSnapshot.getString("firstName") != null
                            ? documentSnapshot.getString("firstName").trim() : "";
                    String lastName = documentSnapshot.getString("lastName") != null
                            ? documentSnapshot.getString("lastName").trim() : "";
                    String writerName = (firstName + " " + lastName).trim();
                    if (writerName.isEmpty()) {
                        String email = documentSnapshot.getString("email");
                        writerName = email != null && !email.trim().isEmpty() ? email : "Writer";
                    }
                    final String writerDisplayName = writerName;

                    BidModel bidModel = BidModel.builder()
                            .assignmentId(assignmentId)
                            .studentId(studentId)
                            .writerId(writerId)
                            .writerName(writerDisplayName)
                            .writerEmail(documentSnapshot.getString("email") != null ? documentSnapshot.getString("email") : "")
                            .writerMobile(documentSnapshot.getString("mobile") != null ? documentSnapshot.getString("mobile") : "")
                            .createdAt(System.currentTimeMillis())
                            .status("Pending")
                            .build();

                    bidRepository.createBid(bidModel, new BidRepository.OnBidActionCallback() {
                        @Override
                        public void onSuccess() {
                            btnBidForAssignment.setText("Bid Submitted");
                            btnBidForAssignment.setEnabled(false);
                            sendNotification(
                                    studentId,
                                    "student",
                                    "New Bid Received",
                                    writerDisplayName + " has bid on your assignment " + getAssignmentTitleText() + ".",
                                    assignmentId,
                                    getAssignmentTitleText(),
                                    assignmentStatus,
                                    ""
                            );
                            sendNotification(
                                    writerId,
                                    "writer",
                                    "Bid Submitted",
                                    "You successfully bid on " + getAssignmentTitleText() + " posted by " + getStudentNameText() + ".",
                                    assignmentId,
                                    getAssignmentTitleText(),
                                    assignmentStatus,
                                    "Pending Confirmation"
                            );
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
        setDetailsLoading(true);
        if (assignmentId == null || assignmentId.isEmpty()) {
            tvNoBids.setVisibility(View.VISIBLE);
            recyclerBids.setVisibility(View.GONE);
            tvNoBids.setText("No bids available for this assignment");
            setDetailsLoading(false);
            return;
        }

        bidRepository.getBidsByAssignment(assignmentId, new BidRepository.OnBidsLoadedCallback() {
            @Override
            public void onBidsLoaded(java.util.List<BidModel> bids) {
                if (bids == null || bids.isEmpty()) {
                    tvNoBids.setVisibility(View.VISIBLE);
                    recyclerBids.setVisibility(View.GONE);
                    setDetailsLoading(false);
                    return;
                }

                enrichBidsForStudentView(bids);
            }

            @Override
            public void onError(String errorMessage) {
                tvNoBids.setVisibility(View.VISIBLE);
                recyclerBids.setVisibility(View.GONE);
                setDetailsLoading(false);
                Toast.makeText(getContext(), "Error loading bids: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrichBidsForStudentView(java.util.List<BidModel> bids) {
        if (bids == null || bids.isEmpty()) {
            tvNoBids.setVisibility(View.VISIBLE);
            recyclerBids.setVisibility(View.GONE);
            currentBidList.clear();
            return;
        }

        currentBidList.clear();
        currentBidList.addAll(bids);

        final int[] remaining = {bids.size()};
        for (BidModel bid : bids) {
            populateBidMetadata(bid, () -> {
                remaining[0]--;
                if (remaining[0] <= 0) {
                    tvNoBids.setVisibility(View.GONE);
                    recyclerBids.setVisibility(View.VISIBLE);
                    bidWriterAdapter.submitList(bids);
                    setDetailsLoading(false);
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
                        sendNotification(
                                bidModel.getWriterId(),
                                "writer",
                                "Bid Accepted",
                                "Your bid for " + getAssignmentTitleText() + " was accepted by " + getStudentNameText() + ".",
                                assignmentId,
                                getAssignmentTitleText(),
                                "Assigned",
                                "Approved"
                        );

                        for (BidModel otherBid : currentBidList) {
                            if (otherBid == null || otherBid.getBidId() == null || otherBid.getWriterId() == null) {
                                continue;
                            }
                            if (otherBid.getBidId().equals(bidModel.getBidId())) {
                                continue;
                            }
                            String otherStatus = otherBid.getStatus();
                            if (otherStatus == null || otherStatus.equalsIgnoreCase("Pending")) {
                                sendNotification(
                                        otherBid.getWriterId(),
                                        "writer",
                                        "Bid Rejected",
                                        "Your bid for " + getAssignmentTitleText() + " was rejected by " + getStudentNameText() + ".",
                                        assignmentId,
                                        getAssignmentTitleText(),
                                        "Assigned",
                                        "Rejected"
                                );
                            }
                        }

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
                sendNotification(
                        bidModel.getWriterId(),
                        "writer",
                        "Bid Rejected",
                        "Your bid for " + getAssignmentTitleText() + " was rejected by " + getStudentNameText() + ".",
                        assignmentId,
                        getAssignmentTitleText(),
                        assignmentStatus,
                        "Rejected"
                );
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

    private void openLocationPickerForEdit() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new LocationPickerFragment())
                .addToBackStack(null)
                .commit();
    }

    private void prefillEditFields() {
        // Pre-fill payment amount
        if (etEditPaymentAmount != null) {
            if (paymentAmount != null && paymentAmount > 0) {
                etEditPaymentAmount.setText(String.format(Locale.US, "%.2f", paymentAmount));
            } else {
                etEditPaymentAmount.setText("");
            }
        }
        // Pre-fill delivery address
        if (!TextUtils.isEmpty(editSelectedAddress)) {
            // Already updated by location picker result – don't overwrite
        } else {
            editSelectedLatitude = deliveryLatitude;
            editSelectedLongitude = deliveryLongitude;
            editSelectedAddress = deliveryAddressText != null ? deliveryAddressText : "";
        }
        if (tvEditSelectedLocation != null) {
            tvEditSelectedLocation.setText(
                    TextUtils.isEmpty(editSelectedAddress) ? "No address selected" : editSelectedAddress);
        }
    }

    private void enableEditMode() {
        if (!isStudentEditDeleteAllowed()) {
            Toast.makeText(getContext(), "Only open assignments can be edited", Toast.LENGTH_SHORT).show();
            return;
        }

        isEditMode = true;
        // Reset edit-location state from current saved values
        editSelectedLatitude = deliveryLatitude;
        editSelectedLongitude = deliveryLongitude;
        editSelectedAddress = deliveryAddressText != null ? deliveryAddressText : "";

        detailsViewLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        selectedFileUri = null;
        newFileUrl = "";
        prefillEditFields();
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
        // Reset edit-location state
        editSelectedLatitude = null;
        editSelectedLongitude = null;
        editSelectedAddress = "";
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

        // Validate payment amount
        String paymentAmountText = etEditPaymentAmount.getText() != null
                ? etEditPaymentAmount.getText().toString().trim() : "";
        double newPaymentAmount;
        if (TextUtils.isEmpty(paymentAmountText)) {
            etEditPaymentAmount.setError("Payment amount is required");
            etEditPaymentAmount.requestFocus();
            return;
        }
        try {
            newPaymentAmount = Double.parseDouble(paymentAmountText);
        } catch (NumberFormatException e) {
            etEditPaymentAmount.setError("Enter a valid numeric amount");
            etEditPaymentAmount.requestFocus();
            return;
        }
        if (newPaymentAmount <= 0) {
            etEditPaymentAmount.setError("Amount must be greater than 0");
            etEditPaymentAmount.requestFocus();
            return;
        }
        etEditPaymentAmount.setError(null);

        // Delivery address – keep existing if not changed
        Double newDeliveryLatitude = editSelectedLatitude != null ? editSelectedLatitude : deliveryLatitude;
        Double newDeliveryLongitude = editSelectedLongitude != null ? editSelectedLongitude : deliveryLongitude;
        String newDeliveryAddress = !TextUtils.isEmpty(editSelectedAddress) ? editSelectedAddress : deliveryAddressText;

        // If file is selected, upload it first
        if (selectedFileUri != null) {
            uploadFileAndUpdateAssignment(title, subject, deadline, description,
                    newPaymentAmount, newDeliveryAddress, newDeliveryLatitude, newDeliveryLongitude);
        } else {
            // Update without file
            updateAssignmentInFirestore(title, subject, deadline, description, fileUrl, currentFileName,
                    newPaymentAmount, newDeliveryAddress, newDeliveryLatitude, newDeliveryLongitude);
        }
    }

    private void uploadFileAndUpdateAssignment(String title, String subject, String deadline, String description,
                                               double newPaymentAmount, String newDeliveryAddress,
                                               Double newDeliveryLatitude, Double newDeliveryLongitude) {
        progressDialog.show();

        String fileExtension = getFileExtension(selectedFileUri);
        String fileName = "Assignment_" + UUID.randomUUID() + fileExtension;

        StorageReference fileReference = storageReference.child(studentId).child(fileName);

        fileReference.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        newFileUrl = uri.toString();
                        updateAssignmentInFirestore(title, subject, deadline, description, newFileUrl, newFileName,
                                newPaymentAmount, newDeliveryAddress, newDeliveryLatitude, newDeliveryLongitude);
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
                                            String fileUrl, String fileName,
                                            double newPaymentAmount, String newDeliveryAddress,
                                            Double newDeliveryLatitude, Double newDeliveryLongitude) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("subject", subject);
        updates.put("deadline", deadline);
        updates.put("description", description);
        updates.put("fileUrl", fileUrl);
        updates.put("fileName", fileName);
        updates.put("paymentAmount", newPaymentAmount);
        updates.put("deliveryAddress", newDeliveryAddress != null ? newDeliveryAddress : "");
        if (newDeliveryLatitude != null) updates.put("deliveryLatitude", newDeliveryLatitude);
        if (newDeliveryLongitude != null) updates.put("deliveryLongitude", newDeliveryLongitude);
        updates.put("updatedAt", System.currentTimeMillis());

        // Update in Firestore
        firebaseFirestore.collection("Assignments").document(assignmentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Assignment updated successfully! ✅", Toast.LENGTH_SHORT).show();

                    // Update view mode UI
                    tvTitle.setText(title);
                    tvSubject.setText(subject);
                    tvDeadline.setText(deadline);
                    tvDescription.setText(description);

                    // Update in-memory payment & location values
                    paymentAmount = newPaymentAmount;
                    deliveryAddressText = newDeliveryAddress != null ? newDeliveryAddress : "";
                    if (newDeliveryLatitude != null) AssignmentDetailsFragment.this.deliveryLatitude = newDeliveryLatitude;
                    if (newDeliveryLongitude != null) AssignmentDetailsFragment.this.deliveryLongitude = newDeliveryLongitude;
                    renderPaymentAndLocation();

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
        if (!isStudentEditDeleteAllowed()) {
            Toast.makeText(getContext(), "Only open assignments can be deleted", Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void onPayNowClicked() {
        if (!"Pending Payment".equalsIgnoreCase(assignmentStatus)) {
            Toast.makeText(getContext(), "Payment is not required for this assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InitRequest payment = new InitRequest();
            payment.setSandBox(true);
            payment.setMerchantId(PAYHERE_SANDBOX_MERCHANT_ID);
            payment.setMerchantSecret(PAYHERE_SANDBOX_MERCHANT_SECRET);
            payment.setNotifyUrl("https://example.com/payhere/notify");
            payment.setOrderId("ASSIGN-" + assignmentId + "-" + System.currentTimeMillis());
            payment.setItemsDescription("Assignment payment - " + getAssignmentTitleText());
            payment.setAmount(1000.00);
            payment.setCurrency("LKR");

            Customer customer = new Customer();
            customer.setFirstName("Student");
            customer.setLastName("User");
            customer.setEmail(firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().getEmail() != null
                    ? firebaseAuth.getCurrentUser().getEmail() : "student@example.com");
            customer.setPhone("0770000000");

            Address billingAddress = customer.getAddress();
            if (billingAddress != null) {
                billingAddress.setAddress("No 1, Main Street");
                billingAddress.setCity("Colombo");
                billingAddress.setCountry("Sri Lanka");
            }

            Address deliveryAddress = new Address();
            deliveryAddress.setAddress("No 1, Main Street");
            deliveryAddress.setCity("Colombo");
            deliveryAddress.setCountry("Sri Lanka");
            customer.setDeliveryAddress(deliveryAddress);

            payment.setCustomer(customer);

            Intent payHereIntent = new Intent(requireContext(), PHMainActivity.class);
            payHereIntent.putExtra(PHConstants.INTENT_EXTRA_DATA, payment);
            payHereLauncher.launch(payHereIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentSuccess() {
        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Completing Payment");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Completed");
        updates.put("paymentStatus", "Completed");
        updates.put("paymentCompletedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        firebaseFirestore.collection("Assignments").document(assignmentId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    // Reload the assignment from Firestore to get latest submission details
                    firebaseFirestore.collection("Assignments").document(assignmentId).get()
                            .addOnSuccessListener(doc -> {
                                progressDialog.dismiss();

                                if (doc.exists()) {
                                    lk.disontech.campusassist.model.AssignmentModel assignment =
                                            doc.toObject(lk.disontech.campusassist.model.AssignmentModel.class);
                                    if (assignment != null) {
                                        submissionFileUrlForStudent = safe(assignment.getSubmissionFileUrl());
                                        String subFileName = safe(assignment.getSubmissionFileName());
                                        String subNotes = safe(assignment.getSubmissionNotes());
                                        String writerIdForNotif = safe(assignment.getAssignedWriterId());

                                        // Update student-facing submitted work UI
                                        if (tvSubmittedFileNameStudent != null) {
                                            tvSubmittedFileNameStudent.setText(
                                                    TextUtils.isEmpty(subFileName) ? "No file available" : subFileName);
                                        }
                                        if (tvWriterNoteStudent != null) {
                                            tvWriterNoteStudent.setText(
                                                    TextUtils.isEmpty(subNotes) ? "No notes from writer." : subNotes);
                                        }

                                        // Send notification to student
                                        sendNotification(
                                                studentId,
                                                "student",
                                                "Payment Completed",
                                                "Payment was completed for " + getAssignmentTitleText()
                                                        + ", and the assignment is now completed.",
                                                assignmentId,
                                                getAssignmentTitleText(),
                                                "Completed",
                                                ""
                                        );

                                        // Send notification to writer
                                        if (!TextUtils.isEmpty(writerIdForNotif)) {
                                            sendNotification(
                                                    writerIdForNotif,
                                                    "writer",
                                                    "Payment Completed",
                                                    "Payment was completed for " + getAssignmentTitleText()
                                                            + ", and the assignment is now completed.",
                                                    assignmentId,
                                                    getAssignmentTitleText(),
                                                    "Completed",
                                                    "Completed"
                                            );
                                        } else if (!TextUtils.isEmpty(assignedWriterId)) {
                                            sendNotification(
                                                    assignedWriterId,
                                                    "writer",
                                                    "Payment Completed",
                                                    "Payment was completed for " + getAssignmentTitleText()
                                                            + ", and the assignment is now completed.",
                                                    assignmentId,
                                                    getAssignmentTitleText(),
                                                    "Completed",
                                                    "Completed"
                                            );
                                        }
                                    }
                                }

                                // Update local state and refresh UI
                                assignmentStatus = "Completed";
                                btnPayNow.setVisibility(View.GONE);
                                if (paymentSuccessBanner != null) {
                                    paymentSuccessBanner.setVisibility(View.VISIBLE);
                                }
                                if (submittedWorkCard != null) {
                                    submittedWorkCard.setVisibility(View.VISIBLE);
                                }
                                applyStudentEditDeleteState();

                                // Show success dialog
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("✅ Payment Successful")
                                        .setMessage("Payment completed successfully.\n\nYour assignment is now complete. You can view and download the submitted work below.")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                // Status update succeeded; still update UI
                                assignmentStatus = "Completed";
                                btnPayNow.setVisibility(View.GONE);
                                if (paymentSuccessBanner != null) {
                                    paymentSuccessBanner.setVisibility(View.VISIBLE);
                                }
                                if (submittedWorkCard != null) {
                                    submittedWorkCard.setVisibility(View.VISIBLE);
                                }

                                // Notifications with cached IDs
                                sendNotification(studentId, "student",
                                        "Payment Completed",
                                        "Payment was completed for " + getAssignmentTitleText()
                                                + ", and the assignment is now completed.",
                                        assignmentId, getAssignmentTitleText(), "Completed", "");

                                if (!TextUtils.isEmpty(assignedWriterId)) {
                                    sendNotification(assignedWriterId, "writer",
                                            "Payment Completed",
                                            "Payment was completed for " + getAssignmentTitleText()
                                                    + ", and the assignment is now completed.",
                                            assignmentId, getAssignmentTitleText(), "Completed", "Completed");
                                }

                                Toast.makeText(getContext(), "Payment completed successfully.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(),
                            "Payment recorded but failed to update status: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void openSubmittedFile() {
        if (TextUtils.isEmpty(submissionFileUrlForStudent)) {
            Toast.makeText(getContext(), "No submitted file available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(submissionFileUrlForStudent));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openDeliveryLocationOnMap() {
        if (deliveryLatitude == null || deliveryLongitude == null) {
            if (TextUtils.isEmpty(deliveryAddressText)) {
                Toast.makeText(getContext(), "Delivery location not available", Toast.LENGTH_SHORT).show();
                return;
            }

            String addressQuery = Uri.encode(deliveryAddressText);
            Intent addressIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + addressQuery));

            if (addressIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(addressIntent);
            } else {
                Toast.makeText(getContext(), "No map app found", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String coordQuery = deliveryLatitude + "," + deliveryLongitude;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(coordQuery)));

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(getContext(), "No map app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        deliveryPreviewMap = null;
        deliveryLocationMarker = null;
        FragmentManager childManager = getChildFragmentManager();
        Fragment mapFragment = childManager.findFragmentByTag("delivery_preview_map");
        if (mapFragment != null) {
            childManager.beginTransaction().remove(mapFragment).commitAllowingStateLoss();
        }
        super.onDestroyView();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void sendNotification(String recipientUserId,
                                  String recipientRole,
                                  String title,
                                  String message,
                                  String assignmentId,
                                  String assignmentTitle,
                                  String assignmentStatus,
                                  String writerWorkStatus) {
        if (recipientUserId == null || recipientUserId.trim().isEmpty()) {
            return;
        }

        NotificationModel notificationModel = NotificationModel.builder()
                .recipientUserId(recipientUserId)
                .recipientRole(recipientRole)
                .title(title)
                .message(message)
                .assignmentId(assignmentId)
                .assignmentTitle(assignmentTitle)
                .assignmentStatus(assignmentStatus)
                .writerWorkStatus(writerWorkStatus)
                .createdAt(System.currentTimeMillis())
                .read(false)
                .build();

        notificationRepository.createNotification(notificationModel, new NotificationRepository.OnNotificationActionCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private String getAssignmentTitleText() {
        return tvTitle != null && tvTitle.getText() != null && !tvTitle.getText().toString().trim().isEmpty()
                ? tvTitle.getText().toString().trim()
                : "this assignment";
    }

    private String getStudentNameText() {
        return tvStudent != null && tvStudent.getText() != null && !tvStudent.getText().toString().trim().isEmpty()
                ? tvStudent.getText().toString().trim()
                : "the student";
    }
}

