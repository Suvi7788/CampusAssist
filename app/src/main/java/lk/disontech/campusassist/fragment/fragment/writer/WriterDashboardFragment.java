package lk.disontech.campusassist.fragment.fragment.writer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.repository.NotificationRepository;

public class WriterDashboardFragment extends Fragment {

    public interface WriterDashboardNavigator {
        void openBrowseAssignments();
        void openAcceptedWork();
        void openAcceptedWorkWithFilter(String status);
        void openSubmissions();
        void openEarnings();
        void openProfile();
        void openNotifications();
        void openDrawerOrMenu();
    }

    private WriterDashboardNavigator navigator;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private NotificationRepository notificationRepository;
    private ListenerRegistration unreadCountListener;
    private TextView tvWelcome;
    private TextView tvInProgressCount;
    private TextView tvCompletedCount;
    private TextView tvPendingConfirmationCount;
    private TextView tvPendingPaymentCount;
    private TextView tvNotificationsBadge;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof WriterDashboardNavigator) {
            navigator = (WriterDashboardNavigator) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unreadCountListener != null) {
            unreadCountListener.remove();
            unreadCountListener = null;
        }
    }

    public WriterDashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_writer_dashboard, container, false);

        View cardBrowse = view.findViewById(R.id.card_browse);
        View cardAccepted = view.findViewById(R.id.card_accepted);
        View cardSubmissions = view.findViewById(R.id.card_submissions);
        View cardEarnings = view.findViewById(R.id.card_earnings);
        View cardProfile = view.findViewById(R.id.card_profile);
        View cardNotifications = view.findViewById(R.id.card_notifications);
        View cardStatInProgress = view.findViewById(R.id.cardStatInProgress);
        View cardStatCompleted = view.findViewById(R.id.cardStatCompleted);
        View cardStatPendingConfirmation = view.findViewById(R.id.cardStatPendingConfirmation);
        View cardStatPendingPayment = view.findViewById(R.id.cardStatPendingPayment);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvInProgressCount = view.findViewById(R.id.tvInProgressCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvPendingConfirmationCount = view.findViewById(R.id.tvPendingConfirmationCount);
        tvPendingPaymentCount = view.findViewById(R.id.tvPendingPaymentCount);
        tvNotificationsBadge = view.findViewById(R.id.tvNotificationsBadge);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        notificationRepository = new NotificationRepository();

        com.google.android.material.appbar.MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(v -> {
            if (navigator != null) navigator.openDrawerOrMenu();
        });

        cardBrowse.setOnClickListener(v -> {
            if (navigator != null) navigator.openBrowseAssignments();
            else toast("Browse Assignments");
        });

        cardAccepted.setOnClickListener(v -> {
            if (navigator != null) navigator.openAcceptedWork();
            else toast("My Work");
        });

        cardSubmissions.setOnClickListener(v -> {
            if (navigator != null) navigator.openSubmissions();
            else toast("Submissions");
        });

        cardEarnings.setOnClickListener(v -> {
            if (navigator != null) navigator.openEarnings();
            else toast("Earnings");
        });

        cardProfile.setOnClickListener(v -> {
            if (navigator != null) navigator.openProfile();
            else toast("Profile");
        });

        cardNotifications.setOnClickListener(v -> {
            if (navigator != null) navigator.openNotifications();
            else toast("Notifications");
        });

        cardStatInProgress.setOnClickListener(v -> openMyWorkForStatus("In Progress"));
        cardStatCompleted.setOnClickListener(v -> openMyWorkForStatus("Completed"));
        cardStatPendingConfirmation.setOnClickListener(v -> openMyWorkForStatus("Pending Confirmation"));
        cardStatPendingPayment.setOnClickListener(v -> openMyWorkForStatus("Pending Payment"));

        loadWelcomeName();
        loadQuickStats();
        listenForUnreadNotificationCount();

        return view;
    }

    private void listenForUnreadNotificationCount() {
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        unreadCountListener = notificationRepository.listenForUnreadCount(
                firebaseAuth.getCurrentUser().getUid(),
                new NotificationRepository.OnUnreadCountChangedCallback() {
                    @Override
                    public void onCountChanged(int unreadCount) {
                        updateNotificationsBadge(unreadCount);
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                }
        );
    }

    private void updateNotificationsBadge(int unreadCount) {
        if (tvNotificationsBadge == null) {
            return;
        }

        if (unreadCount <= 0) {
            tvNotificationsBadge.clearAnimation();
            tvNotificationsBadge.setVisibility(View.GONE);
            return;
        }

        tvNotificationsBadge.setText(String.valueOf(unreadCount));
        tvNotificationsBadge.setVisibility(View.VISIBLE);

        Animation blink = new AlphaAnimation(0.3f, 1f);
        blink.setDuration(650);
        blink.setRepeatMode(Animation.REVERSE);
        blink.setRepeatCount(Animation.INFINITE);
        tvNotificationsBadge.startAnimation(blink);
    }

    private void loadWelcomeName() {
        if (firebaseAuth.getCurrentUser() == null || tvWelcome == null) {
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || tvWelcome == null || firebaseAuth.getCurrentUser() == null) {
                        return;
                    }
                    String firstName = safe(documentSnapshot.getString("firstName")).trim();
                    String lastName = safe(documentSnapshot.getString("lastName")).trim();

                    String displayName = (firstName + " " + lastName).trim();
                    if (displayName.isEmpty() && firebaseAuth.getCurrentUser().getEmail() != null) {
                        displayName = firebaseAuth.getCurrentUser().getEmail();
                    }
                    if (displayName.isEmpty()) {
                        displayName = "Writer";
                    }

                    Context context = getContext();
                    if (context == null) {
                        return;
                    }
                    tvWelcome.setText(context.getString(R.string.writer_welcome_format, displayName));
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || tvWelcome == null || firebaseAuth.getCurrentUser() == null) {
                        return;
                    }
                    String fallback = firebaseAuth.getCurrentUser().getEmail();
                    Context context = getContext();
                    if (context == null) {
                        return;
                    }
                    tvWelcome.setText(context.getString(
                            R.string.writer_welcome_format,
                            fallback == null || fallback.trim().isEmpty() ? "Writer" : fallback
                    ));
                });
    }

    private void loadQuickStats() {
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Assignments")
                .whereEqualTo("assignedWriterId", writerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || tvInProgressCount == null || tvCompletedCount == null || tvPendingPaymentCount == null) {
                        return;
                    }
                    int inProgressCount = 0;
                    int completedCount = 0;
                    int pendingPaymentCount = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String status = safe(doc.getString("status")).trim();
                        if (status.equalsIgnoreCase("In Progress")) {
                            inProgressCount++;
                        } else if (status.equalsIgnoreCase("Completed")) {
                            completedCount++;
                        } else if (status.equalsIgnoreCase("Pending Payment")) {
                            pendingPaymentCount++;
                        }
                    }

                    tvInProgressCount.setText(String.valueOf(inProgressCount));
                    tvCompletedCount.setText(String.valueOf(completedCount));
                    tvPendingPaymentCount.setText(String.valueOf(pendingPaymentCount));
                });

        firebaseFirestore.collection("Bids")
                .whereEqualTo("writerId", writerId)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || tvPendingConfirmationCount == null) {
                        return;
                    }
                    tvPendingConfirmationCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
    }

    private void openMyWorkForStatus(String status) {
        if (navigator != null) {
            navigator.openAcceptedWorkWithFilter(status);
        } else {
            toast("My Work (" + status + ")");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}