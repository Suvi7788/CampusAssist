package lk.disontech.campusassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

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

        // Login button click
        binding.btnLogin.setOnClickListener(v -> performLogin());

        // Register link click
        binding.tvRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            i.putExtra("role", selectedRole);
            startActivity(i);
        });
    }

    private void updateRoleUI(String role) {
        boolean isStudent = "Student".equalsIgnoreCase(role);
        binding.btnStudent.setSelected(isStudent);
        binding.btnWriter.setSelected(!isStudent);
        binding.tvRoleHint.setText(isStudent ? "Logging in as: Student" : "Logging in as: Writer");
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
                                        if (userType == null) {
                                            userType = selectedRole;
                                        }

                                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                        // Navigate to Dashboard with user role
                                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                        intent.putExtra("role", userType);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        binding.btnLogin.setEnabled(true);
        binding.btnLogin.setText("Login");
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
}