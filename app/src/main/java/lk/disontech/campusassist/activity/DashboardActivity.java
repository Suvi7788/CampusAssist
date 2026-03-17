package lk.disontech.campusassist.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.fragment.fragment.student.BrowseWritersFragment;
import lk.disontech.campusassist.fragment.fragment.student.HelpGuidelinesFragment;
import lk.disontech.campusassist.fragment.fragment.student.MyAssignmentsFragment;
import lk.disontech.campusassist.fragment.fragment.student.NotificationsFragment;
import lk.disontech.campusassist.fragment.fragment.student.PostNewAssignmentFragment;
import lk.disontech.campusassist.fragment.fragment.student.StudentDashboardFragment;
import lk.disontech.campusassist.fragment.fragment.writer.BrowseAssignmentsFragment;
import lk.disontech.campusassist.fragment.fragment.writer.MyAcceptedWorkFragment;
import lk.disontech.campusassist.fragment.fragment.writer.WriterDashboardFragment;

public class DashboardActivity extends AppCompatActivity
        implements StudentDashboardFragment.StudentDashboardNavigator,
        WriterDashboardFragment.WriterDashboardNavigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (savedInstanceState != null) return;

        String role = getIntent().getStringExtra("role");
        if (role == null) role = "student";

        switch (role.toLowerCase()) {
            case "student":
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new StudentDashboardFragment())
                        .commit();
                break;

            case "writer":
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new WriterDashboardFragment())
                        .commit();
                break;

            default:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new StudentDashboardFragment())
                        .commit();
                break;
        }
    }

    // =========================
    // Student Navigation
    // =========================

    @Override
    public void openPostNewAssignment() {
        openFragment(new PostNewAssignmentFragment());
    }

    @Override
    public void openMyAssignments() {
        openFragment(new MyAssignmentsFragment());
    }

    @Override
    public void openBrowseWriters() {
        openFragment(new BrowseWritersFragment());
    }

    @Override
    public void openNotifications() {
        openFragment(new NotificationsFragment());
    }

    @Override
    public void openHelpGuidelines() {
        openFragment(new HelpGuidelinesFragment());
    }

    // =========================
    // Writer Navigation
    // =========================

    @Override
    public void openBrowseAssignments() {
        openFragment(new BrowseAssignmentsFragment());
    }

    @Override
    public void openAcceptedWork() {
        openFragment(new MyAcceptedWorkFragment());

    }

    @Override
    public void openProfile() {
        // TODO: Create WriterProfileFragment later
        // openFragment(new WriterProfileFragment());
    }

    @Override
    public void openDrawerOrMenu() {
        // TODO: Open drawer/menu later
    }

    // =========================
    // Common Fragment Opener
    // =========================

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}