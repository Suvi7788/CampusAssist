package lk.disontech.campusassist.fragment.fragment.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import lk.disontech.campusassist.R;

public class WriterProfileFragment extends Fragment {

    private TextView tvInitials, tvName, tvField, tvRating, tvReviews;
    private TextView tvCompleted, tvStatRating, tvTopPercent, tvAbout, tvEducation;
    private MaterialButton btnContact, btnPortfolio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_writer_profile, container, false);

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

        btnContact = view.findViewById(R.id.btnContact);
        btnPortfolio = view.findViewById(R.id.btnPortfolio);

        // Dummy data (later pass via Bundle from BrowseWritersFragment)
        tvInitials.setText("SJ");
        tvName.setText("Dr. Sarah Johnson");
        tvField.setText("Mathematics & Statistics");
        tvRating.setText("4.9");
        tvReviews.setText("(127 reviews)");

        tvCompleted.setText("145");
        tvStatRating.setText("4.9");
        tvTopPercent.setText("Top 5%");

        tvAbout.setText(
                "Ph.D. in Applied Mathematics from MIT.\n" +
                        "10+ years of academic writing experience. Specialized in statistical analysis, calculus, " +
                        "and research methodology."
        );

        tvEducation.setText("Ph.D. Mathematics, MIT");

        btnContact.setOnClickListener(v ->
                Toast.makeText(getContext(), "Contact clicked", Toast.LENGTH_SHORT).show()
        );

        btnPortfolio.setOnClickListener(v ->
                Toast.makeText(getContext(), "Portfolio clicked", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}