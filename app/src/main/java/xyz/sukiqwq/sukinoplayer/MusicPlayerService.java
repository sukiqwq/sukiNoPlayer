package xyz.sukiqwq.sukinoplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import static java.lang.Math.random;

public class MusicPlayerService extends Service {
    MediaPlayer MusicPlayer;
    static int mode = 3;
    /*
    * mode 1 = 顺序；
    * mode 2 = 随机；
    * mode 3 = 循环；
    * */
    class musicBinder extends Binder implements MusicInterface{
        @Override
        public void startMusic() {
            start();
        }

        @Override
        public void pauseMusic() {
            pause();
        }

        @Override
        public void nextMusic() {
            if(mode == 3){
                mode = 1;
                next();
                mode = 3;
            }
            else next();
        }

        @Override
        public void previousMusic() {
            previous();
        }
        public void ListChooseMusic(){
            listChooseMusic();
        }

        @Override
        public void SetProcessTo(int process) {
            setProcessTo(process);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        musicBinder binder = new musicBinder();
        return binder;
    }


    public void onCreate(){
        super.onCreate();
        MusicPlayer = new MediaPlayer();
        try {
            MusicPlayer.setDataSource(MusicListUtils.path + "/" +  MusicListUtils.songList.get(MusicListUtils.index));
            MusicPlayer.prepare();
           // MusicPlayer.start();
            updateSeekBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();
            }
        });
    }
    public void start() {
        if (MusicPlayer != null) {
            MusicPlayer.start();
        }
    }

    public void pause() {
        if (MusicPlayer != null) {
            MusicPlayer.pause();
        }
    }

    public void next(){
        switch(mode){
            case 1:
                if (MusicPlayer != null) {
                if (++MusicListUtils.index > (MusicListUtils.songList.size()-1)) {
                    MusicListUtils.index = 0;
                }
                change();
            }break;
            case 2:
                if(MusicPlayer != null){
                   MusicListUtils.index=(int)random()%(MusicListUtils.songList.size());
                }
                change();
                break;
            case 3:
                if (MusicPlayer != null) {
                        MusicListUtils.index = MusicListUtils.index;
                        change();
                    }
                break;
        }

    }
    public void previous() {
        if (MusicPlayer != null) {
            if (--MusicListUtils.index < 0){
                MusicListUtils.index = MusicListUtils.songList.size()-1;
            }
            change();
        }
    }
    public void change(){
        if (MusicPlayer != null) {
            try {
                MusicPlayer.reset();
                MusicPlayer.setDataSource(MusicListUtils.path + "/" + MusicListUtils.songList.get(MusicListUtils.index));
                MusicPlayer.prepare();
                MusicPlayer.start();
                //updateSeekBar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void listChooseMusic(){
        if (MusicPlayer != null) {
            if (MusicListUtils.index >= 0 && MusicListUtils.index <= MusicListUtils.songList.size()-1) {
                change();
            }
        }
    }

    /*
       musicSeekBar 用于刷新的线程
        */
    private void updateSeekBar() {
        //开启线程发送数据
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //发送数据给activity
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration",MusicPlayer.getDuration());
                    bundle.putInt("currentPosition",MusicPlayer.getCurrentPosition());
                    message.setData(bundle);
                    MainActivity.handler.sendMessage(message);
                }
            }
        }.start();
    }
    public void setProcessTo(int process){
        if(MusicPlayer != null)
            MusicPlayer.seekTo(process);
    }
}