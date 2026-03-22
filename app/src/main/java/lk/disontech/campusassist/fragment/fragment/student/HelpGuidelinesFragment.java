package lk.disontech.campusassist.fragment.fragment.student;

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
import lk.disontech.campusassist.adapter.HelpGuidelineAdapter;
import lk.disontech.campusassist.model.HelpGuidelineItem;

public class HelpGuidelinesFragment extends Fragment {

    public static final String ARG_ROLE = "arg_role";

    private RecyclerView recyclerHelp;
    private HelpGuidelineAdapter helpGuidelineAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_guidelines, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        recyclerHelp = view.findViewById(R.id.recyclerHelp);
        recyclerHelp.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHelp.setNestedScrollingEnabled(false);

        helpGuidelineAdapter = new HelpGuidelineAdapter(this::openHelpDetail);
        recyclerHelp.setAdapter(helpGuidelineAdapter);
        helpGuidelineAdapter.submitList(getHelpItemsByRole(resolveRole()));

        return view;
    }

    private String resolveRole() {
        Bundle args = getArguments();
        if (args != null) {
            String role = args.getString(ARG_ROLE, "student");
            return role == null ? "student" : role.trim().toLowerCase();
        }
        return "student";
    }

    private List<HelpGuidelineItem> getHelpItemsByRole(String role) {
        if ("writer".equalsIgnoreCase(role)) {
            return getWriterHelpItems();
        }
        return getStudentHelpItems();
    }

    private List<HelpGuidelineItem> getStudentHelpItems() {
        List<HelpGuidelineItem> items = new ArrayList<>();
        items.add(new HelpGuidelineItem(
                "How to Post an Assignment",
                "Create assignments with all required details so writers can bid accurately.",
                "To post an assignment:\n\n"
                        + "Go to Post New Assignment.\n"
                        + "Enter title, subject, deadline, and payment amount.\n"
                        + "Select your location using the map.\n"
                        + "Add clear instructions and requirements.\n"
                        + "Click Save.\n\n"
                        + "Make sure your details are clear to get better bids from writers.",
                "#1E3A8A",
                android.R.drawable.ic_menu_edit
        ));
        items.add(new HelpGuidelineItem(
                "Payments & Security",
                "Secure payments handled through PayHere after work is completed.",
                "Payments are made securely using PayHere:\n\n"
                        + "You only pay after the writer submits the assignment.\n"
                        + "Payment is verified before marking the assignment as completed.\n"
                        + "Always review the work before making payment.\n"
                        + "Never share payment details outside the app.",
                "#10B981",
                android.R.drawable.ic_lock_lock
        ));
        items.add(new HelpGuidelineItem(
                "Accepting Bids",
                "Choose the best writer based on their bid and profile.",
                "Review all bids from writers.\n"
                        + "Check their profile and completed work.\n"
                        + "Accept one bid to assign the task.\n"
                        + "Once accepted, other bids will be automatically cancelled.",
                "#F97316",
                android.R.drawable.ic_menu_agenda
        ));
        items.add(new HelpGuidelineItem(
                "Communication",
                "Contact writers directly through provided contact options.",
                "You can view writer details after they bid.\n"
                        + "Communicate clearly about requirements.\n"
                        + "Avoid sharing sensitive personal information.",
                "#8B5CF6",
                android.R.drawable.ic_dialog_email
        ));
        items.add(new HelpGuidelineItem(
                "Rules & Guidelines",
                "Follow platform rules to ensure a safe experience.",
                "Do not post illegal or inappropriate content.\n"
                        + "Do not misuse the platform.\n"
                        + "Respect writers and communicate professionally.\n"
                        + "Violating rules may result in account restrictions.",
                "#EF4444",
                android.R.drawable.ic_menu_info_details
        ));
        return items;
    }

    private List<HelpGuidelineItem> getWriterHelpItems() {
        List<HelpGuidelineItem> items = new ArrayList<>();
        items.add(new HelpGuidelineItem(
                "How to Bid for Assignments",
                "Browse assignments and place bids to get work.",
                "Go to Browse Assignments.\n"
                        + "View assignment details.\n"
                        + "Click Bid for Assignment.\n"
                        + "Submit your offer.\n"
                        + "Make sure your bid is realistic and competitive.",
                "#1E3A8A",
                android.R.drawable.ic_input_add
        ));
        items.add(new HelpGuidelineItem(
                "Completing Assignments",
                "Submit completed work with files and notes.",
                "Once assigned, start the work.\n"
                        + "Upload completed files.\n"
                        + "Add notes for the student.\n"
                        + "Mark assignment as completed.\n"
                        + "Ensure quality before submission.",
                "#10B981",
                android.R.drawable.ic_menu_upload
        ));
        items.add(new HelpGuidelineItem(
                "Earnings & Payments",
                "Earn money after the student completes payment.",
                "Payment is marked as completed after student pays.\n"
                        + "Only paid assignments count as earnings.\n"
                        + "Track your earnings in the dashboard.\n"
                        + "Always complete work properly to receive payment.",
                "#F97316",
                android.R.drawable.ic_menu_info_details
        ));
        items.add(new HelpGuidelineItem(
                "Delivery Guidelines",
                "Follow location-based delivery if required.",
                "Use the map location provided by the student.\n"
                        + "Deliver work as required.\n"
                        + "Ensure correct submission before marking complete.",
                "#8B5CF6",
                android.R.drawable.ic_menu_mylocation
        ));
        items.add(new HelpGuidelineItem(
                "Rules & Conduct",
                "Maintain professionalism and follow platform rules.",
                "Do not submit incomplete or fake work.\n"
                        + "Respect deadlines.\n"
                        + "Communicate professionally.\n"
                        + "Violation may lead to account suspension.",
                "#EF4444",
                android.R.drawable.ic_lock_lock
        ));
        return items;
    }

    private void openHelpDetail(HelpGuidelineItem item) {
        if (item == null || !isAdded()) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(HelpGuidelineDetailFragment.ARG_TITLE, item.getTitle());
        bundle.putString(HelpGuidelineDetailFragment.ARG_FULL_DESCRIPTION, item.getFullDescription());

        HelpGuidelineDetailFragment detailFragment = new HelpGuidelineDetailFragment();
        detailFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}