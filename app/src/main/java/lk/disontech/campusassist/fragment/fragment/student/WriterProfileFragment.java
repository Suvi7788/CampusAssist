package lk.disontech.campusassist.fragment.fragment.student;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.model.User;

public class WriterProfileFragment extends Fragment {

    private TextView tvInitials, tvName, tvField, tvRating, tvReviews;
    private TextView tvCompleted, tvStatRating, tvTopPercent, tvAbout, tvEducation;
    private TextView tvPhone, tvEmail;
    private com.google.android.material.imageview.ShapeableImageView imgAvatar;
    private View editProfileSection;
    private MaterialButton btnEditProfile, btnChangeProfileImage, btnSaveProfile, btnCancelEdit, btnCallWriter;
    private ProgressBar progressSaveProfile;
    private TextInputEditText etFirstName, etLastName, etMobile, etEmail, etBio, etInterests;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;

    private String activeWriterId = "";
    private String currentProfilePicUrl = "";
    private String avatarLoadToken = "";
    private String loadedMobile = "";

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadProfileImageOnly(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_writer_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        tvInitials = view.findViewById(R.id.tvInitials);
        imgAvatar = view.findViewById(R.id.imgAvatar);
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

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangeProfileImage = view.findViewById(R.id.btnChangeProfileImage);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
        btnCallWriter = view.findViewById(R.id.btnCallWriter);
        progressSaveProfile = view.findViewById(R.id.progressSaveProfile);
        editProfileSection = view.findViewById(R.id.editProfileSection);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobile = view.findViewById(R.id.etMobile);
        etEmail = view.findViewById(R.id.etEmail);
        etBio = view.findViewById(R.id.etBio);
        etInterests = view.findViewById(R.id.etInterests);

        applyDefaultUiState();
        setupEditorActions();
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
        editProfileSection.setVisibility(View.GONE);
    }

    private void setupEditorActions() {
        btnEditProfile.setOnClickListener(v -> {
            editProfileSection.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
        });

        btnCancelEdit.setOnClickListener(v -> {
            editProfileSection.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.VISIBLE);
            loadWriterData();
        });

        btnChangeProfileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnCallWriter.setOnClickListener(v -> callWriter());
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

        activeWriterId = writerId;

        boolean canEdit = firebaseAuth.getCurrentUser() != null
                && writerId.equals(firebaseAuth.getCurrentUser().getUid());
        btnEditProfile.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        btnChangeProfileImage.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        btnCallWriter.setVisibility(canEdit ? View.GONE : View.VISIBLE);
        editProfileSection.setVisibility(View.GONE);

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
                    currentProfilePicUrl = safe(writer.getProfilePicURL());
                    loadedMobile = mobile;

                    tvName.setText(fullName);
                    tvInitials.setText(getInitials(firstName, lastName, fullName));
                    tvField.setText(interests.isEmpty() ? "Academic Writer" : interests);
                    tvAbout.setText(bio.isEmpty() ? "No bio available yet." : bio);
                    tvEducation.setText(interests.isEmpty() ? "Not provided" : interests);
                    tvPhone.setText(mobile.isEmpty() ? "Not available" : mobile);
                    tvEmail.setText(email.isEmpty() ? "Not available" : email);

                    etFirstName.setText(firstName);
                    etLastName.setText(lastName);
                    etMobile.setText(mobile);
                    etEmail.setText(email);
                    etBio.setText(bio);
                    etInterests.setText(interests);

                    loadAvatar(currentProfilePicUrl);

                    // Keep static placeholders for metrics until rating/review backend is available.
                    tvRating.setText("N/A");
                    tvReviews.setText("(0 reviews)");
                    tvCompleted.setText("0");
                    tvStatRating.setText("N/A");
                    tvTopPercent.setText(userType.isEmpty() ? "Writer" : userType);

