# Android开发之应用跟新或软件下载

### 	本文章学习前提:okHttp3或以上，EventBus或其它事件总线工具，四大组件的Activity和Service，安卓通知基础知识

## 	新建项目文件

目录结构如下:

![image-20221115172230734](C:\Users\Mr.Gao\Desktop\博客\Android开发应用跟新或软件下载.assets\image-20221115172230734.png)

### MainActivity.java

#### 获取权限

本项目所需权限

```xml
	<!--    网络权限-->
    <uses-permission android:name="android.permission.INTERNET" />
    <!--    软件安装权限-->
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    <!--    文件读写权限-->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Android **6.0以上并且targetSDKVersion>=23**时需要动态申请权限

在MainActivity.java中创建权限申请方法

```java
//    获取权限方法
    public static void getPermissionCamera(Activity activity) {
//      检查权限
        int readPermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

//        检查是否有该权限，没有才去申请
//        PackageManager.PERMISSION_GRANTED--->有
//        PackageManager.PERMISSION_DENIED---->无
        if (readPermissionCheck != PackageManager.PERMISSION_GRANTED|| writePermissionCheck != PackageManager.PERMISSION_GRANTED) {
//            将这些权限添加到数组中
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//            通过ActivityCompat.requestPermissions()方法申请权限
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
        }
    }

```

在权限中，有两种不同的大类，运行时权限和非运行时权限

运行时权限是软件正常运行时需要用到的权限如果没有会影响软件功能或报错，运行时权限的申请就需要用到以上方法。

非运行时权限是指软件运行过程中并不需要该权限是为了某些特殊的功能，如软件安装等需要申请的权限，即便没有也不影响软件的正常运行，我们需要在下载完成后点击安装跳转到软件安装页面，所以我们需要申请的软件安装权限就需要用到以下方法

```java
public void checkPermission(){
        boolean haveInstallPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if(!haveInstallPermission){
                //没有权限让调到设置页面进行开启权限；
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                startActivityForResult(intent, 10086);
            }else{

                //有权限，执行自己的逻辑；

            }
        }else{
            //其他android版本，可以直接执行安装逻辑；

        }
    }
```

跳转到设置页面让用户自行打开软件安装权限

#### 版本对比

```java
Global.LOCAL_VERSION<Global.SERVICE_VERSION
```

本地版本低于服务端版本就创建更新弹窗

#### 创建更新弹窗

当你的本地版本低于你获取到的服务端版本号，就需要弹出弹窗确保软件的及时跟新

```java
private void checkVersion() {
    if (Global.LOCAL_VERSION<Global.SERVICE_VERSION) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("软件更新通知")
                .setMessage("发现新版本，建议立即跟新")
            //设置更新和取消事件监听
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, UpdateService.class);
                        intent.putExtra("titleId", R.string.app_name);
                        startService(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialog.create().show();
    } else {


    }
```

#### 启动服务

```java
 Intent intent = new Intent(MainActivity.this, UpdateService.class);
                        intent.putExtra("titleId", R.string.app_name);
                        startService(intent);
```

这是一段启动服务的代码，需要在点击更新时启动跟新服务，并且前台不影响用户的正常体验

### Global.java

#### 本地版本和服务端版本信息

创建两个静态变量,用于MainActivity中进行版本对比

```java
public static int LOCAL_VERSION = 0;
public static int SERVICE_VERSION = 0;
```

### UpdateService.java

#### 变量和常量的创建

创建如下变量和常量

```java
 //    成功和失败的返回码
    public static final Integer SUCCESS = 0x10000;
    public static final Integer FAILED = 0x10001;
    //    软件下载链接,这是微信的
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
```

#### 重写方法

继承Service重写onCreate、onDestroy、onStartCommand这三个方法

#### 创建通知

在onCreate和onDestroy方法中注册和注销EventBus

```java
  //注册EventBus
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }
```

```java
   //注销EventBus
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
```



#### 网络请求

在onStartCommand方法中创建一个新的线程用于进行网络请求

```java
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
```

#### 文件下载

创建fileLoad方法用于下载文件并发送进度

```java
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
//        从url中获取文件名方法StringReverse,反转第一遍
        String s = StringReverse(url);
        int i = s.indexOf("?");
        i += 1;
        int i1 = s.indexOf("/");
        String substring = s.substring(i, i1);
