package seoul.iot.biketrainner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.tsengvn.typekit.TypekitContextWrapper;

/**
 * Created by AliceLim on 2017. 11. 21..
 */

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button mBtnMenu;
    private DrawerLayout mNavDrawerLayout;
    private Button mapNaviCloseBtn;
    private Button goTrainBtn;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mBtnMenu = (Button)findViewById(R.id.map_navi);
        mNavDrawerLayout = (DrawerLayout)findViewById(R.id.map_drawer_layout);
        mapNaviCloseBtn = (Button)findViewById(R.id.map_navi_close);
        goTrainBtn = (Button)findViewById(R.id.map_go_train);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.map_navi_view);
        navigationView.setNavigationItemSelectedListener(this);

        //mNavDrawerLayout.openDrawer(Gravity.LEFT);

        mBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mapNaviCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavDrawerLayout.closeDrawers();
            }
        });

        goTrainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
