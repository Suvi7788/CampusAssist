package lk.disontech.campusassist.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.disontech.campusassist.model.AssignmentModel;

public class AssignmentRepository {

    private FirebaseFirestore firestore;

    public AssignmentRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Fetch all assignments posted by students
     */
    public void getAllAssignments(OnAssignmentsLoadedCallback callback) {
        firestore.collection("Assignments")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<AssignmentModel> assignmentList = new ArrayList<>();
                        for (int i = 0; i < value.size(); i++) {
                            AssignmentModel model = value.getDocuments().get(i)
                                    .toObject(AssignmentModel.class);
                            if (model != null) {
                                assignmentList.add(model);
                            }
                        }
                        callback.onAssignmentsLoaded(assignmentList);
                    }
                });
    }

    /**
     * Fetch assignments by subject/category
     */
    public void getAssignmentsBySubject(String subject, OnAssignmentsLoadedCallback callback) {
        firestore.collection("Assignments")
                .whereEqualTo("subject", subject)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<AssignmentModel> assignmentList = new ArrayList<>();
                        for (int i = 0; i < value.size(); i++) {
                            AssignmentModel model = value.getDocuments().get(i)
                                    .toObject(AssignmentModel.class);
                            if (model != null) {
                                assignmentList.add(model);
                            }
                        }
                        callback.onAssignmentsLoaded(assignmentList);
                    }
                });
    }

    /**
     * Callback interface for handling async operations
     */
    public interface OnAssignmentsLoadedCallback {
        void onAssignmentsLoaded(List<AssignmentModel> assignments);
        void onError(String errorMessage);
    }
}

