package lk.disontech.campusassist.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.fragment.fragment.student.BrowseWritersFragment;
import lk.disontech.campusassist.fragment.fragment.student.HelpGuidelinesFragment;
import lk.disontech.campusassist.fragment.fragment.student.MyAssignmentsFragment;
import lk.disontech.campusassist.fragment.fragment.student.NotificationsFragment;
import lk.disontech.campusassist.fragment.fragment.student.StudentDashboardFragment;
import lk.disontech.campusassist.fragment.fragment.student.PostNewAssignmentFragment;
import lk.disontech.campusassist.fragment.fragment.writer.WriterDashboardFragment;


public class DashboardActivity extends AppCompatActivity
        implements StudentDashboardFragment.StudentDashboardNavigator {

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

            // TODO later:
            // case "writer": load WriterDashboardFragment
            // case "admin": load AdminDashboardFragment

            default:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new StudentDashboardFragment())
                        .commit();
                break;
        }
    }

    // ✅ This is called from StudentDashboardFragment when the card is clicked
    @Override
    public void openPostNewAssignment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new PostNewAssignmentFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openMyAssignments() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new MyAssignmentsFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openBrowseWriters() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new BrowseWritersFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openNotifications() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new NotificationsFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openHelpGuidelines() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new HelpGuidelinesFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openDrawerOrMenu() { /* later */ }
}