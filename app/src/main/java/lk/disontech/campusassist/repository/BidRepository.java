package lk.disontech.campusassist.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

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
                    if (!querySnapshot.isEmpty()) {
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

    public void getBidsByAssignment(@NonNull String assignmentId, @NonNull OnBidsLoadedCallback callback) {
        firestore.collection("Bids")
                .whereEqualTo("assignmentId", assignmentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<BidModel> bidList = new ArrayList<>(querySnapshot.toObjects(BidModel.class));
                    Collections.sort(bidList, Comparator.comparingLong(BidModel::getCreatedAt).reversed());
                    callback.onBidsLoaded(bidList);
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
}

