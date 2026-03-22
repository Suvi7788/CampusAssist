package lk.disontech.campusassist.fragment.fragment.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import lk.disontech.campusassist.R;

public class HelpGuidelineDetailFragment extends Fragment {

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_FULL_DESCRIPTION = "arg_full_description";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_guideline_detail, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        TextView tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDetailDescription = view.findViewById(R.id.tvDetailDescription);
        TextView tvSupportEmail = view.findViewById(R.id.tvSupportEmail);
        TextView tvSupportPhone = view.findViewById(R.id.tvSupportPhone);

        String title = "Help";
        String fullDescription = "";

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(ARG_TITLE, title);
            fullDescription = args.getString(ARG_FULL_DESCRIPTION, fullDescription);
        }

        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        tvDetailTitle.setText(title);
        tvDetailDescription.setText(fullDescription);
        tvSupportEmail.setText("Email: support@campusassistslk");
        tvSupportPhone.setText("Phone: 0761677883");

        return view;
    }
}

