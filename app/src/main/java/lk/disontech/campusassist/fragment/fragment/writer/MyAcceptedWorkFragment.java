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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.AcceptedWorkAdapter;
import lk.disontech.campusassist.fragment.fragment.student.AssignmentDetailsFragment;
import lk.disontech.campusassist.model.AcceptedWorkModel;
import lk.disontech.campusassist.model.AssignmentModel;
import lk.disontech.campusassist.model.BidModel;
import lk.disontech.campusassist.model.NotificationModel;
import lk.disontech.campusassist.repository.BidRepository;
import lk.disontech.campusassist.repository.NotificationRepository;

public class MyAcceptedWorkFragment extends Fragment {

    private RecyclerView recyclerAcceptedWork;
    private TextInputEditText etSearchMyWork;
    private MaterialAutoCompleteTextView actvStatusFilter;
    private ProgressBar progressMyWork;
    private TextView tvMyWorkLoading;
    private TextView tvMyWorkEmpty;
    private AcceptedWorkAdapter acceptedWorkAdapter;
    private final List<AcceptedWorkModel> acceptedWorkList = new ArrayList<>();
    private final List<AcceptedWorkModel> fullWorkList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private BidRepository bidRepository;
    private NotificationRepository notificationRepository;
    private String selectedStatusFilter = "All";
    private ArrayAdapter<String> statusFilterAdapter;
    private final List<String> statusFilterOptions = new ArrayList<>();

    public MyAcceptedWorkFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_accepted_work, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerAcceptedWork = view.findViewById(R.id.recyclerAcceptedWork);
        etSearchMyWork = view.findViewById(R.id.etSearchMyWork);
        actvStatusFilter = view.findViewById(R.id.actvStatusFilter);
        progressMyWork = view.findViewById(R.id.progressMyWork);
        tvMyWorkLoading = view.findViewById(R.id.tvMyWorkLoading);
        tvMyWorkEmpty = view.findViewById(R.id.tvMyWorkEmpty);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        bidRepository = new BidRepository();
        notificationRepository = new NotificationRepository();

        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        recyclerAcceptedWork.setLayoutManager(new LinearLayoutManager(getContext()));

        acceptedWorkAdapter = new AcceptedWorkAdapter(getContext(), acceptedWorkList,
                new AcceptedWorkAdapter.MyWorkActionListener() {
                    @Override
                    public void onViewUpdateClick(AcceptedWorkModel model) {
                        if (!model.canOpenDetails()) {
                            Toast.makeText(getContext(), "Rejected or canceled bids cannot be opened", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bundle b = new Bundle();
                        b.putString("title", model.getTitle());
                        b.putString("subject", model.getSubject());
                        b.putString("studentName", model.getStudentName());
                        b.putString("deadline", model.getDueDate());
                        b.putString("status", model.getAssignmentStatus());
                        b.putString("description", model.getDescription());
                        b.putString("fileUrl", model.getFileUrl());
                        b.putString("fileName", model.getFileName());
                        b.putString("assignmentId", model.getAssignmentId());
                        b.putString("studentId", model.getStudentId());
                        b.putBoolean("isWriterView", true);
                        b.putBoolean("fromMyWork", true);
                        b.putString("writerWorkStatus", model.getStatus());
                        b.putString("writerBidStatus", model.getBidStatus());

                        AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
                        fragment.setArguments(b);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.dashboardContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onCancelBidClick(AcceptedWorkModel model) {
                        showCancelBidConfirmation(model);
                    }
                });

        recyclerAcceptedWork.setAdapter(acceptedWorkAdapter);
        setupFilters();
        loadMyWork();

        return view;
    }

    private void setupFilters() {
        applyInitialFilterFromArguments();
        refreshStatusFilterOptions();
        statusFilterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                statusFilterOptions
        );
        actvStatusFilter.setAdapter(statusFilterAdapter);
        actvStatusFilter.setText(selectedStatusFilter, false);
        actvStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatusFilter = statusFilterOptions.get(position);
            showLoadingState(true, "Applying filter...");
            recyclerAcceptedWork.post(() -> {
                applyFilters();
                showLoadingState(false, "");
            });
        });

