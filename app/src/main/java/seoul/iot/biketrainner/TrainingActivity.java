package seoul.iot.biketrainner;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContentResolverCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;

import com.tsengvn.typekit.Typekit;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

public class TrainingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TrainingActivity";
    private static final int ZONE_SIZE = 6;

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Intent reqConnectIntent = new Intent(TrainingActivity.this, ReqConnectActivity.class);
                    //startActivity(reqConnectIntent);
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Intent reqConnectIntent1 = new Intent(TrainingActivity.this, ReqConnectActivity.class);
                    //startActivity(reqConnectIntent1);
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private UsbService usbService;
    private TextView heartDisplay;
    private MyHandler mHandler;
    private DrawerLayout mNavDrawerLayout;
    private Button mBtnEnd;
    private Button mBtnMenu;
    private ImageView mIvZoneCircle;
    private TextView leftZone;
    private TextView centerZone;
    private TextView rightZone;
    private TextView ment;
    private Button mapBtn;
    private ImageButton naviCloseBtn;
    private int[] ZONE_START = {50, 60, 70, 80, 90, 100};

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    // 폰트 설정을 할려는 화면은 이 코드를 복사붙여넣기 하면 된다.
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        mBtnEnd = (Button) findViewById(R.id.t_end);
        mBtnMenu = (Button) findViewById(R.id.t_menu);
        mNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        heartDisplay = (TextView) findViewById(R.id.tv_heart);
        mIvZoneCircle = (ImageView) findViewById(R.id.t_zone_circle);
        leftZone = (TextView)findViewById(R.id.t_left_zone);
        centerZone = (TextView)findViewById(R.id.t_center_zone);
        rightZone = (TextView)findViewById(R.id.t_right_zone);
        ment = (TextView)findViewById(R.id.t_ment);
        mapBtn = (Button)findViewById(R.id.t_map);
        naviCloseBtn = (ImageButton) findViewById(R.id.navi_close);

        final List<Integer> circleImages = Arrays.asList(new Integer[]{
                R.drawable.zone_1, R.drawable.zone_2,
                R.drawable.zone_3, R.drawable.zone_4,
                R.drawable.zone_5, R.drawable.zone_6});
        Button buttonScrewBar = (Button) findViewById(R.id.buttonScrewBar);
        buttonScrewBar.setTag(0);
        buttonScrewBar.setOnClickListener(new View.OnClickListener() {
            int test = 0;
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                setZone(ZONE_START[test%6]);
                test++;
                //mIvZoneCircle.setImageResource(circleImages.get((Integer) v.getTag()));
                //v.setTag((Integer) v.getTag() + 1);
                //v.setTag((Integer) v.getTag() % 6);
            }
        });

        //ZONE 시작 심박수 설정(ZONE_START[n] : n+1존 시작점)
        ZONE_START[1] = 60;
        ZONE_START[2] = 70;
        ZONE_START[3] = 80;
        ZONE_START[4] = 90;
        ZONE_START[5] = 100;

        Intent intent = getIntent();

        mHandler = new MyHandler(this);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //mNavDrawerLayout.openDrawer(Gravity.LEFT);

        mBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mBtnEnd.setOnClickListener(new View.OnClickListener() {
            TimerTask timerTask;
            Timer timer;
            boolean isRun = false;
            long count = 0;

            @Override
            public void onClick(View v) {
                if(!isRun) {
                    leftZone.setVisibility(View.VISIBLE);
                    centerZone.setVisibility(View.VISIBLE);
                    rightZone.setVisibility(View.VISIBLE);
                    ment.setVisibility(View.VISIBLE);

                    count = 0;

                    mBtnEnd.setBackgroundResource(R.drawable.pause);
                    mBtnEnd.setText("00:00:00");
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            TrainingActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    count++;
                                    mBtnEnd.setText(String.format("%02d:%02d:%02d", count / 3600, (count % 3600) / 60, (count % 60)));
                                }
                            });
                        }
                    };
                    timer = new Timer();
                    timer.schedule(timerTask, 1000, 1000);
                    isRun = true;
                }
                else {
                    leftZone.setVisibility(View.INVISIBLE);
                    centerZone.setVisibility(View.INVISIBLE);
                    rightZone.setVisibility(View.INVISIBLE);
                    ment.setVisibility(View.INVISIBLE);
                    mIvZoneCircle.setBackground(getDrawable(R.drawable.circle));

                    timerTask.cancel();
                    timer.cancel();
                    mBtnEnd.setBackgroundResource(R.drawable.end_button);
                    mBtnEnd.setText("운동 시작");
                    isRun = false;
                }
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        naviCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavDrawerLayout.closeDrawers();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mNavDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mNavDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_training) {
            Intent intent = new Intent(this, TrainingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_scoreboard) {

        } else if (id == R.id.nav_family) {

        } else if (id == R.id.nav_profile) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setZone(int heart) {
        //Zone 이미지 설정(heart 값에따라 image 변경)
        if (heart < ZONE_START[1]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_1));
            leftZone.setText("Zone6");
            centerZone.setText("Zone1");
            rightZone.setText("Zone2");
            centerZone.setTextColor(0xffd020ff);
            ment.setText("오랫 동안, 편안한 라이딩을 해주세요.\n신체가 예열 또는 회복되고 있습니다.");
        }
        else if (heart < ZONE_START[2]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_2));
            leftZone.setText("Zone1");
            centerZone.setText("Zone2");
            rightZone.setText("Zone3");
            centerZone.setTextColor(0xff38d0ff);
            ment.setText("중간 강도로 긴 라이딩을 해주세요.\n" +
                    "점진적으로 준비 작업에 돌입하고있습니다.");
        }
        else if (heart < ZONE_START[3]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_3));
            leftZone.setText("Zone2");
            centerZone.setText("Zone3");
            rightZone.setText("Zone4");
            centerZone.setTextColor(0xff29c300);
            ment.setText("고통을 수반하지만 집중하여 속도를 유지하여주세요.\n" +
                    "라이딩의 기본 페이스가 될 것 입니다.");
        }
        else if (heart < ZONE_START[4]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_4));
            leftZone.setText("Zone3");
            centerZone.setText("Zone4");
            rightZone.setText("Zone5");
            centerZone.setTextColor(0xfffff712);
            ment.setText("다리의 불타는 느낌을 유지하여주세요.\n" +
                    "한계를 넘어 페이스를 할 수 있게됩니다.");
        }
        else if (heart < ZONE_START[5]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_5));
            leftZone.setText("Zone4");
            centerZone.setText("Zone5");
            rightZone.setText("Zone6");
            centerZone.setTextColor(0xffff722c);
            ment.setText("고도의 집중이 필요합니다. 호흡이 매우 빠르겠지만\n" +
                    "다리의 근육이 연소되기 시작합니다.");
        }
        else {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_6));
            leftZone.setText("Zone5");
            centerZone.setText("Zone6");
            rightZone.setText("Zone1");
            centerZone.setTextColor(0xfffa184b);
            ment.setText("최선을 다해 달려보세요.\n" +
                    "이 구간이 지나면 심폐활량이 증가하게됩니다.");
        }
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<TrainingActivity> mActivity;
        private int curHeart;
        private String mstr;

        public MyHandler(TrainingActivity activity) {
            mActivity = new WeakReference<>(activity);
            curHeart = 1;
            mstr = "";
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    final String data = (String) msg.obj;
                    //mActivity.get().heartDisplay.setText(data);
                    new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            mActivity.get().heartDisplay.setText(mstr);
                            setZone(Integer.parseInt(mstr));
                        }
                    };
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    if (stringAppend(buffer)) {
                        try {
                            mActivity.get().heartDisplay.setText(mstr);
                            setZone(Integer.parseInt(mstr));
                            mstr = "";
                        } catch (Exception e) {

                        }
                    }
                    //setZone(Integer.parseInt(buffer));
                    break;
            }
        }

        private boolean stringAppend(String buffer) {
            char[] arrC = buffer.toCharArray();

            if (arrC[arrC.length - 1] >= '0' && arrC[arrC.length - 1] <= '9') {
                mstr += buffer;
                return false;
            } else {
                for (int i = 0; i < arrC.length && (arrC[arrC.length - 1] >= '0' && arrC[arrC.length - 1] <= '9'); i++) {
                    mstr += String.valueOf(arrC[i]);
                }
                return true;
            }
        }

        private void setZone(int heart) {
            //Zone 이미지 설정(heart 값에따라 image 변경)
            if (heart < mActivity.get().ZONE_START[1]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_1));
                mActivity.get().leftZone.setText("Zone6");
                mActivity.get().centerZone.setText("Zone1");
                mActivity.get().rightZone.setText("Zone2");
                mActivity.get().centerZone.setTextColor(0xffd020ff);
                mActivity.get().ment.setText("오랫 동안, 편안한 라이딩을 해주세요.\n신체가 예열 또는 회복되고 있습니다.");

            }
            else if (heart < mActivity.get().ZONE_START[2]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_2));
                mActivity.get().leftZone.setText("Zone1");

                mActivity.get().centerZone.setText("Zone2");
                mActivity.get().rightZone.setText("Zone3");
                mActivity.get().centerZone.setTextColor(0xff38d0ff);
                mActivity.get().ment.setText("중간 강도로 긴 라이딩을 해주세요.\n" +
                        "점진적으로 준비 작업에 돌입하고있습니다.");
            }
            else if (heart < mActivity.get().ZONE_START[3]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_3));
                mActivity.get().leftZone.setText("Zone2");
                mActivity.get().centerZone.setText("Zone3");
                mActivity.get().rightZone.setText("Zone4");
                mActivity.get().centerZone.setTextColor(0xff29c300);
                mActivity.get().ment.setText("고통을 수반하지만 집중하여 속도를 유지하여주세요.\n" +
                        "라이딩의 기본 페이스가 될 것 입니다.");
            }
            else if (heart < mActivity.get().ZONE_START[4]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_4));
                mActivity.get().leftZone.setText("Zone3");
                mActivity.get().centerZone.setText("Zone4");
                mActivity.get().rightZone.setText("Zone5");
                mActivity.get().centerZone.setTextColor(0xfffff712);
                mActivity.get().ment.setText("다리의 불타는 느낌을 유지하여주세요.\n" +
                        "한계를 넘어 페이스를 할 수 있게됩니다.");
            }
            else if (heart < mActivity.get().ZONE_START[5]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_5));
                mActivity.get().leftZone.setText("Zone4");
                mActivity.get().centerZone.setText("Zone5");
                mActivity.get().rightZone.setText("Zone6");
                mActivity.get().centerZone.setTextColor(0xffff722c);
                mActivity.get().ment.setText("고도의 집중이 필요합니다. 호흡이 매우 빠르겠지만\n" +
                        "다리의 근육이 연소되기 시작합니다.");
            }
            else {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_6));
                mActivity.get().leftZone.setText("Zone5");
                mActivity.get().centerZone.setText("Zone6");
                mActivity.get().rightZone.setText("Zone1");
                mActivity.get().centerZone.setTextColor(0xfffa184b);
                mActivity.get().ment.setText("최선을 다해 달려보세요.\n" +
                        "이 구간이 지나면 심폐활량이 증가하게됩니다.");
            }
        }
    }
}