//        从url中获取文件名方法StringReverse，反转回来
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
```



#### 事件监听

创建一个方法，方法名自定义，参数类型为你通过EventBus发送的类型，用于获取进度和改变通知的进度条百分比

```java
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
```



#### 字节转换

在文件下载的写入环节有一个currentSize = transitionBit(progress);这个方法是将单位为b的转换成mb，用于获取人们普遍认知的文件大小

```java
//    b转mb
private Integer transitionBit(long b) {
    long l = b / 1024;
    long l1 = l / 1024;
    return Math.toIntExact(l1);
}
```

单位的转换基本规则如下:

```txt
1B（字节）=8b（位）
1 KB = 1024 B
1 MB = 1024 KB
1 GB = 1024 MB
1TB = 1024GB
```

所以b转成mb只需要乘上两个1024即可

#### 字符串反转

在文件下载设置文件安装包名称的时候我遇到了一个难题，微信的下载链接中安装包名即不在最后又不在最前，所以我通过反转后进行字符的截取操作，然后再反转一便就是安装包的名称了

```java
//    字符串反转
private String StringReverse(String str) {
    StringBuffer stringBuffer = new StringBuffer(str);
    String s = stringBuffer.reverse().toString();
    return s;
}
```

#### 安装程序

当我们完成了文件的下载后，再事件监听中会销毁进度条的通知创建一个下载完成安装通知，我们通过通知的setContentIntent(intent);设置安装程序，安装程序我们封装成一个方法

```java
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

        install.setDataAndType(uri, "application/vnd.android.package-archive");
//        将设置好的intent返回
        return install;
    }
```

install.setDataAndType中传入的第二个参数是安装apk所必需的，具体文件类型对比表如下

```java
//       	install取设置属于和类型，数据就是获取到的uri，更具文件类型不同，type参数也不相同，具体参考下表
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
```

每种文件类型对应不同的字符串。

### SmartApplication.java

#### 获取本地版本和设置服务端版本

在SmartApplication中我们只需要做的就是从服务端获取版本set到SERVICE_VERSION中了，获取本地版本信息set到LOCAL_VERSION版本中，这里我就不写网络请求了，直接设置。SmartApplication要继承Application

```java
public void initGlobal(){
        try {
            Global.LOCAL_VERSION = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
            Global.SERVICE_VERSION = 1;
        }catch (Exception ex){
            ex.printStackTrace();
        }
}
```

### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.android_study"
    android:versionCode="1"
    android:versionName="1.0">
    <!--   设置版本号，每个新版本都要手动修改然后再让用户跟新-->
```

权限注册

```xml
<!--    网络权限-->
<uses-permission android:name="android.permission.INTERNET" />
<!--    软件安装权限-->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<!--    文件读写权限-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Service注册，Service和Activity一样也是需要注册到清单文件中才能使用

```xml
    <service
            android:name=".common.update.UpdateService"
            android:enabled="true"
            android:exported="true"></service>
```

在application文件中需要注册provider，文件的安装需要这个标签

```xml
       <!--        声明provider标签-->
        <!--        authorities前面是你的项目包名  package="com.example.android_study",最够一个是fileprovider-->
        <!--        exported，是否开启跨应用共享数据，默认false-->
        <!--        grantUriPermissions 是否授予uri权限，默认false，我们选择true-->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.example.android_study.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <!--       meta-data     -->
            <!--       name选择android.support.FILE_PROVIDER_PATHS提供文件路径-->
            <!--       resource选择提供文件路径的文件，这边新建在xml下，文件名随便取-->
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```

#### file_paths.xml

在资源目录xml文件夹下创建file_paths.xml文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>

    <!--    外部路径-->
    <external-path
        name="external"
        path="." />
    <!--    外部文件路径-->
    <external-files-path
        name="external_files"
        path="." />
    <!--    隐藏路径-->
    <cache-path
        name="cache"
        path="." />
    <!--    外部隐藏路径-->
    <external-cache-path
        name="external_cache"
        path="." />


    <!--    最主要的是files-path和root-path，其它可写可不写-->
    <!--    文件路径-->
    <files-path
        name="files"
        path="." />
    <!--    根目录-->
    <root-path
        name="root_path"
        path="." />
</paths>
```

## 项目github下载地址

