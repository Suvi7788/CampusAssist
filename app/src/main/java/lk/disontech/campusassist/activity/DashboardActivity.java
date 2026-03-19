package lk.disontech.campusassist.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

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

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private String role = "student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        String intentRole = getIntent().getStringExtra("role");
        if (intentRole != null && !intentRole.trim().isEmpty()) {
            role = intentRole.toLowerCase();
        }

        setupHeader();
        setupMenuByRole();

        if (savedInstanceState == null) {
            if ("writer".equals(role)) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new WriterDashboardFragment())
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new StudentDashboardFragment())
                        .commit();
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        TextView tvUserRole = headerView.findViewById(R.id.tvUserRole);

        tvUserName.setText("CampusAssist User");
        tvUserRole.setText("writer".equals(role) ? "Writer" : "Student");
    }

    private void setupMenuByRole() {
        navigationView.getMenu().clear();

        if ("writer".equals(role)) {
            navigationView.inflateMenu(R.menu.writer_drawer_menu);
            setupWriterMenuClicks();
        } else {
            navigationView.inflateMenu(R.menu.student_drawer_menu);
            setupStudentMenuClicks();
        }
    }

    private void setupStudentMenuClicks() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceRootFragment(new StudentDashboardFragment());
            } else if (id == R.id.nav_post_assignment) {
                openPostNewAssignment();
            } else if (id == R.id.nav_my_assignments) {
                openMyAssignments();
            } else if (id == R.id.nav_browse_writers) {
                openBrowseWriters();
            } else if (id == R.id.nav_notifications) {
                openNotifications();
            } else if (id == R.id.nav_help) {
                openHelpGuidelines();
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupWriterMenuClicks() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_writer_home) {
                replaceRootFragment(new WriterDashboardFragment());
            } else if (id == R.id.nav_available_assignments) {
                openBrowseAssignments();
            } else if (id == R.id.nav_my_work) {
                openAcceptedWork();
            } else if (id == R.id.nav_submissions) {
                Toast.makeText(this, "Open Submissions screen", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_earnings) {
                Toast.makeText(this, "Open Earnings screen", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_notifications) {
                openNotifications();
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void replaceRootFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .commit();
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

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
        Toast.makeText(this, "Open Profile screen", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openDrawerOrMenu() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}