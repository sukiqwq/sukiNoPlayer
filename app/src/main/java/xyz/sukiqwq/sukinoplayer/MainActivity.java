package xyz.sukiqwq.sukinoplayer;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.aware.DiscoverySession;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public String musiclibDir = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath()// 得到外部存储卡的数据库的路径名
            + "/Music";
    Context mainActivity = this;
    MusicListUtils musicListUtils = new MusicListUtils(this);
    boolean btn_start_state = true;
    int btn_playsetting_state = 0;
    private ImageButton btn_playsetting,btn_pre,btn_start,btn_pause,btn_next,btn_list;//创建ImageButton的对象
    static private TextView timeCurr,timeTotal;
    private ListView musicListView;
    private Intent PlayerIntent = new Intent();                                      //创建意图对象
    PlayerClickListener OnPlayerClickListener = new PlayerClickListener(); //创建监听器
    MusicInterface musicInter;                                          //创建一个接口，这个接口用于操作播放器
    ArrayAdapter<String> adapter;
    static SeekBar musicSeekbar_MainActiviity;
    ServiceConnection conn = new ServiceConnection()                    //创建一个服务连接接口对象
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicInter = (MusicInterface)iBinder;                      //实现服务连接接口，将用于操作播放器接口与Ibinder连接
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                       //显示主界面

        InitButton();                                                  //初始化按键
        PlayerIntent.setClass(this,MusicPlayerService.class);
        bindService(PlayerIntent,conn,BIND_AUTO_CREATE);
            MusicListView_Init();
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(btn_start_state){
                    musicInter.startMusic();
                    btn_start.setImageDrawable(getDrawable(R.drawable.btn_stop));
                    btn_start_state = false;
                }
                MusicListUtils.index = i;
                Log.i("onItemClick", "onItemClick: index="+MusicListUtils.index);
                musicInter.ListChooseMusic();
            }
        });
        MusicSeekbar_MainActiviity_Init();
    }
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");
            musicSeekbar_MainActiviity.setMax(duration);
            musicSeekbar_MainActiviity.setProgress(currentPosition);
            timeCurr.setText(timeParse(currentPosition));
            timeTotal.setText(timeParse(duration));
        }
    };
    protected void CreateFileDir(){
         File file = new File(musiclibDir);
         if(!file.exists()){
             while(!file.mkdirs());
         }
    }
    protected void InitButton(){
                btn_playsetting = findViewById(R.id.play_setting);
                btn_start = findViewById(R.id.start);
                /*btn_pause = findViewById(R.id.pause);*/
                btn_next = findViewById(R.id.next);
                btn_pre = findViewById(R.id.previous);
                btn_list = findViewById(R.id.list);
                timeCurr = findViewById(R.id.time_process);
                timeTotal= findViewById(R.id.time_total);

                btn_playsetting.setOnClickListener(OnPlayerClickListener);
                btn_start.setOnClickListener(OnPlayerClickListener);
               /* btn_pause.setOnClickListener(OnPlayerClickListener);*/
                btn_next.setOnClickListener(OnPlayerClickListener);
                btn_pre.setOnClickListener(OnPlayerClickListener);
                btn_list.setOnClickListener(OnPlayerClickListener);

    }
    public void MusicListView_Init(){
        CreateFileDir();
        musicListUtils.writeXML();
        musicListUtils.readXML();
        adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1, musicListUtils.songList);
        musicListView = (ListView)findViewById(R.id.music_listView);
        musicListView.setAdapter(adapter);

    }
    class PlayerClickListener implements View.OnClickListener{
                @Override
                public void onClick(View view) {
                    switch(view.getId()){
                        case R.id.play_setting:
                            {
                            if(btn_playsetting_state == 0){
                                btn_playsetting.setImageDrawable(getDrawable(R.drawable.btn_listplay));
                                btn_playsetting_state = 1;
                                MusicPlayerService.mode = 1;
                                //Log.e("tag", ""+btn_playsetting_state);
                            }
                            else if(btn_playsetting_state == 1){
                                btn_playsetting.setImageDrawable(getDrawable(R.drawable.btn_randomplay));
                                btn_playsetting_state = 2;
                                MusicPlayerService.mode = 2;
                               // Log.e("tag", ""+btn_playsetting_state);
                            }
                            else if(btn_playsetting_state == 2){
                                btn_playsetting.setImageDrawable(getDrawable(R.drawable.btn_cycle));
                                btn_playsetting_state=0;
                                MusicPlayerService.mode = 3;
                                //Log.e("tag", ""+btn_playsetting_state);
                            }
                        }
                            break;
                        case R.id.start:

                            Log.i("tag1", "onClick: start");
                            if(btn_start_state){
                                musicInter.startMusic();
                                btn_start.setImageDrawable(getDrawable(R.drawable.btn_stop));
                                btn_start_state = false;
                            }
                            else{
                                musicInter.pauseMusic();
                                btn_start.setImageDrawable(getDrawable(R.drawable.btn_start));
                                btn_start_state = true;
                            }
                            break;
                case R.id.next:
                    Log.i("tag3", "onClick: next");
                    musicInter.nextMusic();
                    if(btn_start_state){
                        musicInter.startMusic();
                        btn_start.setImageDrawable(getDrawable(R.drawable.btn_stop));
                        btn_start_state = false;
                    }
                    break;
                case R.id.previous:
                    Log.i("tag4", "onClick: previous");
                    musicInter.previousMusic();
                    if(btn_start_state){
                        musicInter.startMusic();
                        btn_start.setImageDrawable(getDrawable(R.drawable.btn_stop));
                        btn_start_state = false;
                    }
                    break;
                case R.id.list:
                    Log.i("tag5", "onClick: List");
                        musicListUtils.writeXML();
                        musicListUtils.readXML();
                        musicListView.setAdapter(adapter);
                    break;
            }
        }
    }

    protected void MusicSeekbar_MainActiviity_Init(){
        musicSeekbar_MainActiviity = findViewById(R.id.music_seekbar);

        musicSeekbar_MainActiviity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(musicInter != null){
                    if(btn_start_state){
                        btn_start.setImageDrawable(getDrawable(R.drawable.btn_stop));
                        btn_start_state = false;
                    }
                    musicInter.SetProcessTo(musicSeekbar_MainActiviity.getProgress());
                    musicInter.startMusic();
                }
            }
        });
    }
    /*
    ms@mm:ss
     */
    public static String timeParse(long duration) {
        String time = "" ;
        long minute = duration / 60000 ;
        long seconds = duration % 60000 ;
        long second = Math.round((float)seconds/1000) ;
        if( minute < 10 ){
            time += "0" ;
        }
        time += minute+":" ;
        if( second < 10 ){
            time += "0" ;
        }
        time += second ;
        return time ;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MusicListView_Init();
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }
}