package com.agribird.hrmsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.agribird.hrmsapp.ui.Attendance.AttendanceFragment;
import com.agribird.hrmsapp.ui.Attendance.TodayPresentFragment;
import com.agribird.hrmsapp.ui.Profile.ProfileFragment;
import com.agribird.hrmsapp.ui.dashboard.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabView;
    private View bottomBarContainer;

    private Fragment activeFragment;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_Navigation_Bar);
        bottomBarContainer=findViewById(R.id.bottomBarContainer);
        fabView = findViewById(R.id.fab_view);

        SharedPreferences sp = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
        userRole = sp.getString("userRole", "EMPLOYEE");
        setupBottomNavigationMenu(userRole);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            Fragment selectedFragment = getFragmentForItem(itemId);

            if (selectedFragment != null) {

                loadFragment(selectedFragment);

                return true;
            }
            return false;
        });


        bottomNavigation.setOnItemReselectedListener(item -> {

            int itemId = item.getItemId();

            Fragment selectedFragment = getFragmentForItem(itemId);

            if (selectedFragment != null) {

                loadFragment(selectedFragment);
            }
        });

        fabView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                boolean isAdmin = isAdminRole(userRole);


                if (isAdmin) {

                    loadFragment(new TodayPresentFragment()
                    );

                } else {

                    loadFragment(new AttendanceFragment()
                    );
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

                    if (currentFragment != null) {
                        if (shouldShowBottomNavigation(currentFragment)) {

                            showBottomNavigation(true);

                        } else {

                            showBottomNavigation(false);
                        }
                    }
                });

        if (savedInstanceState == null) {

            boolean isAdmin = isAdminRole(userRole);

            if (isAdmin) {

                bottomNavigation.setSelectedItemId(R.id.nav_home);

            } else {

                bottomNavigation.setSelectedItemId(R.id.home);
            }
        }
    }

    private boolean isAdminRole(String role) {

        if (role == null) {
            return false;
        }

        return "SUPER_ADMIN".equalsIgnoreCase(role)
                || "Super Administrator".equalsIgnoreCase(role)
                || "Administrator".equalsIgnoreCase(role)
                || "Admin staff".equalsIgnoreCase(role)
                || "Admin Manager".equalsIgnoreCase(role);
    }


    private void setupBottomNavigationMenu(
            String userRole
    ) {

        if (bottomNavigation == null) {
            return;
        }

        bottomNavigation.getMenu().clear();


        if (isAdminRole(userRole)) {

            // ADMIN MENU
            bottomNavigation.inflateMenu(R.menu.bottom_menu_admin);

        } else {

            bottomNavigation.inflateMenu(R.menu.bottom_nav_menu);
        }
    }


    private Fragment getFragmentForItem(
            int itemId
    ) {
        if (itemId == R.id.home) {
            return new DashboardFragment();
        }

        if (itemId == R.id.nav_home) {

            return new DashboardFragment();
        }

        if (itemId == R.id.nav_profile) {

            return new ProfileFragment();
        }

        return null;
    }

    private boolean shouldShowBottomNavigation(
            Fragment fragment
    ) {

        if (fragment instanceof DashboardFragment) {

            return true;
        }

        if (fragment instanceof ProfileFragment) {

            return true;
        }

        if (!isAdminRole(userRole)
                && fragment instanceof AttendanceFragment) {

            return true;
        }

        if (isAdminRole(userRole)
                && fragment instanceof TodayPresentFragment) {

            return true;
        }

        return false;
    }


    public void showBottomNavigation(boolean show) {

        if (show) {
            bottomBarContainer.setVisibility(View.VISIBLE);

            fabView.setVisibility(View.VISIBLE);

        } else {

            bottomBarContainer.setVisibility(View.GONE);

            fabView.setVisibility(View.GONE);
        }
    }

    private void loadFragment(Fragment fragment) {
        activeFragment = fragment;

        if (shouldShowBottomNavigation(fragment)) {

            showBottomNavigation(true);

        } else {

            showBottomNavigation(false);
        }

        getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
//package com.agribird.hrmsapp;
//
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.ui.Attendance.AttendanceFragment;
//import com.agribird.hrmsapp.ui.Attendance.TodayPresentFragment;
//import com.agribird.hrmsapp.ui.Profile.ProfileFragment;
//import com.agribird.hrmsapp.ui.dashboard.DashboardFragment;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//public class MainActivity extends AppCompatActivity {
//    private BottomNavigationView bottomNavigation;
//    private FloatingActionButton fabView;
//
//    private Fragment activeFragment;
//
//    private String userRole;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//
//
//        bottomNavigation = findViewById(R.id.bottom_Navigation_Bar);
//        fabView = findViewById(R.id.fab_view);
//
//        SharedPreferences sp = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
//        userRole = sp.getString("userRole", "EMPLOYEE");
//
//        setupBottomNavigationMenu(userRole);
//
//        bottomNavigation.setOnItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//            Fragment selectedFragment = getFragmentForItem(itemId);
//
//            if (selectedFragment != null) {
//                loadFragment(selectedFragment);
//                return true;
//            }
//            return false;
//        });
//
//        bottomNavigation.setOnItemReselectedListener(
//                item -> {
//                    int itemId = item.getItemId();
//
//                    Fragment selectedFragment = getFragmentForItem(itemId);
//
//                    if (selectedFragment != null) {
//                        loadFragment(selectedFragment);
//                    }
//                }
//        );
//
//
//        fabView.setOnClickListener(new View.OnClickListener() {
//                                       @Override
//                                       public void onClick(View v) {
//
//                                           boolean isAdmin =
//                                                   "Super Administrator".equals(userRole)
//                                                           || "Administrator".equals(userRole)
//                                                           || "Admin staff".equals(userRole)
//                                                           || "Admin Manager".equals(userRole);
//
//
//                                           if (isAdmin) {
//                                               loadFragment(new TodayPresentFragment());
//                                           }
//                                           else {
//                                               loadFragment(new AttendanceFragment()
//                                               );
//                                           }
//                                       }
//                                   }
//        );
//
//        if (savedInstanceState == null) {
//            boolean isAdmin = isAdminRole(userRole);
//            if (isAdmin) {
//                bottomNavigation.setSelectedItemId(R.id.nav_home);
//
//            } else {
//                bottomNavigation.setSelectedItemId(R.id.home);
//            }
//        }
//    }
//
//    private boolean isAdminRole(
//            String role
//    ) {
//
//        return "Super Administrator".equals(role)
//                || "Administrator".equals(role)
//                || "Admin staff".equals(role)
//                || "Admin Manager".equals(role);
//    }
//
//    private void setupBottomNavigationMenu(
//            String userRole
//    ) {
//        if (bottomNavigation == null) {
//            return;
//        }
//        bottomNavigation.getMenu().clear();
//        if (isAdminRole(userRole)) {
//            bottomNavigation.inflateMenu(R.menu.bottom_menu_admin
//            );
//        } else {
//            bottomNavigation.inflateMenu(R.menu.bottom_nav_menu
//            );
//        }
//    }
//    private Fragment getFragmentForItem(
//            int itemId
//    ) {
//
//        if (itemId == R.id.home) {
//            return new DashboardFragment();
//        }
//        if (itemId == R.id.nav_home) {
//            return new DashboardFragment();
//        }
//
//        if (itemId == R.id.nav_profile) {
//            return new ProfileFragment();
//        }
//        return null;
//    }
//
//    private void loadFragment(Fragment fragment) {
//
//        activeFragment = fragment;
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//                .replace(R.id.fragmentContainer, fragment)
//                .commit();
//    }
//}
//package com.agribird.hrmsapp;
//
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.ui.Attendance.AttendanceFragment;
//import com.agribird.hrmsapp.ui.Attendance.TodayPresentFragment;
//import com.agribird.hrmsapp.ui.Profile.ProfileFragment;
//import com.agribird.hrmsapp.ui.dashboard.DashboardFragment;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//
//public class MainActivity extends AppCompatActivity {
//
//    private BottomNavigationView bottomNavigationView;
//    private Fragment activeFragment;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//
//        SharedPreferences sp = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
//
//        String userRole = sp.getString("userRole", "EMPLOYEE");
//
//        setupBottomNavigationMenu(userRole);
//
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//
//            int itemId = item.getItemId();
//
//            Fragment selectedFragment = null;
//
//            if (itemId == R.id.home || itemId == R.id.nav_home) {
//                selectedFragment = new DashboardFragment();
//            }
//            else if (itemId == R.id.nav_attendance) {
//                selectedFragment = new AttendanceFragment();
//            }
//            else if (itemId == R.id.nav_present) {
//                selectedFragment = new TodayPresentFragment();
//            }
//            else if (itemId == R.id.nav_profile) {
//                selectedFragment = new ProfileFragment();
//            }
//            if (selectedFragment != null) {
//                loadFragment(selectedFragment);
//                return true;
//            }
//            return false;
//        });
//
//        bottomNavigationView.setOnItemReselectedListener(item -> {
//        });
//
//        if (savedInstanceState == null) {
//
//            loadFragment(new DashboardFragment());
//
//        }
//    }
//
//    private void setupBottomNavigationMenu(String userRole) {
//
//        if (bottomNavigationView == null) {
//            return;
//        }
//        bottomNavigationView.getMenu().clear();
//
//        boolean isAdmin =
//                "Super Administrator".equals(userRole)
//                        || "Administrator".equals(userRole)
//                        || "Admin staff".equals(userRole)
//                        || "Admin Manager".equals(userRole);
//
//
//        if (isAdmin) {
//            bottomNavigationView.inflateMenu(
//                    R.menu.bottom_menu_admin
//            );
//
//        } else {
//
//            bottomNavigationView.inflateMenu(
//                    R.menu.bottom_nav_menu
//            );
//        }
//    }
//
//    private void loadFragment(Fragment fragment) {
//
//        if (activeFragment != null
//                && activeFragment.getClass().equals(fragment.getClass())) {
//
//            return;
//        }
//
//        activeFragment = fragment;
//
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(
//                        android.R.anim.fade_in,
//                        android.R.anim.fade_out
//                )
//                .replace(
//                        R.id.frameLayout,
//                        fragment
//                )
//                .commit();
//    }
//}