        etSearchMyWork.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showLoadingState(true, "Applying filter...");
                recyclerAcceptedWork.post(() -> {
                    applyFilters();
                    showLoadingState(false, "");
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applyInitialFilterFromArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        String initialFilter = safe(args.getString("initialStatusFilter"));
        if (!initialFilter.isEmpty()) {
            selectedStatusFilter = initialFilter;
        }
    }

    private void loadMyWork() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingState(true, "Loading work...");
        String writerId = firebaseAuth.getCurrentUser().getUid();
        bidRepository.getBidsByWriter(writerId, new BidRepository.OnBidsLoadedCallback() {
            @Override
            public void onBidsLoaded(List<BidModel> bids) {
                if (bids == null || bids.isEmpty()) {
                    fullWorkList.clear();
                    acceptedWorkList.clear();
                    acceptedWorkAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    showLoadingState(false, "");
                    return;
                }

                buildMyWorkListFromBids(bids);
            }

            @Override
            public void onError(String errorMessage) {
                showLoadingState(false, "");
                updateEmptyState();
                Toast.makeText(getContext(), "Error loading bids: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildMyWorkListFromBids(List<BidModel> bids) {
        List<AcceptedWorkModel> generated = new ArrayList<>();
        final int[] remaining = {bids.size()};

        for (BidModel bid : bids) {
            String assignmentId = bid.getAssignmentId();
            if (TextUtils.isEmpty(assignmentId)) {
                remaining[0]--;
                if (remaining[0] == 0) {
                    onMyWorkListReady(generated);
                }
                continue;
            }

            firebaseFirestore.collection("Assignments").document(assignmentId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        AssignmentModel assignment = documentSnapshot.toObject(AssignmentModel.class);
                        if (assignment != null) {
                            String mappedStatus = mapWriterStatus(bid, assignment);
                            boolean isRejected = "Rejected".equalsIgnoreCase(mappedStatus)
                                    || "Bid Canceled".equalsIgnoreCase(mappedStatus);
                            boolean canCancel = "Pending Confirmation".equalsIgnoreCase(mappedStatus)
                                    && (bid.getStatus() == null || bid.getStatus().equalsIgnoreCase("Pending"));

                            generated.add(new AcceptedWorkModel(
                                    bid.getBidId(),
                                    assignmentId,
                                    safe(assignment.getTitle()),
                                    safe(assignment.getSubject()),
                                    safe(assignment.getStudentName()),
                                    safe(assignment.getDeadline()),
                                    mappedStatus,
                                    safe(bid.getStatus()),
                                    safe(assignment.getStatus()),
                                    safe(assignment.getDescription()),
                                    safe(assignment.getFileUrl()),
                                    safe(assignment.getFileName()),
                                    safe(assignment.getStudentId()),
                                    !isRejected,
                                    canCancel
                            ));
                        }
                    })
                    .addOnCompleteListener(task -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            onMyWorkListReady(generated);
                        }
                    });
        }
    }

    private void onMyWorkListReady(List<AcceptedWorkModel> items) {
        fullWorkList.clear();
        fullWorkList.addAll(items);
        refreshStatusFilterOptions();
        applyFilters();
        showLoadingState(false, "");
    }

    private void refreshStatusFilterOptions() {
        Set<String> options = new LinkedHashSet<>();
        options.add("All");
        options.add("Pending Confirmation");
        options.add("Approved");
        options.add("Rejected");
        options.add("Bid Canceled");
        options.add("In Progress");
        options.add("Completed");
        options.add("Pending Payment");

        for (AcceptedWorkModel model : fullWorkList) {
            if (model != null && !TextUtils.isEmpty(model.getStatus())) {
                options.add(model.getStatus());
            }
        }

        statusFilterOptions.clear();
        statusFilterOptions.addAll(options);

        if (statusFilterAdapter != null) {
            statusFilterAdapter.notifyDataSetChanged();
        }

        if (!statusFilterOptions.contains(selectedStatusFilter)) {
            selectedStatusFilter = "All";
            if (actvStatusFilter != null) {
                actvStatusFilter.setText("All", false);
            }
        }
    }

    private void applyFilters() {
        String query = etSearchMyWork.getText() == null ? "" : etSearchMyWork.getText().toString().trim().toLowerCase(Locale.US);

        acceptedWorkList.clear();
        for (AcceptedWorkModel model : fullWorkList) {
            boolean matchesStatus = "All".equalsIgnoreCase(selectedStatusFilter)
                    || model.getStatus().equalsIgnoreCase(selectedStatusFilter);

            boolean matchesSearch = query.isEmpty()
                    || safe(model.getTitle()).toLowerCase(Locale.US).contains(query)
                    || safe(model.getSubject()).toLowerCase(Locale.US).contains(query)
                    || safe(model.getStudentName()).toLowerCase(Locale.US).contains(query);

            if (matchesStatus && matchesSearch) {
                acceptedWorkList.add(model);
            }
        }

        acceptedWorkAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (tvMyWorkEmpty != null) {
            tvMyWorkEmpty.setVisibility(acceptedWorkList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (recyclerAcceptedWork != null) {
            recyclerAcceptedWork.setVisibility(acceptedWorkList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void showLoadingState(boolean isLoading, String message) {
        if (progressMyWork != null) {
            progressMyWork.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (tvMyWorkLoading != null) {
            tvMyWorkLoading.setText(message == null || message.isEmpty() ? "Loading..." : message);
            tvMyWorkLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (isLoading && tvMyWorkEmpty != null) {
            tvMyWorkEmpty.setVisibility(View.GONE);
        }
    }

    private void showCancelBidConfirmation(AcceptedWorkModel model) {
        if (model == null || TextUtils.isEmpty(model.getBidId())) {
            Toast.makeText(getContext(), "Invalid bid", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Bid")
                .setMessage("Cancel this bid request?")
                .setPositiveButton("Yes", (dialog, which) -> cancelBid(model))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void cancelBid(AcceptedWorkModel model) {
        bidRepository.cancelBid(model.getBidId(), new BidRepository.OnBidActionCallback() {
            @Override
            public void onSuccess() {
                notifyStudentBidCancelled(model);
                Toast.makeText(getContext(), "Bid cancelled", Toast.LENGTH_SHORT).show();
                loadMyWork();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyStudentBidCancelled(AcceptedWorkModel model) {
        if (firebaseAuth.getCurrentUser() == null || model == null || TextUtils.isEmpty(model.getStudentId())) {
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(writerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String firstName = documentSnapshot.getString("firstName") != null ? documentSnapshot.getString("firstName").trim() : "";
                    String lastName = documentSnapshot.getString("lastName") != null ? documentSnapshot.getString("lastName").trim() : "";
                    String writerName = (firstName + " " + lastName).trim();
                    if (writerName.isEmpty()) {
                        String email = documentSnapshot.getString("email");
                        writerName = email != null && !email.trim().isEmpty() ? email.trim() : "A writer";
                    }

                    NotificationModel notificationModel = NotificationModel.builder()
                            .recipientUserId(model.getStudentId())
                            .recipientRole("student")
                            .title("Bid Cancelled")
                            .message(writerName + " cancelled the bid for your assignment " + safe(model.getTitle()) + ".")
                            .assignmentId(model.getAssignmentId())
                            .assignmentTitle(model.getTitle())
                            .assignmentStatus(model.getAssignmentStatus())
                            .writerWorkStatus("")
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
                });
    }

    private String mapWriterStatus(BidModel bid, AssignmentModel assignment) {
        String bidStatus = safe(bid.getStatus());
        String assignmentStatus = safe(assignment.getStatus());

        if (bidStatus.equalsIgnoreCase("Cancelled")) {
            return "Bid Canceled";
        }
        if (bidStatus.equalsIgnoreCase("Rejected")) {
            return "Rejected";
        }

        if (bidStatus.equalsIgnoreCase("Pending") || bidStatus.isEmpty()) {
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}