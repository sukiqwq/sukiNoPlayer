package xyz.sukiqwq.sukinoplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

public class MusicListUtils {
    static List<String> songList = new ArrayList<>();
    static String path;
    static int index = 0;
    MainActivity mainActivity;

    public MusicListUtils(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void readXML() {
        try {
            XmlPullParser parser = Xml.newPullParser();
            File file = new File(mainActivity.getCacheDir() + "/musicList.xml");
            FileInputStream fis = new FileInputStream(file);
            int tag = parser.getEventType();
            parser.setInput(fis, "utf-8");
            songList.clear();
            while (tag != XmlPullParser.END_DOCUMENT) {
                switch (tag) {
                    case XmlPullParser.START_TAG:
                        String s = parser.getName();
                        if (s.equals("path")) {
                            path = parser.getAttributeValue(null, "value");
                            Log.i("TAG", parser.getAttributeValue(null, "value"));
                        }
                        else if(s.equals("MusicInfo")) {
                            String musicInfo = parser.nextText();
                            songList.add(musicInfo);
                            Log.i("TAG", musicInfo);
                        }
                }
                tag = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeXML() {
        try {
            XmlSerializer serializer = Xml.newSerializer();
            File file = new File(mainActivity.getCacheDir() + "/musicList.xml");
            Log.i("TAG", mainActivity.getCacheDir().toString());
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            serializer.setOutput(fos, "utf-8");
            serializer.startDocument("utf-8" ,true);
            serializer.startTag(null, "path");
            serializer.attribute(null, "value", mainActivity.musiclibDir);
            String[] fileList = new File(mainActivity.musiclibDir).list();
            for (String s : fileList) {
                if(s.endsWith(".mp3")) {
                    serializer.startTag(null, "MusicInfo");
                    serializer.text(s);
                    serializer.endTag(null, "MusicInfo");
                    Log.e("TAG", s);
                }
            }
            serializer.endTag(null, "path");
            serializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





}
