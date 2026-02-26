package lk.disontech.campusassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.fragment.fragment.student.StudentDashboardFragment;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton btnStudent, btnWriter, btnLogin ;
    private TextView tvRoleHint,tvRegister;

    private String selectedRole = "student"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnStudent = findViewById(R.id.btnStudent);
        btnWriter  = findViewById(R.id.btnWriter);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRoleHint = findViewById(R.id.tvRoleHint);
        tvRegister= findViewById(R.id.tvRegister);


        // If MainActivity passes role via Intent
        String roleFromIntent = getIntent().getStringExtra("role");
        if (roleFromIntent != null) selectedRole = roleFromIntent;

        updateRoleUI(selectedRole);

        btnStudent.setOnClickListener(v -> {
            selectedRole = "student";
            updateRoleUI(selectedRole);
        });

        btnWriter.setOnClickListener(v -> {
            selectedRole = "writer";
            updateRoleUI(selectedRole);
        });

        btnLogin.setOnClickListener(v -> {

            if ("student".equalsIgnoreCase(selectedRole)) {
                Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                i.putExtra("role", "student");
                startActivity(i);
                finish();
            } else if ("writer".equalsIgnoreCase(selectedRole)) {
                Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                i.putExtra("role", "writer");
                startActivity(i);
                finish();
            }


        });

        tvRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            i.putExtra("role", selectedRole); // pass current selected role
            startActivity(i);
        });
    }

    private void updateRoleUI(String role) {
        boolean isStudent = "student".equalsIgnoreCase(role);

        btnStudent.setSelected(isStudent);
        btnWriter.setSelected(!isStudent);

        // Small label (optional)
        tvRoleHint.setText(isStudent ? "Logging in as: Student" : "Logging in as: Writer");
    }
}