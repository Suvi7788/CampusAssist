package lk.disontech.campusassist.fragment.fragment.writer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.AcceptedWorkAdapter;
import lk.disontech.campusassist.fragment.fragment.student.AssignmentDetailsFragment;
import lk.disontech.campusassist.model.AcceptedWorkModel;

public class MyAcceptedWorkFragment extends Fragment {

    private RecyclerView recyclerAcceptedWork;
    private AcceptedWorkAdapter acceptedWorkAdapter;
    private List<AcceptedWorkModel> acceptedWorkList;

    public MyAcceptedWorkFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_accepted_work, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        recyclerAcceptedWork = view.findViewById(R.id.recyclerAcceptedWork);

        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        recyclerAcceptedWork.setLayoutManager(new LinearLayoutManager(getContext()));

        acceptedWorkList = new ArrayList<>();
        acceptedWorkList.add(new AcceptedWorkModel(
                "Research Paper on Climate Change",
                "Environmental Science",
                "John Doe",
                "Feb 28, 2026",
                "In Progress"
        ));

        acceptedWorkList.add(new AcceptedWorkModel(
                "Machine Learning Project",
                "Computer Science",
                "Jane Smith",
                "Mar 5, 2026",
                "In Progress"
        ));

        acceptedWorkList.add(new AcceptedWorkModel(
                "Essay on Shakespeare",
                "Literature",
                "Mike Johnson",
                "Feb 22, 2026",
                "Under Review"
        ));

        acceptedWorkAdapter = new AcceptedWorkAdapter(getContext(), acceptedWorkList, model -> {
            Bundle b = new Bundle();
            b.putString("title", model.getTitle());
            b.putString("subject", model.getSubject());
            b.putString("student", model.getStudentName());
            b.putString("deadline", model.getDueDate());
            b.putString("status", model.getStatus());

            AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
            fragment.setArguments(b);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.dashboardContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerAcceptedWork.setAdapter(acceptedWorkAdapter);

        return view;
    }
}