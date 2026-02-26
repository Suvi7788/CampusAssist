package lk.disontech.campusassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.activity.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardStudentC, cardWriterC, cardAdminC;
    private View btnGoLoginB, btnViewAllScreens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardStudentC = findViewById(R.id.cardStudent);
        cardWriterC = findViewById(R.id.cardWriter);
        cardAdminC = findViewById(R.id.cardAdmin);

        btnGoLoginB = findViewById(R.id.btnGoLogin);
//        btnViewAllScreens = findViewById(R.id.btnViewAllScreens);

        cardStudentC.setOnClickListener(v -> openLoginWithRole("student"));
        cardWriterC.setOnClickListener(v -> openLoginWithRole("writer"));
        cardAdminC.setOnClickListener(v -> openLoginWithRole("admin"));

        btnGoLoginB.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        });
        // Optional: create a simple activity to show all screens
//        btnViewAllScreens.setOnClickListener(v ->
//                startActivity(new Intent(this, ViewAllScreensActivity.class))
//        );
    }

    private void openLoginWithRole(String role) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("role", role); // you can read this in LoginActivity
        startActivity(i);
    }
}