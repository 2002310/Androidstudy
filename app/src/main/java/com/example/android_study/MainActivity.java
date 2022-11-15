package com.example.android_study;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import com.example.android_study.common.config.Global;
import com.example.android_study.common.update.UpdateService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        getPermissionCamera(this);
        checkVersion();
    }

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

    private void checkVersion() {
        if (Global.LOCAL_VERSION<Global.SERVICE_VERSION) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("软件更新通知")
                    .setMessage("发现新版本，建议立即跟新")
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
    }
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

}