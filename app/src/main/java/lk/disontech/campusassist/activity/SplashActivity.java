package lk.disontech.campusassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import lk.disontech.campusassist.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 4000;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            routeFromSplash();
        }, SPLASH_DELAY);
    }

    private void routeFromSplash() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            openMain();
            return;
        }

        firebaseFirestore.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String rawRole = documentSnapshot != null ? documentSnapshot.getString("userType") : null;
                    openDashboard(normalizeRole(rawRole));
                })
                .addOnFailureListener(e -> openDashboard("student"));
    }

    private void openMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openDashboard(String role) {
        Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
        intent.putExtra("role", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String normalizeRole(String rawRole) {
        String normalized = rawRole == null ? "" : rawRole.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("writ")) {
            return "writer";
        }
        return "student";
    }
}