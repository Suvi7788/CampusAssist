package lk.disontech.campusassist.fragment.fragment.writer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import lk.disontech.campusassist.R;

public class WriterSubmissionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_writer_submissions, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }
}

