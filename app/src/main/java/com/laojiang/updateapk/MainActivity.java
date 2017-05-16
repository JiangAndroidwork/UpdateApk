package com.laojiang.updateapk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.skywds.android.bsdiffpatch.JniApi;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int INSTALL_APK = 12;
    private Button button;
    private String newFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String url = (String) msg.obj;
            installNewApk(url);

        }
    };
    @Override
    public void onClick(View v) {
        new Thread(new BsPatchRunnable()).start();
    }
    /**
     * 增量更新库。需要放到异步线程中使用 !!!
     */
    public class BsPatchRunnable implements Runnable {

        @Override
        public void run() {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                toast("未检测到外部存储设备，无法导出文件");
                return;
            }
            String oldVersionPath = getOldVersionPath(MainActivity.this);
            Log.i("旧版apk文件路径==", oldVersionPath);
            //新版本apk存放文件
            newFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "bjhj/bsdiff" + File.separator + "new.apk";

            //将增量更新patch文件放在  这里
            String updateFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "bjhj/bsdiff" + File.separator + "update.patch";

            //创建新版本apk所在文件夹
            new File(newFile).getParentFile().mkdirs();

            File patchFile = new File(updateFile);
            if (!patchFile.exists()) {
                toast("增量更新文件不存在");
                return;
            }
            //增量更新生成新版apk安装包
            if (oldVersionPath != null) {
                int bspatch = JniApi.bspatch(oldVersionPath, newFile, updateFile);

                toast("增量更新文件处理完成:" + bspatch);

                if (bspatch == 0) {
                    toast("补丁包处理完毕, 开始安装新版本");
                    //安装新版apk文件
                    install();

                } else {
                    toast("补丁包处理失败");
                }

            }


        }

    }

    private void install() {
        Message message = Message.obtain();
        message.what = INSTALL_APK;
        message.obj = newFile;
        handler.sendMessage(message);
    }

    /**
     * 安装apk
     */
    public void installNewApk(String filePath) {

        File currentFile = new File(filePath);
        if (!currentFile.exists()){
            return;
        }
        Uri uri = Build.VERSION.SDK_INT >= 24 ? FileProvider.getUriForFile(MainActivity.this, "com.android.updateapk", currentFile) : Uri.fromFile(currentFile);
        // 核心是下面几句代码
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri,
                "application/vnd.android.package-archive");
        startActivity(intent);
    }


    public void toast(String str) {
        Message msg = Message.obtain();
        msg.what = 0x123;
        msg.obj = str;
        mHandler.sendMessage(msg);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0x123)
                Toast.makeText(MainActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();

        }
    };

    /**
     * 获取原始apk路径
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    public String getOldVersionPath(Context context) {


        context = context.getApplicationContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String apkPath = applicationInfo.sourceDir;
        return apkPath;
    }
}
