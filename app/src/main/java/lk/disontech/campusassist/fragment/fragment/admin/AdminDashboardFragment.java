//package lk.disontech.campusassist.fragment.fragment.admin;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import lk.disontech.campusassist.R;
//
//public class AdminDashboardFragment extends Fragment {
//
//    public AdminDashboardFragment() {
//        // Required empty public constructor
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(
//                R.layout.fragment_student_dashboard,
//                container,
//                false
//        );
//
//        // Example click handling
//        view.findViewById(R.id.card_post_assignment)
//                .setOnClickListener(v ->
//                        Toast.makeText(getContext(),
//                                "Post Assignment Clicked",
//                                Toast.LENGTH_SHORT).show()
//                );
//
//        view.findViewById(R.id.card_my_assignments)
//                .setOnClickListener(v ->
//                        Toast.makeText(getContext(),
//                                "My Assignments Clicked",
//                                Toast.LENGTH_SHORT).show()
//                );
//
//        view.findViewById(R.id.card_browse_writers)
//                .setOnClickListener(v ->
//                        Toast.makeText(getContext(),
//                                "Browse Writers Clicked",
//                                Toast.LENGTH_SHORT).show()
//                );
//
//        return view;
//    }
//}