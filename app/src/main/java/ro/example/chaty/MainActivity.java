package ro.example.chaty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import ro.example.chaty.fragments.ChatsFragment;
import ro.example.chaty.fragments.ProfileFragment;
import ro.example.chaty.fragments.UsersFragment;
import ro.example.chaty.model.User;

public class MainActivity extends AppCompatActivity implements NetworkListener{

    CircleImageView profileImage;
    TextView username;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    private WifiManager wifiManager;
    private NetworkChangeReceiver receiver;
    private IntentFilter filter;
    private TextView connectivityTextView;
    private boolean wasOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user1 = snapshot.getValue(User.class);
                assert user1 != null;
                username.setText(user1.getUsername());
                if(user1.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.user);
                } else {
                    Glide.with(getApplicationContext()).load(user1.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), ViewPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
        viewPagerAdapter.addFragment(new UsersFragment(), "Users");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);



        connectivityTextView = findViewById(R.id.connectivity_text_view);

        initNetworkManager();
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }


    @Override
    protected void onStart() {
        super.onStart();
        initNetworkManager();
        registerReceiver(receiver, filter);
        status("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void networkChange() {
        if (receiver.isOnline(this)){
            connectivityTextView.setVisibility(View.GONE);
            if (wasOffline) {
                Toast.makeText(this, "Back online", Toast.LENGTH_LONG).show();
                Log.v("Br", "back online");
                wasOffline = false;
            }
        } else {
            wasOffline = true;
            connectivityTextView.setVisibility(View.VISIBLE);
        }
    }


    private void initNetworkManager(){
        receiver = new NetworkChangeReceiver();
        receiver.setupListener(this);
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wasOffline = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                status("offline");
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //finish();
                return true;
        }
        return false;
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user !=null){
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("status", status);

            databaseReference.updateChildren(hashMap);
        }

    }


}