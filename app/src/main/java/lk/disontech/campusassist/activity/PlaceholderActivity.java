package lk.disontech.campusassist.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import lk.disontech.campusassist.R;

public class PlaceholderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        String title = getIntent().getStringExtra("title");
        if (title == null) title = "Screen";

        TextView tv = findViewById(R.id.tvPlaceholderTitle);
        tv.setText(title);
    }
}