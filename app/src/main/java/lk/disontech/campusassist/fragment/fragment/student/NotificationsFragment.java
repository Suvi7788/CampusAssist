package lk.disontech.campusassist.fragment.fragment.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.AssignmentModel;
import lk.disontech.campusassist.model.NotificationModel;
import lk.disontech.campusassist.repository.NotificationRepository;

public class NotificationsFragment extends Fragment {

    private LinearLayout notificationsContainer;
    private TextView tvNotificationsEmpty;
    private ProgressBar progressNotifications;
    private TextView tvNotificationsLoading;
    private MaterialButton btnMarkAllAsRead;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private NotificationRepository notificationRepository;
    private ListenerRegistration notificationsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        notificationRepository = new NotificationRepository();

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        notificationsContainer = view.findViewById(R.id.notificationsContainer);
        tvNotificationsEmpty = view.findViewById(R.id.tvNotificationsEmpty);
        progressNotifications = view.findViewById(R.id.progressNotifications);
        tvNotificationsLoading = view.findViewById(R.id.tvNotificationsLoading);
        btnMarkAllAsRead = view.findViewById(R.id.btnMarkAllAsRead);

        btnMarkAllAsRead.setOnClickListener(v -> markAllAsRead());
        listenForNotifications(inflater);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }

    private void listenForNotifications(LayoutInflater inflater) {
        setLoadingState(true);
        if (firebaseAuth.getCurrentUser() == null) {
            setLoadingState(false);
            tvNotificationsEmpty.setVisibility(View.VISIBLE);
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        notificationsListener = notificationRepository.listenForUserNotifications(uid,
                new NotificationRepository.OnNotificationsLoadedCallback() {
                    @Override
                    public void onNotificationsLoaded(List<NotificationModel> notifications) {
                        setLoadingState(false);
                        notificationsContainer.removeAllViews();

                        if (notifications == null || notifications.isEmpty()) {
                            tvNotificationsEmpty.setVisibility(View.VISIBLE);
                            btnMarkAllAsRead.setVisibility(View.GONE);
                            return;
                        }

                        tvNotificationsEmpty.setVisibility(View.GONE);
                        btnMarkAllAsRead.setVisibility(View.VISIBLE);

                        for (NotificationModel notification : notifications) {
                            addNotification(inflater, notification);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoadingState(false);
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (progressNotifications != null) {
            progressNotifications.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (tvNotificationsLoading != null) {
            tvNotificationsLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnMarkAllAsRead != null) {
            btnMarkAllAsRead.setEnabled(!isLoading);
        }
    }

    private void addNotification(LayoutInflater inflater, NotificationModel notification) {

        View card = inflater.inflate(R.layout.item_notification_card, notificationsContainer, false);

        ImageView imgIcon = card.findViewById(R.id.imgIcon);
        TextView tvTitle = card.findViewById(R.id.tvTitle);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        TextView tvAssignmentInfo = card.findViewById(R.id.tvAssignmentInfo);
        TextView tvTime = card.findViewById(R.id.tvTime);
        View viewUnreadDot = card.findViewById(R.id.viewUnreadDot);
        MaterialButton btnViewAssignment = card.findViewById(R.id.btnViewAssignment);

        tvTitle.setText(notification.getTitle());
        tvMessage.setText(notification.getMessage());
        tvAssignmentInfo.setText("Assignment: " + safe(notification.getAssignmentTitle()));
        tvTime.setText(formatTimestamp(notification.getCreatedAt()));
        viewUnreadDot.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        card.setAlpha(notification.isRead() ? 0.85f : 1f);

        // Icon + tint based on type
        String type = safe(notification.getTitle()).toLowerCase(Locale.US);
        if (type.contains("accepted") || type.contains("completed") || type.contains("submitted")) {
                imgIcon.setImageResource(android.R.drawable.checkbox_on_background);
                imgIcon.setColorFilter(Color.parseColor("#10B981"));
        } else if (type.contains("bid") || type.contains("payment") || type.contains("progress")) {
                imgIcon.setImageResource(android.R.drawable.ic_dialog_email);
                imgIcon.setColorFilter(Color.parseColor("#2563EB"));
        } else if (type.contains("rejected") || type.contains("cancel")) {
                imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                imgIcon.setColorFilter(Color.parseColor("#F97316"));
        } else {
                imgIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                imgIcon.setColorFilter(Color.parseColor("#6B7280"));
        }

        card.setOnClickListener(v -> markNotificationAsRead(notification));
        btnViewAssignment.setOnClickListener(v -> openAssignmentFromNotification(notification));

        notificationsContainer.addView(card);
    }

    private void markNotificationAsRead(NotificationModel notification) {
        if (notification == null || notification.isRead() || notification.getNotificationId() == null) {
            return;
        }
        notificationRepository.markAsRead(notification.getNotificationId(), new NotificationRepository.OnNotificationActionCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAllAsRead() {
        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        notificationRepository.markAllAsRead(firebaseAuth.getCurrentUser().getUid(),
                new NotificationRepository.OnNotificationActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAssignmentFromNotification(NotificationModel notification) {
        if (notification == null || notification.getAssignmentId() == null || notification.getAssignmentId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Assignment not found", Toast.LENGTH_SHORT).show();
            return;
        }

        markNotificationAsRead(notification);

        firebaseFirestore.collection("Assignments").document(notification.getAssignmentId()).get()
                .addOnSuccessListener(documentSnapshot -> navigateToAssignmentDetails(notification, documentSnapshot))
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToAssignmentDetails(NotificationModel notification, DocumentSnapshot documentSnapshot) {
        AssignmentModel assignment = documentSnapshot.toObject(AssignmentModel.class);
        if (assignment == null) {
            Toast.makeText(getContext(), "Assignment data unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = getActivity() != null && getActivity().getIntent() != null
                ? safe(getActivity().getIntent().getStringExtra("role"))
                : "";
        boolean isWriter = role.equalsIgnoreCase("writer");

        Bundle bundle = new Bundle();
        bundle.putString("title", safe(assignment.getTitle()));
        bundle.putString("subject", safe(assignment.getSubject()));
        bundle.putString("status", safe(assignment.getStatus()));
        bundle.putString("description", safe(assignment.getDescription()));
        bundle.putString("deadline", safe(assignment.getDeadline()));
        bundle.putString("fileUrl", safe(assignment.getFileUrl()));
        bundle.putString("fileName", safe(assignment.getFileName()));
        bundle.putString("assignmentId", safe(assignment.getAssignmentId()));
        bundle.putString("studentId", safe(assignment.getStudentId()));
        bundle.putString("studentName", safe(assignment.getStudentName()));
        bundle.putBoolean("isWriterView", isWriter);

        if (isWriter) {
            bundle.putBoolean("fromMyWork", true);
            bundle.putString("writerWorkStatus", safe(notification.getWriterWorkStatus()));
            if (!safe(notification.getWriterWorkStatus()).equalsIgnoreCase("Pending Confirmation")
                    && !safe(notification.getWriterWorkStatus()).equalsIgnoreCase("Rejected")
                    && !safe(notification.getWriterWorkStatus()).equalsIgnoreCase("Bid Canceled")) {
                bundle.putString("writerBidStatus", "Accepted");
            } else if (safe(notification.getWriterWorkStatus()).equalsIgnoreCase("Rejected")) {
                bundle.putString("writerBidStatus", "Rejected");
            } else if (safe(notification.getWriterWorkStatus()).equalsIgnoreCase("Bid Canceled")) {
                bundle.putString("writerBidStatus", "Cancelled");
            } else {
                bundle.putString("writerBidStatus", "Pending");
            }
        }

        AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
        fragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private String formatTimestamp(long createdAt) {
        if (createdAt <= 0L) {
            return "Just now";
        }
        long diff = System.currentTimeMillis() - createdAt;
        long minutes = diff / (60 * 1000);
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }
        return new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US).format(new Date(createdAt));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}