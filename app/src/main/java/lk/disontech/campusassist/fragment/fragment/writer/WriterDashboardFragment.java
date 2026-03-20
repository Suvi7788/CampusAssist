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
import com.google.firebase.firestore.ListenerRegistration;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.repository.NotificationRepository;

public class WriterDashboardFragment extends Fragment {

    public interface WriterDashboardNavigator {
        void openBrowseAssignments();
        void openAcceptedWork();
        void openProfile();
        void openNotifications();
        void openDrawerOrMenu();
    }

    private WriterDashboardNavigator navigator;
    private FirebaseAuth firebaseAuth;
    private NotificationRepository notificationRepository;
    private ListenerRegistration unreadCountListener;
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
        View cardProfile = view.findViewById(R.id.card_profile);
        View cardNotifications = view.findViewById(R.id.card_notifications);
        tvNotificationsBadge = view.findViewById(R.id.tvNotificationsBadge);

        firebaseAuth = FirebaseAuth.getInstance();
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

        cardProfile.setOnClickListener(v -> {
            if (navigator != null) navigator.openProfile();
            else toast("Profile");
        });

        cardNotifications.setOnClickListener(v -> {
            if (navigator != null) navigator.openNotifications();
            else toast("Notifications");
        });

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

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}