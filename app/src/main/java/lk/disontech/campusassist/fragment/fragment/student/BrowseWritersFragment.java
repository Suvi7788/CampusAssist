package lk.disontech.campusassist.fragment.fragment.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import lk.disontech.campusassist.R;

public class BrowseWritersFragment extends Fragment {

    private LinearLayout writersContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse_writers, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        writersContainer = view.findViewById(R.id.writersContainer);

        loadDummyWriters(inflater);

        return view;
    }

    private void loadDummyWriters(LayoutInflater inflater) {
        addWriter(inflater, "Dr. Sarah Johnson", "Mathematics & Statistics", "SJ");
        addWriter(inflater, "Prof. Michael Chen", "Computer Science", "MC");
        addWriter(inflater, "Dr. Emily Brown", "Literature & History", "EB");
        addWriter(inflater, "James Wilson", "Physics & Engineering", "JW");
    }

    private void addWriter(LayoutInflater inflater,
                           String name,
                           String field,
                           String initials) {

        View card = inflater.inflate(R.layout.item_writer_card, writersContainer, false);

        TextView tvName = card.findViewById(R.id.tvName);
        TextView tvField = card.findViewById(R.id.tvField);
        TextView tvInitials = card.findViewById(R.id.tvInitials);

        MaterialButton btnViewProfile = card.findViewById(R.id.btnViewProfile);
        MaterialButton btnContact = card.findViewById(R.id.btnContact);

        tvName.setText(name);
        tvField.setText(field);
        tvInitials.setText(initials);

        btnViewProfile.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            bundle.putString("field", field);
            bundle.putString("initials", initials);

            WriterProfileFragment fragment = new WriterProfileFragment();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.dashboardContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnContact.setOnClickListener(v ->
                Toast.makeText(getContext(), "Contacting: " + name, Toast.LENGTH_SHORT).show()
        );

        writersContainer.addView(card);
    }
}