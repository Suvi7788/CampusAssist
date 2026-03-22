package lk.disontech.campusassist.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lk.disontech.campusassist.model.BidModel;

public class BidRepository {

    private final FirebaseFirestore firestore;

    public BidRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void createBid(@NonNull BidModel bidModel, @NonNull OnBidActionCallback callback) {
        firestore.collection("Bids")
                .whereEqualTo("assignmentId", bidModel.getAssignmentId())
                .whereEqualTo("writerId", bidModel.getWriterId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasActiveBid = false;
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        String status = documentSnapshot.getString("status");
                        if (isActiveBidStatus(status)) {
                            hasActiveBid = true;
                            break;
                        }
                    }

                    if (hasActiveBid) {
                        callback.onError("You already placed a bid for this assignment");
                        return;
                    }

                    String bidId = firestore.collection("Bids").document().getId();
                    bidModel.setBidId(bidId);
                    if (bidModel.getCreatedAt() == 0L) {
                        bidModel.setCreatedAt(System.currentTimeMillis());
                    }

                    firestore.collection("Bids").document(bidId)
                            .set(bidModel)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getLatestBidByAssignmentAndWriter(@NonNull String assignmentId,
                                                   @NonNull String writerId,
                                                   @NonNull OnBidLoadedCallback callback) {
        firestore.collection("Bids")
                .whereEqualTo("assignmentId", assignmentId)
                .whereEqualTo("writerId", writerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    BidModel latestBid = null;
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        BidModel bidModel = documentSnapshot.toObject(BidModel.class);
                        if (bidModel == null) {
                            continue;
                        }
                        if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
                            bidModel.setBidId(documentSnapshot.getId());
                        }
                        if (latestBid == null || bidModel.getCreatedAt() > latestBid.getCreatedAt()) {
                            latestBid = bidModel;
                        }
                    }
                    callback.onBidLoaded(latestBid);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getBidsByAssignment(@NonNull String assignmentId, @NonNull OnBidsLoadedCallback callback) {
        firestore.collection("Bids")
                .whereEqualTo("assignmentId", assignmentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<BidModel> bidList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        BidModel bidModel = documentSnapshot.toObject(BidModel.class);
                        if (bidModel != null) {
                            if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
                                bidModel.setBidId(documentSnapshot.getId());
                            }
                            bidList.add(bidModel);
                        }
                    }
                    Collections.sort(bidList, Comparator.comparingLong(BidModel::getCreatedAt).reversed());
                    callback.onBidsLoaded(bidList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getBidsByWriter(@NonNull String writerId, @NonNull OnBidsLoadedCallback callback) {
        firestore.collection("Bids")
                .whereEqualTo("writerId", writerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<BidModel> bidList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        BidModel bidModel = documentSnapshot.toObject(BidModel.class);
                        if (bidModel != null) {
                            if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
                                bidModel.setBidId(documentSnapshot.getId());
                            }
                            bidList.add(bidModel);
                        }
                    }
                    Collections.sort(bidList, Comparator.comparingLong(BidModel::getCreatedAt).reversed());
                    callback.onBidsLoaded(bidList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateBidStatus(@NonNull String bidId, @NonNull String status,
                                @NonNull OnBidActionCallback callback) {
        firestore.collection("Bids").document(bidId)
                .update("status", status)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void cancelBid(@NonNull String bidId, @NonNull OnBidActionCallback callback) {
        updateBidStatus(bidId, "Cancelled", callback);
    }

    public void acceptBid(@NonNull String assignmentId,
                          @NonNull String acceptedBidId,
                          @NonNull String writerId,
                          @NonNull String writerName,
                          @NonNull OnBidActionCallback callback) {
        firestore.collection("Bids")
                .whereEqualTo("assignmentId", assignmentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (writerId.trim().isEmpty()) {
                        callback.onError("Invalid writer selected for assignment");
                        return;
                    }

                    WriteBatch batch = firestore.batch();

                    try {
                        batch.update(
                                firestore.collection("Assignments").document(assignmentId),
                                "status", "Assigned",
                                "assignedWriterId", writerId,
                                "assignedWriterName", writerName == null ? "Writer" : writerName,
                                "updatedAt", System.currentTimeMillis()
                        );
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                        return;
                    }

                    querySnapshot.getDocuments().forEach(documentSnapshot -> {
                        String bidId = documentSnapshot.getId();
                        String currentStatus = documentSnapshot.getString("status");

                        if (acceptedBidId.equals(bidId)) {
                            batch.update(documentSnapshot.getReference(), "status", "Accepted");
                        } else if (currentStatus == null || currentStatus.equalsIgnoreCase("Pending")) {
                            batch.update(documentSnapshot.getReference(), "status", "Rejected");
                        }
                    });

                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface OnBidActionCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    public interface OnBidsLoadedCallback {
        void onBidsLoaded(List<BidModel> bids);

        void onError(String errorMessage);
    }

    public interface OnBidLoadedCallback {
        void onBidLoaded(BidModel bidModel);

        void onError(String errorMessage);
    }

    private boolean isActiveBidStatus(String status) {
        if (status == null) {
            return true;
        }
        return !(status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Rejected"));
    }
}

