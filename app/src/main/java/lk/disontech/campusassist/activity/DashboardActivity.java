package lk.disontech.campusassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.fragment.fragment.student.BrowseWritersFragment;
import lk.disontech.campusassist.fragment.fragment.student.HelpGuidelinesFragment;
import lk.disontech.campusassist.fragment.fragment.student.MyAssignmentsFragment;
import lk.disontech.campusassist.fragment.fragment.student.NotificationsFragment;
import lk.disontech.campusassist.fragment.fragment.student.PostNewAssignmentFragment;
import lk.disontech.campusassist.fragment.fragment.student.StudentDashboardFragment;
import lk.disontech.campusassist.fragment.fragment.student.WriterProfileFragment;
import lk.disontech.campusassist.fragment.fragment.writer.BrowseAssignmentsFragment;
import lk.disontech.campusassist.fragment.fragment.writer.MyAcceptedWorkFragment;
import lk.disontech.campusassist.fragment.fragment.writer.WriterEarningsFragment;
import lk.disontech.campusassist.fragment.fragment.writer.WriterDashboardFragment;
import lk.disontech.campusassist.fragment.fragment.writer.WriterSubmissionsFragment;

public class DashboardActivity extends AppCompatActivity
        implements StudentDashboardFragment.StudentDashboardNavigator,
        WriterDashboardFragment.WriterDashboardNavigator {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private View logoutLoadingOverlay;
    private boolean isLogoutInProgress = false;
    private String role = "student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        logoutLoadingOverlay = findViewById(R.id.logoutLoadingOverlay);

        role = normalizeRole(getIntent().getStringExtra("role"));

        setupHeader();
        setupMenuByRole();
        setupBottomNavByRole();

        if (savedInstanceState == null) {
            if ("writer".equals(role)) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new WriterDashboardFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_bottom_writer_home);
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboardContainer, new StudentDashboardFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_bottom_student_home);
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
            if (isLogoutInProgress) {
                return true;
            }
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
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupWriterMenuClicks() {
        navigationView.setNavigationItemSelectedListener(item -> {
            if (isLogoutInProgress) {
                return true;
            }
            int id = item.getItemId();

            if (id == R.id.nav_writer_home) {
                replaceRootFragment(new WriterDashboardFragment());
            } else if (id == R.id.nav_available_assignments) {
                openBrowseAssignments();
            } else if (id == R.id.nav_my_work) {
                openAcceptedWork();
            } else if (id == R.id.nav_submissions) {
                openSubmissions();
            } else if (id == R.id.nav_earnings) {
                openEarnings();
            } else if (id == R.id.nav_notifications) {
                openNotifications();
            } else if (id == R.id.nav_logout) {
                logoutUser();
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

    private void setupBottomNavByRole() {
        if (bottomNavigationView == null) {
            return;
        }

        bottomNavigationView.getMenu().clear();
        if ("writer".equals(role)) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_writer);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (isLogoutInProgress) {
                    return false;
                }
                int id = item.getItemId();
                if (id == R.id.nav_bottom_writer_home) {
                    replaceRootFragment(new WriterDashboardFragment());
                } else if (id == R.id.nav_bottom_writer_available) {
                    replaceRootFragment(new BrowseAssignmentsFragment());
                } else if (id == R.id.nav_bottom_writer_work) {
                    replaceRootFragment(new MyAcceptedWorkFragment());
                } else if (id == R.id.nav_bottom_writer_notifications) {
                    replaceRootFragment(new NotificationsFragment());
                }
                return true;
            });
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_student);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (isLogoutInProgress) {
                    return false;
                }
                int id = item.getItemId();
                if (id == R.id.nav_bottom_student_home) {
                    replaceRootFragment(new StudentDashboardFragment());
                } else if (id == R.id.nav_bottom_student_post) {
                    replaceRootFragment(new PostNewAssignmentFragment());
                } else if (id == R.id.nav_bottom_student_assignments) {
                    replaceRootFragment(new MyAssignmentsFragment());
                } else if (id == R.id.nav_bottom_student_notifications) {
                    replaceRootFragment(new NotificationsFragment());
                }
                return true;
            });
        }
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
    public void openMyAssignmentsWithFilter(String status) {
        MyAssignmentsFragment fragment = new MyAssignmentsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("initialStatusFilter", status);
        fragment.setArguments(bundle);
        openFragment(fragment);
    }

    // Kept for drawer menu navigation.
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
    public void openAcceptedWorkWithFilter(String status) {
        MyAcceptedWorkFragment fragment = new MyAcceptedWorkFragment();
        Bundle bundle = new Bundle();
        bundle.putString("initialStatusFilter", status);
        fragment.setArguments(bundle);
        openFragment(fragment);
    }

    @Override
    public void openProfile() {
        openFragment(new WriterProfileFragment());
    }

    @Override
    public void openSubmissions() {
        openFragment(new WriterSubmissionsFragment());
    }

    @Override
    public void openEarnings() {
        openFragment(new WriterEarningsFragment());
    }

    private void logoutUser() {
        if (isLogoutInProgress) {
            return;
        }

        setLogoutLoading(true);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLogoutLoading(boolean isLoading) {
        isLogoutInProgress = isLoading;
        if (logoutLoadingOverlay != null) {
            logoutLoadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (navigationView != null) {
            navigationView.setEnabled(!isLoading);
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setEnabled(!isLoading);
        }
    }

    @Override
    public void openDrawerOrMenu() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private String normalizeRole(String rawRole) {
        String normalized = rawRole == null ? "" : rawRole.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("writ")) {
            return "writer";
        }
        if (normalized.contains("stud")) {
            return "student";
        }
        return "student";
    }
}