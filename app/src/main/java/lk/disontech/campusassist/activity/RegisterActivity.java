package lk.disontech.campusassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import lk.disontech.campusassist.R;

import android.widget.ArrayAdapter;

public class RegisterActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView actUserType;
    private TextInputEditText etFirstName, etLastName, etEmail, etMobile, etPassword;
    private MaterialButton btnRegisterb;
    private TextView tvLoginb;

    private String selectedUserType = "Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        actUserType = findViewById(R.id.actUserType);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnRegisterb = findViewById(R.id.btnRegister);
        tvLoginb = findViewById(R.id.tvLogin);

        setupUserTypeDropdown();

        btnRegisterb.setOnClickListener(v -> onRegister());
        tvLoginb.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupUserTypeDropdown() {
        String[] userTypes = {"Student", "Writer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                userTypes
        );
        actUserType.setAdapter(adapter);

        // default
        actUserType.setText(selectedUserType, false);

        actUserType.setOnItemClickListener((parent, view, position, id) -> {
            selectedUserType = parent.getItemAtPosition(position).toString();
        });
    }

    private void onRegister() {
        String firstName = safeText(etFirstName);
        String lastName = safeText(etLastName);
        String email = safeText(etEmail);
        String mobile = safeText(etMobile);
        String password = safeText(etPassword);
        String userType = (actUserType.getText() != null && actUserType.getText().toString().trim().length() > 0)
                ? actUserType.getText().toString().trim()
                : selectedUserType;

        // Basic validations
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (mobile.isEmpty() || mobile.length() < 9) {
            etMobile.setError("Enter a valid mobile number");
            etMobile.requestFocus();
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // TODO: Call your API / Firebase / DB insert here
        // For now: just show a success toast and go to login
        Toast.makeText(this,
                "Registered as " + userType + " ✅",
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    private String safeText(TextInputEditText editText) {
        return (editText.getText() == null) ? "" : editText.getText().toString().trim();
    }
}