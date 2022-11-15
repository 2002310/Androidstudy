package com.example.android_study.common.update;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Intent;

import android.net.Uri;

import android.os.Environment;
import android.os.IBinder;

import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.example.android_study.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;


public class UpdateService extends Service {
    //    成功和失败的返回码
    public static final Integer SUCCESS = 0x10000;
    public static final Integer FAILED = 0x10001;
    //    软件下载链接
    private static String url = "https://2077c844f4695d651cc0e04b96185f7c.rdt.tfogc.com:49156/dldir1.qq.com/weixin/android/weixin8030android2260_arm64.apk?mkey=6371c668beb5da42f221b7fc132b742a&arrive_key=257598248064&cip=218.204.114.205&proto=https";
    //    软件的最大值和当前已经下载的值
    private int maxSize = 100;
    private int currentSize = 0;
    //    通知管理器
    private NotificationManagerCompat nManager = null;
    //    通知
    private NotificationCompat.Builder build = null;
    //    文件名
    private String s1 = "";
    //    具体的文件路径
    private String filepath = "";
    //    进度
    private long progress = 0;


    public UpdateService() {
    }

    //注册EventBus
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    //注销EventBus
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
//        开启线程进行网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileLoad(url);
                } catch (IOException e) {
//                    失败发送失败消息
                    EventBus.getDefault().post(FAILED);
                    e.printStackTrace();
                }
            }
        }).start();
//        创建通知
        createNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    //    创建通知
    private void createNotification() {
        nManager = NotificationManagerCompat.from(this);
        NotificationChannel chanel = new NotificationChannel("CHANNEL_ID", "CHANEL_NAME", NotificationManager.IMPORTANCE_MIN);
        nManager.createNotificationChannel(chanel);
        build = new NotificationCompat.Builder(this, "CHANNEL_ID");
//        设置标题
        build.setContentTitle("软件下载")
//                设置内容
                .setContentText("0%")
//                设置小图标
                .setSmallIcon(R.drawable.ic_baseline_arrow_drop_down_circle_24);
//        设置进度条
        build.setProgress(maxSize, currentSize, false);
//        通过NotificationManagerCompat发送通知并赋予id
        nManager.notify(1, build.build());
    }

    //    安装程序
    public Intent StartInstall() {
        Intent install = new Intent(Intent.ACTION_VIEW);
//        设置FLAG_ACTIVITY_NEW_TASK,确保软件安装后返回该页面
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        临时访问读权限 设置FLAG_GRANT_READ_URI_PERMISSION,intent的接受者将被授予 INTENT数据uri或者在ClipData上的读权限。
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//       安卓官方更推荐使用 FileProvider.getUriForFile来获取文件的uri
//       要使用FileProvider需要在AndroidManifest文件中申明，authority参数需要与AndroidManifest文件声明的provider标签一致
        Uri uri = FileProvider.getUriForFile(this, "com.example.android_study.fileprovider", new File(filepath));
//       install取设置属于和类型，数据就是获取到的uri，更具文件类型不同，type参数也不相同，具体参考下表
            /*{后缀名，MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",    "image/bmp"},
            {".c",  "text/plain"},
            {".class",  "application/octet-stream"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",   "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h",  "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".mpga",   "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop",   "text/plain"},
            {".rc", "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh", "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".z",  "application/x-compress"},
            {".zip",    "application/x-zip-compressed"},*/
//            不知道什么类型也可以用   {"","*/*"}
        install.setDataAndType(uri, "application/vnd.android.package-archive");
//        将设置好的intent返回
        return install;
    }

    //    创建EventBus监听
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void UpdateEventBus(Integer message) {
//      转换百分比，不先除再乘的原因是int小于1会转换0
        int a = currentSize * 100;
        int b = a / maxSize;
//      设置notification的内容，每次通过EventBus监听到数据时都去改变百分比
        build.setContentText("下载中：" + b + "%");
//      设置进度条，进度条无需改变参数
        build.setProgress(maxSize, currentSize, false);
//      发送通知
        nManager.notify(1, build.build());
//      当文件下载完成后
        if (message == SUCCESS) {
            Intent install = StartInstall();
            PendingIntent intent = PendingIntent.getActivity(this, 0, install, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                    .setSmallIcon(R.drawable.ic_baseline_arrow_drop_down_circle_24)
                    .setContentTitle("下载完成")
                    .setContentText("点击安装")
                    .setContentIntent(intent);
            nManager.cancel(1);
            nManager.notify(2, builder.build());
        }
        if (message == FAILED) {
            Toast.makeText(this, "软件更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    //文件下载方法
    private void fileLoad(String url) throws IOException {
//        创建文件输出流
        FileOutputStream fileOutputStream = null;
//        okHttp发起网络请求并返回结果
        OkHttpClient ok = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = ok.newCall(request);
//        获取到返回体
        ResponseBody body = call.execute().body();
//        断言，如果返回题不为空继续执行
        assert body != null;
//        获取到文件总大小
        long l = body.contentLength();
//        将返回提转换成流
        InputStream inputStream = body.byteStream();
//        通过transitionBit方法把b转换为mb设置为最大值
        maxSize = transitionBit(l);

//      创建文件路径目录，通过Environment.getExternalStorageDirectory()获取到手机文件的根目录
        File dir = new File(Environment.getExternalStorageDirectory() + "/Download");
        if (!dir.exists()) {
            dir.mkdirs();
        }
//        从url中获取文件名方法StringReverse
        String s = StringReverse(url);
        int i = s.indexOf("?");
        i += 1;
        int i1 = s.indexOf("/");
        String substring = s.substring(i, i1);
        //        从url中获取文件名方法StringReverse
        s1 = StringReverse(substring);
//        创建文件
        File file = new File(dir, s1);
        if (!file.exists()) {
            dir.createNewFile();
        }
//        文件写入操作
        byte[] buf = new byte[1024];
        int len = 0;
        int a = 1024 * 1024;
        fileOutputStream = new FileOutputStream(file);
        while ((len = inputStream.read(buf)) != -1) {
            fileOutputStream.write(buf, 0, len);
            progress += len;
            if (progress % a == 0) {
                currentSize = transitionBit(progress);
//                每下载成功1mb通知一次，保证通知不频繁
                EventBus.getDefault().post(currentSize);
            }
        }
//      开了就要关
        inputStream.close();
        fileOutputStream.close();

        filepath = Environment.getExternalStorageDirectory() + "/Download/" + s1;
        EventBus.getDefault().post(SUCCESS);
    }

    //    b转mb
    private Integer transitionBit(long b) {
        long l = b / 1024;
        long l1 = l / 1024;
        return Math.toIntExact(l1);
    }

    //    字符串反转
    private String StringReverse(String str) {
        StringBuffer stringBuffer = new StringBuffer(str);
        String s = stringBuffer.reverse().toString();
        return s;
    }

}