                    loadWriterStatistics(finalWriterId, documentSnapshot);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadWriterStatistics(String writerId, com.google.firebase.firestore.DocumentSnapshot writerSnapshot) {
        Double ratingValue = writerSnapshot.getDouble("rating");
        Long reviewsCount = writerSnapshot.getLong("reviewsCount");

        tvRating.setText(formatRating(ratingValue));
        tvStatRating.setText(formatRating(ratingValue));
        tvReviews.setText("(" + (reviewsCount != null ? reviewsCount : 0L) + " reviews)");

        firebaseFirestore.collection("Assignments")
                .whereEqualTo("assignedWriterId", writerId)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvCompleted.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e -> tvCompleted.setText("0"));
    }

    private void loadAvatar(String profilePicUrl) {
        avatarLoadToken = profilePicUrl;

        if (TextUtils.isEmpty(profilePicUrl)) {
            imgAvatar.setImageDrawable(null);
            tvInitials.setVisibility(View.VISIBLE);
            return;
        }

        try {
            StorageReference ref = firebaseStorage.getReferenceFromUrl(profilePicUrl);
            ref.getBytes(1024 * 1024)
                    .addOnSuccessListener(bytes -> {
                        if (!profilePicUrl.equals(avatarLoadToken)) {
                            return;
                        }
                        imgAvatar.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        tvInitials.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        if (!profilePicUrl.equals(avatarLoadToken)) {
                            return;
                        }
                        tvInitials.setVisibility(View.VISIBLE);
                    });
        } catch (Exception ignored) {
            tvInitials.setVisibility(View.VISIBLE);
        }
    }

    private void saveProfile() {
        if (TextUtils.isEmpty(activeWriterId)) {
            Toast.makeText(getContext(), "Unable to update profile", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = safe(etFirstName.getText() != null ? etFirstName.getText().toString() : "");
        String lastName = safe(etLastName.getText() != null ? etLastName.getText().toString() : "");
        String mobile = safe(etMobile.getText() != null ? etMobile.getText().toString() : "");
        String bio = safe(etBio.getText() != null ? etBio.getText().toString() : "");
        String interests = safe(etInterests.getText() != null ? etInterests.getText().toString() : "");

        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            return;
        }
        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            return;
        }
        if (mobile.isEmpty()) {
            etMobile.setError("Mobile is required");
            return;
        }

        setProfileSaving(true);

        updateUserDocument(firstName, lastName, mobile, bio, interests, currentProfilePicUrl);
    }

    private void uploadProfileImageOnly(Uri imageUri) {
        if (TextUtils.isEmpty(activeWriterId)) {
            Toast.makeText(getContext(), "Unable to update profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        btnChangeProfileImage.setEnabled(false);

        // Use a unique object key so the new image URL changes and avoids stale cache.
        String fileName = activeWriterId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = firebaseStorage.getReference()
                .child("profile_images")
                .child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String newUrl = uri.toString();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("profilePicURL", newUrl);

                            firebaseFirestore.collection("Users").document(activeWriterId)
                                    .update(updates)
                                    .addOnSuccessListener(unused -> {
                                        currentProfilePicUrl = newUrl;
                                        loadAvatar(newUrl);
                                        btnChangeProfileImage.setEnabled(true);
                                        Toast.makeText(getContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnChangeProfileImage.setEnabled(true);
                                        Toast.makeText(getContext(), "Image save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            btnChangeProfileImage.setEnabled(true);
                            Toast.makeText(getContext(), "Image URL error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    btnChangeProfileImage.setEnabled(true);
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserDocument(String firstName, String lastName, String mobile,
                                    String bio, String interests, String profilePicUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("mobile", mobile);
        updates.put("bio", bio);
        updates.put("interests", interests);
        updates.put("profilePicURL", profilePicUrl);

        firebaseFirestore.collection("Users").document(activeWriterId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    resetSaveState();
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    loadWriterData();
                })
                .addOnFailureListener(e -> {
                    resetSaveState();
                    Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void callWriter() {
        if (TextUtils.isEmpty(loadedMobile)) {
            Toast.makeText(getContext(), "Writer mobile number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + loadedMobile));
        startActivity(intent);
    }

    private void resetSaveState() {
        setProfileSaving(false);
        btnEditProfile.setVisibility(View.VISIBLE);
        editProfileSection.setVisibility(View.GONE);
    }

    private void setProfileSaving(boolean isSaving) {
        if (progressSaveProfile != null) {
            progressSaveProfile.setVisibility(isSaving ? View.VISIBLE : View.GONE);
        }
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(!isSaving);
            btnSaveProfile.setText(isSaving ? "Saving..." : "Save");
        }
        if (btnCancelEdit != null) {
            btnCancelEdit.setEnabled(!isSaving);
        }
        if (btnChangeProfileImage != null) {
            btnChangeProfileImage.setEnabled(!isSaving);
        }
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

    private String formatRating(Double rating) {
        if (rating == null || rating <= 0) {
            return "N/A";
        }
        return String.format(java.util.Locale.US, "%.1f", rating);
    }
}