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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ValueAnimator loginLoadingAnimator;
    private boolean entryAnimationPlayed = false;
    private boolean sessionRedirectInProgress = false;

    private String selectedRole = "Student"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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

        // If LoginActivity passes role via Intent
        String roleFromIntent = getIntent().getStringExtra("role");
        if (roleFromIntent != null) {
            selectedRole = roleFromIntent;
        }

        updateRoleUI(selectedRole);

        // Student button click
        binding.btnStudent.setOnClickListener(v -> {
            selectedRole = "Student";
            updateRoleUI(selectedRole);
        });

        // Writer button click
        binding.btnWriter.setOnClickListener(v -> {
            selectedRole = "Writer";
            updateRoleUI(selectedRole);
        });

        // Login button click with tactile animation
        binding.btnLogin.setOnClickListener(v -> animateButtonPress(binding.btnLogin, this::performLogin));

        // Register link click
        binding.tvRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            i.putExtra("role", selectedRole);
            startActivity(i);
        });

        playEntryAnimation();
    }

    private void updateRoleUI(String role) {
        boolean isStudent = "Student".equalsIgnoreCase(role);
        binding.btnStudent.setSelected(isStudent);
        binding.btnWriter.setSelected(!isStudent);
        binding.tvRoleHint.setText(isStudent ? "Logging in as: Student" : "Logging in as: Writer");
    }

    @Override
    protected void onStart() {
        super.onStart();
        redirectIfSessionExists();
    }

    private void redirectIfSessionExists() {
        if (sessionRedirectInProgress) {
            return;
        }

        FirebaseUser currentUser = firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
        if (currentUser == null) {
            return;
        }

        sessionRedirectInProgress = true;
        firebaseFirestore.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userType = documentSnapshot != null ? documentSnapshot.getString("userType") : null;
                    openDashboardAndClearTask(normalizeRole(userType, selectedRole));
                })
                .addOnFailureListener(e -> openDashboardAndClearTask("student"));
    }

    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Clear errors
        clearErrors();

        // Validation
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required!");
            binding.etEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email address!");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required!");
            binding.etPassword.requestFocus();
            return;
        }

        // Show progress
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Logging in...");
        startLoginLoadingAnimation();

        // Firebase Login
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        String uid = firebaseAuth.getCurrentUser().getUid();

                        // Fetch user role from Firestore
                        firebaseFirestore.collection("Users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String userType = documentSnapshot.getString("userType");
                                        String normalizedRole = normalizeRole(userType, selectedRole);

                                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                        // Navigate to Dashboard with user role
                                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                        intent.putExtra("role", normalizedRole);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        stopLoginLoadingAnimation();
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        resetButton();
                                        Toast.makeText(LoginActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    resetButton();
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        resetButton();
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, "Login Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void clearErrors() {
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);
    }

    private void resetButton() {
        stopLoginLoadingAnimation();
        binding.btnLogin.setEnabled(true);
        binding.btnLogin.setText("Login");
        binding.btnLogin.setAlpha(1f);
        binding.btnLogin.setScaleX(1f);
        binding.btnLogin.setScaleY(1f);
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
                binding.loginCard
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
                    createAppearAnimator(binding.loginCard, 220L)
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

    private void startLoginLoadingAnimation() {
        stopLoginLoadingAnimation();
        loginLoadingAnimator = ValueAnimator.ofFloat(1f, 0.55f, 1f);
        loginLoadingAnimator.setDuration(900L);
        loginLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        loginLoadingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            binding.btnLogin.setAlpha(value);
        });
        loginLoadingAnimator.start();
    }

    private void stopLoginLoadingAnimation() {
        if (loginLoadingAnimator != null) {
            loginLoadingAnimator.cancel();
            loginLoadingAnimator = null;
        }
    }

    private String normalizeRole(String roleFromUserData, String fallbackRole) {
        String raw = roleFromUserData;
        if (raw == null || raw.trim().isEmpty()) {
            raw = fallbackRole;
        }

        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("writ")) {
            return "writer";
        }
        if (normalized.contains("stud")) {
            return "student";
        }
        if (normalized.contains("admin")) {
            return "admin";
        }
        return "student";
    }

    private void openDashboardAndClearTask(String normalizedRole) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("role", normalizedRole);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        stopLoginLoadingAnimation();
        startActivity(intent);
        finish();
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
        stopLoginLoadingAnimation();
        super.onDestroy();
    }
}