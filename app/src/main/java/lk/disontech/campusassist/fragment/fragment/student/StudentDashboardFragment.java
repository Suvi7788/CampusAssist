package lk.disontech.campusassist.fragment.fragment.student;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AssignmentModel;
import lk.disontech.campusassist.repository.NotificationRepository;

public class StudentDashboardFragment extends Fragment {

    // Optional: host Activity can implement this to handle navigation
    public interface StudentDashboardNavigator {
        void openPostNewAssignment();
        void openMyAssignments();
        void openMyAssignmentsWithFilter(String status);
        void openNotifications();
        void openHelpGuidelines();
        void openDrawerOrMenu(); // for hamburger icon
    }

    private StudentDashboardNavigator navigator;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private NotificationRepository notificationRepository;
    private ListenerRegistration unreadCountListener;

    private TextView tvOpenCount;
    private TextView tvAssignedCount;
    private TextView tvInProgressCount;
    private TextView tvCompletedCount;
    private TextView tvPendingPaymentCount;
    private TextView tvWelcome;
    private TextView tvNotificationsBadge;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof StudentDashboardNavigator) {
            navigator = (StudentDashboardNavigator) context;
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

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_student_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);

        View cardPostNew = view.findViewById(R.id.cardPostNewAssignment);
        View cardMyAssignments = view.findViewById(R.id.cardMyAssignments);
        View cardNotifications = view.findViewById(R.id.cardNotifications);
        View cardHelp = view.findViewById(R.id.cardHelpGuidelines);

        View cardStatOpen = view.findViewById(R.id.cardStatOpen);
        View cardStatAssigned = view.findViewById(R.id.cardStatAssigned);
        View cardStatInProgress = view.findViewById(R.id.cardStatInProgress);
        View cardStatCompleted = view.findViewById(R.id.cardStatCompleted);
        View cardStatPendingPayment = view.findViewById(R.id.cardStatPendingPayment);

        tvOpenCount = view.findViewById(R.id.tvOpenCount);
        tvAssignedCount = view.findViewById(R.id.tvAssignedCount);
        tvInProgressCount = view.findViewById(R.id.tvInProgressCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvPendingPaymentCount = view.findViewById(R.id.tvPendingPaymentCount);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvNotificationsBadge = view.findViewById(R.id.tvNotificationsBadge);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        notificationRepository = new NotificationRepository();

        // Hamburger click (host can open DrawerLayout)
        toolbar.setNavigationOnClickListener(v -> {
            if (navigator != null) {
                navigator.openDrawerOrMenu();
            } else {
                Toast.makeText(requireContext(), "Menu clicked (connect to Drawer later)", Toast.LENGTH_SHORT).show();
            }
        });

        cardPostNew.setOnClickListener(v -> {
            if (navigator != null) navigator.openPostNewAssignment();
            else showTodo("Post New Assignment");
        });

        cardMyAssignments.setOnClickListener(v -> {
            if (navigator != null) navigator.openMyAssignments();
            else showTodo("My Assignments");
        });

        cardNotifications.setOnClickListener(v -> {
            if (navigator != null) navigator.openNotifications();
            else showTodo("Notifications");
        });

        cardHelp.setOnClickListener(v -> {
            if (navigator != null) navigator.openHelpGuidelines();
            else showTodo("Help / Guidelines");
        });

        cardStatOpen.setOnClickListener(v -> openMyAssignmentsForStatus("Open"));
        cardStatAssigned.setOnClickListener(v -> openMyAssignmentsForStatus("Assigned"));
        cardStatInProgress.setOnClickListener(v -> openMyAssignmentsForStatus("In Progress"));
        cardStatCompleted.setOnClickListener(v -> openMyAssignmentsForStatus("Completed"));
        cardStatPendingPayment.setOnClickListener(v -> openMyAssignmentsForStatus("Pending Payment"));

        loadWelcomeName();
        loadQuickStats();
        listenForUnreadNotificationCount();
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
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");

                    String displayName = "";
                    if (firstName != null && !firstName.trim().isEmpty()) {
                        displayName = firstName.trim();
                    }
                    if (lastName != null && !lastName.trim().isEmpty()) {
                        displayName = (displayName + " " + lastName.trim()).trim();
                    }

                    if (displayName.isEmpty() && firebaseAuth.getCurrentUser().getEmail() != null) {
                        displayName = firebaseAuth.getCurrentUser().getEmail();
                    }
                    if (displayName.isEmpty()) {
                        displayName = "Student";
                    }

                    tvWelcome.setText("Welcome back, " + displayName + "!");
                })
                .addOnFailureListener(e -> {
                    if (firebaseAuth.getCurrentUser().getEmail() != null) {
                        tvWelcome.setText("Welcome back, " + firebaseAuth.getCurrentUser().getEmail() + "!");
                    }
                });
    }

    private void openMyAssignmentsForStatus(String status) {
        if (navigator != null) {
            navigator.openMyAssignmentsWithFilter(status);
        } else {
            showTodo("My Assignments (" + status + ")");
        }
    }

    private void loadQuickStats() {
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String studentId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Assignments")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AssignmentModel> assignments = queryDocumentSnapshots.toObjects(AssignmentModel.class);

                    int openCount = 0;
                    int assignedCount = 0;
                    int inProgressCount = 0;
                    int completedCount = 0;
                    int pendingPaymentCount = 0;

                    for (AssignmentModel assignment : assignments) {
                        String status = assignment != null && assignment.getStatus() != null
                                ? assignment.getStatus().trim()
                                : "";

                        if (status.equalsIgnoreCase("Open")) {
                            openCount++;
                        } else if (status.equalsIgnoreCase("Assigned")) {
                            assignedCount++;
                        } else if (status.equalsIgnoreCase("In Progress")) {
                            inProgressCount++;
                        } else if (status.equalsIgnoreCase("Completed")) {
                            completedCount++;
                        } else if (status.equalsIgnoreCase("Pending Payment")) {
                            pendingPaymentCount++;
                        }
                    }

                    tvOpenCount.setText(String.valueOf(openCount));
                    tvAssignedCount.setText(String.valueOf(assignedCount));
                    tvInProgressCount.setText(String.valueOf(inProgressCount));
                    tvCompletedCount.setText(String.valueOf(completedCount));
                    tvPendingPaymentCount.setText(String.valueOf(pendingPaymentCount));
                })
                .addOnFailureListener(e -> Toast.makeText(
                        requireContext(),
                        "Failed to load quick stats",
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void showTodo(String screenName) {
        Toast.makeText(requireContext(), "Open: " + screenName + " (create screen later)", Toast.LENGTH_SHORT).show();
    }
}