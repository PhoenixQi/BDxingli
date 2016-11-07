package com.example.lenovo.bdxingli;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.renderscript.Sampler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity  {
    private TextView Tview1;
    private TextView Tview2;
    private TextView Tview3;
    private TextView Tview4;
    private TextView Tvtime;
    private EditText ET_initial_Time;// 初始设置时间
    private TextView TV_timer_couting;// 正在运行/显示的时间
    private Button btnStart;
    private Button btnPause;
    private Button btnStop;
    private double mlo;
    private double mla;
    private int mal;
    private double msd;
    private boolean flag;
    private HistogramView histogramForGPS;
    private HistogramView histogramForBD;
    static SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    private LocationManager lm;
    private static final String TAG = "BDActivity";

    private long timer_unit = 1000; //1000毫秒
    private long distination_total;// = timer_unit * 10;//10" 初始设置时间
    private long timer_couting; //正在运行/显示的时间
    private int timerStatus = CountDownTimerUtil.PREPARE;
    private Timer timer;
    private TimerTask timerTask;
    private Button Record;
    private Button Stop;
    private Handler handler;
    private Handler Lhandler;
    private String time;
    private String date;
    private String s;
    private ListView list1;
    private boolean FlagColor;
    private boolean FlagR;
    private ArrayList<String> mArrayList = new ArrayList<String>();
    private String Xtime;
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            lm.removeUpdates(locationListener);

        if(handler!=null) ;
        handler.removeMessages(1);
        Log.w("Destroy","destroy");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tview1 = (TextView) this.findViewById(R.id.TextView1);//经度
        Tview2 = (TextView) this.findViewById(R.id.TextView2);//速度
        Tview3 = (TextView) this.findViewById(R.id.TextView3);//纬度
        Tview4 = (TextView) this.findViewById(R.id.TextView4);//高度
        Tvtime = (TextView) this.findViewById(R.id.TextView_time);//时间
        Record = (Button) this.findViewById(R.id.btn_record);
        Stop = (Button)this.findViewById(R.id.stop);
        s= "";
        histogramForGPS = (HistogramView) findViewById(R.id.HistogramViewForGPS);
        histogramForBD = (HistogramView) findViewById(R.id.HistogramViewForBD);
        FlagR =true;
        list1 = (ListView)findViewById(R.id.List1);
        initTimerStatus();
        new TimeThread().start();
        //mlistAdapter = new ListViewAdapter(MainActivity.this,mArrayList);
        final BaseAdapter Badapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mArrayList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout line =  new LinearLayout(MainActivity.this);
                String mb = mArrayList.get(position);
                TextView mtext = new TextView(MainActivity.this);
                mtext.setText(mb);
                mtext.setTextColor(Color.WHITE);
                mtext.setGravity(Gravity.CENTER);
                line.addView(mtext);
                return line;
            }
        };
        //mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, mArrayList);
        Lhandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1)
                    mArrayList.add(s);
                    list1.setAdapter(Badapter);
                    list1.setSelection(mArrayList.size());

            }

        };

        Record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=true;
                mrecord();
                FlagColor=true;
                if(FlagR==true)
                Toast.makeText(MainActivity.this, "开始记录",
                        Toast.LENGTH_SHORT).show();

                if(FlagColor==true)
                    Record.setBackgroundColor(Color.parseColor("#8D7A71"));
                    Record.setText("记录中....");
                FlagR = false;

            }
        });
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FlagR==false) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("是否停止记录")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            Toast.makeText(MainActivity.this, "停止记录",
                                                    Toast.LENGTH_SHORT).show();
                                            handler.sendEmptyMessage(2);
                                            FlagR = true;
                                            //list1.setVisibility(View.GONE);
                                            FlagColor = false;
                                            if (FlagColor == false)
                                                Record.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                            FlagColor = true;
                                            Record.setText("开始记录");
                                        }
                                    }
                            )
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        //为获取地理位置信息时设置查询条件
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        //获取位置信息
        //如果不设置查询要求，getLastKnownLocation方法传入的参数为LocationManager.GPS_PROVIDER
        try {
            Location location = lm.getLastKnownLocation(bestProvider);//从GPS获取最近的定位信息
            updateView(location);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        //监听状态
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            lm.addGpsStatusListener(listener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);//1秒更新一次，或最小位移变化超过1米更新一次

    }
    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;  //消息(一个整型值)
                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }
    //在主线程里面处理消息并更新UI界面
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("yyyy-MM-dd HH:mm:ss", sysTime);
                    Tvtime.setText("北斗时间:"+sysTimeStr); //更新时间
                    break;
                default:
                    break;

            }
        }
    };
    DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener()//退出按钮
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            // 创建退出对话框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 设置对话框标题
            isExit.setTitle("系统提示");
            // 设置对话框消息
            isExit.setMessage("确定要退出吗");
            // 添加选择按钮并注册监听
            isExit.setButton("确定", listener1);
            isExit.setButton2("取消", listener1);
            // 显示对话框
            isExit.show();

        }

        return false;

    }
    private void initTimerStatus() {
        timerStatus = CountDownTimerUtil.PREPARE;
        timer_couting = 0;
    }

    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    updateHistogram();
                    break;
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
        }

    };
    private void mrecord(){
        if(flag==true) {
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if(msg.what==1)
                            mRecord(mlo, mla, mal, msd,time);
                        if(msg.what==2)
                            flag=false;
                        if(msg.what==3)
                            mArrayList.add(s);
                    }
                };
            handler.sendEmptyMessageDelayed(1, 1000);
            Lhandler.sendEmptyMessageDelayed(1,1000);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.w("STOP","stop");
    }
    @Override
    public  void onPause(){
        super.onPause();
        Log.w("PAUSE","pause");
    }
    /**
     * 更新信号柱状图
     */
    private void updateHistogram() {
        //获取当前状态
        GpsStatus gpsStatus = lm.getGpsStatus(null);
        //获取卫星颗数的默认最大值
        int maxSatellites = gpsStatus.getMaxSatellites();
        //创建一个迭代器保存所有卫星
        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
        int count = 0;
        LinkedHashMap<Integer, Integer> GPSdata = new LinkedHashMap<Integer, Integer>();
//                    HashMap GPSdata = null;//GPS的<卫星编号,信号强度>
        LinkedHashMap<Integer, Integer> BDdata = new LinkedHashMap<Integer, Integer>();
//                    HashMap BDdata = null;//BD的<卫星编号,信号强度>
        while (iters.hasNext() && count <= maxSatellites) {
            GpsSatellite s = iters.next();
            int number = s.getPrn(); //卫星的伪随机噪声码，整形数据，即卫星编号
            int str = (int) s.getSnr(); //卫星的信噪比，浮点型数据，即信号强度
            //Log.w("signal", "第" + number + "卫星的信噪比为" + str);
            if (number <= 32) {
                GPSdata.put(number, str);
            } else {
                if (number > 100)
                    number -= 100;
                BDdata.put(number, str);
            }
            count++;
        }
        if (GPSdata != null) {
            histogramForGPS.upDataTextForXY(GPSdata);
        }
        if (BDdata != null) {
            histogramForBD.upDataTextForXY(BDdata);
        }
        //Log.w("signal", "搜索到：" + count + "颗卫星");
    }

    private LocationListener locationListener = new LocationListener() {

        /**
         位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            updateView(location);
            long unix = location.getTime();
            Date time = new Date(unix);
            Log.i(TAG, "时间：" + format.format(time));
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());

        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");

                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            try {
                Location location = lm.getLastKnownLocation(provider);
                updateView(location);
//                updateHistogram();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
//            gpsLinearLayout.removeView(histogramForGPS);
//            bdLinearLayout.removeView(histogramForBD);
            updateView(null);
        }

    };
    private void mRecord(double lo,double la,int al,double sd,String time){

        s = time+" "+" " +al+" "+" "+lo+" "+" "+la+" "+ " "+sd;
        String path = Environment.getExternalStorageDirectory().toString();
        String name = "/LocationMessage";
        String mss = path+name+".txt";
        try
        {

            FileOutputStream outStream = new FileOutputStream(new File(mss),true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream,"gb2312");
            writer.write(s);
            writer.write("\r\n");
            writer.flush();
            if(flag==false) {
                writer.close();//记得关闭
                outStream.close();
            }
            else {
                mrecord();
            }
        }
        catch (Exception e)
        {
            Log.e("m", "file write error");
        }
    }
    private void updateView(Location location) {
        if (location != null) {
            String longitude = turnlocation(location.getLongitude());//经度
            String latitude = turnlocation(location.getLatitude());//维度
            double lo = location.getLongitude();
            double la = location.getLatitude();
            long  ltime = location.getTime();
            String stime = "";
            double sd = 0.0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            stime = sdf.format(new Date(ltime));
            time  = stime;
            int altitude = (int)location.getAltitude();
            Tview1.setText("经度：" + longitude);
            Tview3.setText("纬度：" + latitude);
            if (location.hasAltitude()) {
                Tview4.setText("高度：" + altitude + "米");
            }
            if (location.hasSpeed()) {
                float speed = location.getSpeed();
                double sudu = speed * 3.6;// 把米/秒 转换成 千米/时
                DecimalFormat df = new DecimalFormat("#.00");//定义为两个小数位数
                sd = Double.parseDouble(df.format(sudu));
                Tview2.setText("速度：" + sd + "千米/时");
            }
            printTime();
            mlo = lo;
            mla = la;
            mal = altitude;
            msd = sd;

        } else {
            //清空TextView对象
            Tview1.setText("经度：0");
            Tview2.setText("速度：0");
            Tview3.setText("纬度：0");
            Tview4.setText("高度：0");
        }
    }
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否要求速度信息
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        //设置是否需要方位信息
        criteria.setBearingRequired(true);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    /**
     * 把GPS的经纬度转换成标准的度分秒单位
     *
     * @param double1 GPS的经纬度
     * @return 标准的度分秒单位
     */

    private static String turnlocation(double double1) {
        double i = double1;//比如获得的数字为：double1=36.12432314
        int d = (int) i;//度   其整数即是度数,“36”
        String d1 = String.valueOf(d);
        double d2 = (double) d;
        double min = i - d2;
        int min1 = (int) (min * 60);//分  其分是"0.12432314×60=7.4593884"的整数"7",
        String m1 = String.valueOf(min1);
        double min2 = min * 60;
        double s = min2 - min1;
        double ss = s * 60;//秒   其秒是"0.4593884×60=27.5639304","27.6"
        DecimalFormat df = new DecimalFormat("#.00");//定义为两个小数位数
        String ss1 = df.format(ss);
        String last = d1 + "°" + m1 + "′" + ss1 + "″";
        return last;
    }

    private void printTime() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);//获取年份
        int month = ca.get(Calendar.MONTH);//获取月份
        int day = ca.get(Calendar.DATE);//获取日
        int minute = ca.get(Calendar.MINUTE);//分
        int hour = ca.get(Calendar.HOUR_OF_DAY);//小时
        int second = ca.get(Calendar.SECOND);//秒、
        //date = year + " " + month + " " + day + " ";
        //time = " " + hour + ":" + minute + ":" + second ;
        //Tvtime.setText("北斗时间：" + year + "年" + month + "月" + day + "日" + hour + "时" + minute + "分" + second + "秒");
    }

}
