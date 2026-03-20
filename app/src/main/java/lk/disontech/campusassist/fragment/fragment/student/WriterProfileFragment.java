package lk.disontech.campusassist.fragment.fragment.student;

import android.os.Bundle;
import android.text.TextUtils;
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

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.User;

public class WriterProfileFragment extends Fragment {

    private TextView tvInitials, tvName, tvField, tvRating, tvReviews;
    private TextView tvCompleted, tvStatRating, tvTopPercent, tvAbout, tvEducation;
    private TextView tvPhone, tvEmail;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_writer_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        tvInitials = view.findViewById(R.id.tvInitials);
        tvName = view.findViewById(R.id.tvName);
        tvField = view.findViewById(R.id.tvField);
        tvRating = view.findViewById(R.id.tvRating);
        tvReviews = view.findViewById(R.id.tvReviews);

        tvCompleted = view.findViewById(R.id.tvCompleted);
        tvStatRating = view.findViewById(R.id.tvStatRating);
        tvTopPercent = view.findViewById(R.id.tvTopPercent);

        tvAbout = view.findViewById(R.id.tvAbout);
        tvEducation = view.findViewById(R.id.tvEducation);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmail = view.findViewById(R.id.tvEmail);

        applyDefaultUiState();
        loadWriterData();

        return view;
    }

    private void applyDefaultUiState() {
        tvInitials.setText("-");
        tvName.setText("Loading profile...");
        tvField.setText("Writer");
        tvRating.setText("N/A");
        tvReviews.setText("(0 reviews)");
        tvCompleted.setText("0");
        tvStatRating.setText("N/A");
        tvTopPercent.setText("N/A");
        tvAbout.setText("No bio available yet.");
        tvEducation.setText("Not provided");
        tvPhone.setText("Not available");
        tvEmail.setText("Not available");
    }

    private void loadWriterData() {
        String writerId = null;
        Bundle args = getArguments();
        if (args != null) {
            writerId = args.getString("writerId", null);
        }

        if (TextUtils.isEmpty(writerId) && firebaseAuth.getCurrentUser() != null) {
            writerId = firebaseAuth.getCurrentUser().getUid();
        }

        if (TextUtils.isEmpty(writerId)) {
            Toast.makeText(getContext(), "Unable to load writer profile", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalWriterId = writerId;
        firebaseFirestore.collection("Users").document(finalWriterId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(getContext(), "Writer profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User writer = documentSnapshot.toObject(User.class);
                    if (writer == null) {
                        Toast.makeText(getContext(), "Invalid writer data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String firstName = safe(writer.getFirstName());
                    String lastName = safe(writer.getLastName());
                    String fullName = (firstName + " " + lastName).trim();
                    if (fullName.isEmpty()) {
                        fullName = "Writer";
                    }

                    String interests = safe(writer.getInterests());
                    String bio = safe(writer.getBio());
                    String userType = safe(writer.getUserType());
                    String email = safe(writer.getEmail());
                    String mobile = safe(writer.getMobile());

                    tvName.setText(fullName);
                    tvInitials.setText(getInitials(firstName, lastName, fullName));
                    tvField.setText(interests.isEmpty() ? "Academic Writer" : interests);
                    tvAbout.setText(bio.isEmpty() ? "No bio available yet." : bio);
                    tvEducation.setText(interests.isEmpty() ? "Not provided" : interests);
                    tvPhone.setText(mobile.isEmpty() ? "Not available" : mobile);
                    tvEmail.setText(email.isEmpty() ? "Not available" : email);

                    // Keep static placeholders for metrics until rating/review backend is available.
                    tvRating.setText("N/A");
                    tvReviews.setText("(0 reviews)");
                    tvCompleted.setText("0");
                    tvStatRating.setText("N/A");
                    tvTopPercent.setText(userType.isEmpty() ? "Writer" : userType);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private String getInitials(String firstName, String lastName, String fullName) {
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            String first = firstName.isEmpty() ? "" : firstName.substring(0, 1).toUpperCase();
            String last = lastName.isEmpty() ? "" : lastName.substring(0, 1).toUpperCase();
            String initials = (first + last).trim();
            return initials.isEmpty() ? "W" : initials;
        }

        if (!fullName.isEmpty()) {
            return fullName.substring(0, 1).toUpperCase();
        }

        return "W";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}