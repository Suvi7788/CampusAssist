package lk.disontech.campusassist.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lk.disontech.campusassist.model.NotificationModel;

public class NotificationRepository {

    private final FirebaseFirestore firestore;

    public NotificationRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void createNotification(@NonNull NotificationModel notificationModel,
                                   @NonNull OnNotificationActionCallback callback) {
        String notificationId = firestore.collection("Notifications").document().getId();
        notificationModel.setNotificationId(notificationId);
        if (notificationModel.getCreatedAt() == 0L) {
            notificationModel.setCreatedAt(System.currentTimeMillis());
        }
        notificationModel.setRead(notificationModel.isRead());

        firestore.collection("Notifications").document(notificationId)
                .set(notificationModel)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public ListenerRegistration listenForUserNotifications(@NonNull String userId,
                                                           @NonNull OnNotificationsLoadedCallback callback) {
        return firestore.collection("Notifications")
                .whereEqualTo("recipientUserId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    List<NotificationModel> items = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            NotificationModel model = documentSnapshot.toObject(NotificationModel.class);
                            if (model != null) {
                                if (model.getNotificationId() == null || model.getNotificationId().trim().isEmpty()) {
                                    model.setNotificationId(documentSnapshot.getId());
                                }
                                items.add(model);
                            }
                        }
                    }
                    Collections.sort(items, Comparator.comparingLong(NotificationModel::getCreatedAt).reversed());
                    callback.onNotificationsLoaded(items);
                });
    }

    public ListenerRegistration listenForUnreadCount(@NonNull String userId,
                                                     @NonNull OnUnreadCountChangedCallback callback) {
        return firestore.collection("Notifications")
                .whereEqualTo("recipientUserId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    int unreadCount = 0;
                    if (value != null) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            Boolean isRead = documentSnapshot.getBoolean("read");
                            if (isRead == null || !isRead) {
                                unreadCount++;
                            }
                        }
                    }
                    callback.onCountChanged(unreadCount);
                });
    }

    public void markAsRead(@NonNull String notificationId,
                           @NonNull OnNotificationActionCallback callback) {
        firestore.collection("Notifications").document(notificationId)
                .update("read", true)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void markAllAsRead(@NonNull String userId,
                              @NonNull OnNotificationActionCallback callback) {
        firestore.collection("Notifications")
                .whereEqualTo("recipientUserId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = firestore.batch();
                    querySnapshot.getDocuments().forEach(document -> {
                        Boolean isRead = document.getBoolean("read");
                        if (isRead == null || !isRead) {
                            batch.update(document.getReference(), "read", true);
                        }
                    });
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface OnNotificationActionCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    public interface OnNotificationsLoadedCallback {
        void onNotificationsLoaded(List<NotificationModel> notifications);

        void onError(String errorMessage);
    }

    public interface OnUnreadCountChangedCallback {
        void onCountChanged(int unreadCount);

        void onError(String errorMessage);
    }
}


