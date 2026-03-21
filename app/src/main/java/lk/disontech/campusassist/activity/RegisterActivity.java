package lk.disontech.campusassist.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.databinding.ActivityRegisterBinding;
import lk.disontech.campusassist.model.User;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ValueAnimator registerLoadingAnimator;
    private boolean entryAnimationPlayed = false;

    private String selectedUserType = "Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup navigation bar styling
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.md_theme_onPrimary));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setupUserTypeDropdown();

        // Register button click listener with tactile animation
        binding.btnRegister.setOnClickListener(v -> animateButtonPress(binding.btnRegister, this::onRegister));

        // Login link click listener
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        playEntryAnimation();
    }

    private void setupUserTypeDropdown() {
        String[] userTypes = {"Student", "Writer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                userTypes
        );
        binding.actUserType.setAdapter(adapter);

        // Set default
        binding.actUserType.setText(selectedUserType, false);

        binding.actUserType.setOnItemClickListener((parent, view, position, id) -> {
            selectedUserType = parent.getItemAtPosition(position).toString();
        });
    }

    private void onRegister() {
        String firstName = safeText(binding.etFirstName);
        String lastName = safeText(binding.etLastName);
        String email = safeText(binding.etEmail);
        String mobile = safeText(binding.etMobile);
        String password = safeText(binding.etPassword);
        String userType = (binding.actUserType.getText() != null && binding.actUserType.getText().toString().trim().length() > 0)
                ? binding.actUserType.getText().toString().trim()
                : selectedUserType;

        // Clear previous errors
        clearErrors();

        // Validation
        if (firstName.isEmpty()) {
            binding.etFirstName.setError("First name is required!");
            binding.etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            binding.etLastName.setError("Last name is required!");
            binding.etLastName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required!");
            binding.etEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email address!");
            binding.etEmail.requestFocus();
            return;
        }

        if (mobile.isEmpty()) {
            binding.etMobile.setError("Mobile number is required!");
            binding.etMobile.requestFocus();
            return;
        } else if (mobile.length() < 9) {
            binding.etMobile.setError("Please enter a valid mobile number!");
            binding.etMobile.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required!");
            binding.etPassword.requestFocus();
            return;
        } else if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters!");
            binding.etPassword.requestFocus();
            return;
        }

        // Disable button and show progress
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Registering...");
        startRegisterLoadingAnimation();

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Create user object
                        User user = User.builder()
                                .uId(uid)
                                .firstName(firstName)
                                .lastName(lastName)
                                .email(email)
                                .mobile(mobile)
                                .userType(userType)
                                .build();

                        // Save to Firestore
                        firebaseFirestore.collection("Users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                                    // Navigate to LoginActivity
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    stopRegisterLoadingAnimation();
                                    startActivity(intent);
                                    finish();

                                }).addOnFailureListener(e -> {
                                    resetButton();
                                    Toast.makeText(RegisterActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        resetButton();
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void clearErrors() {
        binding.etFirstName.setError(null);
        binding.etLastName.setError(null);
        binding.etEmail.setError(null);
        binding.etMobile.setError(null);
        binding.etPassword.setError(null);
    }

    private void resetButton() {
        stopRegisterLoadingAnimation();
        binding.btnRegister.setEnabled(true);
        binding.btnRegister.setText("Register");
        binding.btnRegister.setAlpha(1f);
        binding.btnRegister.setScaleX(1f);
        binding.btnRegister.setScaleY(1f);
    }

    private void playEntryAnimation() {
        if (entryAnimationPlayed) {
            return;
        }
        entryAnimationPlayed = true;

        View[] animatedViews = new View[]{
                binding.logoCircle,
                binding.tvTitle,
                binding.tvSubTitle,
                binding.registerCard
        };

        for (View view : animatedViews) {
            view.setAlpha(0f);
            view.setTranslationY(36f);
        }

        binding.getRoot().post(() -> {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    createAppearAnimator(binding.logoCircle, 0L),
                    createAppearAnimator(binding.tvTitle, 80L),
                    createAppearAnimator(binding.tvSubTitle, 140L),
                    createAppearAnimator(binding.registerCard, 220L)
            );
            set.start();
        });
    }

    private Animator createAppearAnimator(View view, long startDelayMs) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
        ObjectAnimator translate = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 36f, 0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, translate);
        set.setStartDelay(startDelayMs);
        set.setDuration(360L);
        set.setInterpolator(new DecelerateInterpolator());
        return set;
    }

    private void animateButtonPress(View button, Runnable action) {
        if (!button.isEnabled()) {
            return;
        }
        button.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80L)
                .withEndAction(() -> {
                    button.animate().scaleX(1f).scaleY(1f).setDuration(80L).start();
                    action.run();
                })
                .start();
    }

    private void startRegisterLoadingAnimation() {
        stopRegisterLoadingAnimation();
        registerLoadingAnimator = ValueAnimator.ofFloat(1f, 0.55f, 1f);
        registerLoadingAnimator.setDuration(900L);
        registerLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        registerLoadingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            binding.btnRegister.setAlpha(value);
        });
        registerLoadingAnimator.start();
    }

    private void stopRegisterLoadingAnimation() {
        if (registerLoadingAnimator != null) {
            registerLoadingAnimator.cancel();
            registerLoadingAnimator = null;
        }
    }

    private String safeText(TextInputEditText editText) {
        return (editText.getText() == null) ? "" : editText.getText().toString().trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onDestroy() {
        stopRegisterLoadingAnimation();
        super.onDestroy();
    }
}