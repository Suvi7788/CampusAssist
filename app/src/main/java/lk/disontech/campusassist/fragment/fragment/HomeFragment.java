//package lk.disontech.campusassist.fragment;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import lk.disontech.campusassist.R;
//
//public class HomeFragment extends Fragment {
//
//    private TextView tvWelcome, tvRoleMessage;
//    private Button btnPrimaryAction;
//
//    private String userRole;
//
//    public HomeFragment(String role) {
//        this.userRole = role;
//    }
//
//    public HomeFragment() {
//        // Required empty constructor
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
//
//        tvWelcome = view.findViewById(R.id.tv_welcome);
//        tvRoleMessage = view.findViewById(R.id.tv_role_message);
//        btnPrimaryAction = view.findViewById(R.id.btn_primary_action);
//
//        setupUI();
//
//        return view;
//    }
//
//    private void setupUI() {
//
//        tvWelcome.setText("Welcome to CampusAssist 👋");
//
//        if (userRole == null) {
//            userRole = "student";
//        }
//
//        switch (userRole) {
//
//            case "student":
//                tvRoleMessage.setText("Find writers and post your assignments.");
//                btnPrimaryAction.setText("Post Assignment");
//                break;
//
//            case "writer":
//                tvRoleMessage.setText("Browse assignments and start earning.");
//                btnPrimaryAction.setText("View Available Tasks");
//                break;
//
//            case "admin":
//                tvRoleMessage.setText("Manage users and monitor the platform.");
//                btnPrimaryAction.setText("Open Admin Panel");
//                break;
//
//            default:
//                tvRoleMessage.setText("Welcome!");
//                btnPrimaryAction.setText("Explore");
//                break;
//        }
//
//        btnPrimaryAction.setOnClickListener(v ->
//                Toast.makeText(getContext(),
//                        btnPrimaryAction.getText() + " clicked",
//                        Toast.LENGTH_SHORT).show()
//        );
//    }
//}