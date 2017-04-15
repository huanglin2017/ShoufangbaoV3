package com.kupangstudio.shoufangbao;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceHolder.Callback;
import android.widget.Button;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.ImageUtils;
import com.kupangstudio.shoufangbao.widget.RotateTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Android手指拍照
 *
 * @author wwj
 * @date 2013/4/29
 */
public class CameraCardActivity extends BaseActivity {

    private Camera camera;
    private Camera.Parameters parameters = null;
    byte[] picByte = null;
    private Button btnTakePhoto;
    private RotateTextView tvCancel;
    private RotateTextView tvOk;
    private boolean hasTake;
    private boolean isCard;
    private boolean isCameraInit;
    private SurfaceView surfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示界面
        setContentView(R.layout.activity_camera_card);
        CommonUtils.addActivity(this);
        isCard = getIntent().getBooleanExtra("isCard", false);
        btnTakePhoto = (Button) findViewById(R.id.camera_photo);
        tvCancel = (RotateTextView) findViewById(R.id.camera_cancel);
        tvOk = (RotateTextView) findViewById(R.id.camera_ok);
        surfaceView = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        surfaceView.getHolder()
                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(176, 144);
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isCameraInit) {
                    Toast.makeText(CameraCardActivity.this, "相机初始化中，请稍后", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnTakePhoto.setEnabled(false);
                camera.takePicture(null, null, new MyPictureCallback());
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOk.setEnabled(true);
                btnTakePhoto.setEnabled(true);
                if (hasTake) {
                    camera.startPreview();
                    tvOk.setVisibility(View.GONE);
                    hasTake = false;
                } else {
                    finish();
                }
            }
        });
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvOk.isEnabled()) {
                    Toast.makeText(CameraCardActivity.this, "亲，照片读取中！", Toast.LENGTH_SHORT).show();
                }
                tvOk.setEnabled(false);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(camera == null) {
            surfaceView.getHolder()
                    .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceView.getHolder().setFixedSize(176, 144);
            surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
            surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数
        }
    }

    private final class MyPictureCallback implements PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                hasTake = true;
                picByte = data;
                saveImage(data);
                camera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final class SurfaceCallback implements Callback {

        // 拍照状态变化时调用该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, final int width,
                                   final int height) {
            if (camera == null) {
                Toast.makeText(CameraCardActivity.this, "请您打开相机权限！", Toast.LENGTH_SHORT).show();
                finish();
            }
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        initCamera();
                        camera.cancelAutoFocus();
                    }
                }
            });
        }

        // 开始拍照时调用该方法
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (camera == null) {
                    camera = Camera.open(); // 打开摄像头
                }
                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                camera.setDisplayOrientation(getPreviewDegree(CameraCardActivity.this));
                camera.startPreview();
            } catch (Exception e) {
                Toast.makeText(CameraCardActivity.this, "相机打开出错", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

        }

        // 停止拍照时调用该方法
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.stopPreview();
                camera.release(); // 释放照相机
                camera = null;
            }
        }
    }


    private void initCamera() {
        parameters = camera.getParameters(); // 获取各项参数
        parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List picSize = parameters.getSupportedPictureSizes();
        Camera.Size size = null;
        if (null != picSize && 0 < picSize.size()) {
            if (picSize.size() / 2 != 0) {
                size = (Camera.Size) picSize.get(picSize.size() / 2);
            }
        }
        if (size != null) {
            // 设置保存的图片尺寸
            parameters.setPictureSize(size.width, size.height);
        } else {
            // 设置保存的图片尺寸
            parameters.setPictureSize(
                    parameters.getSupportedPictureSizes().get(0).width,
                    parameters.getSupportedPictureSizes().get(0).height);
        }
        parameters.setJpegQuality(80); // 设置照片质量
        setDispaly(parameters, camera);
        try {
            camera.setParameters(parameters);
            camera.startPreview();
            isCameraInit = true;
            camera.cancelAutoFocus();
        } catch (Exception e) {
            Toast.makeText(CameraCardActivity.this, "相机打开出错", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
                if (camera != null && event.getRepeatCount() == 0) {
                    // 拍照
                    //注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
                    //，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
                    camera.takePicture(null, null, new MyPictureCallback());
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera, getPreviewDegree(CameraCardActivity.this));
        } else {
            parameters.setRotation(getPreviewDegree(CameraCardActivity.this));
        }
    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {

        }
    }

    private void saveImage(byte[] data) throws IOException {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        File file;
        if (isCard) {
            file = new File(Constants.IMAGE_PATH + File.separator + "authcard.jpg");
        } else {
            file = new File(Constants.IMAGE_PATH + File.separator + "authidcard.jpg");
        }
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if(!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
        ImageUtils.scaleSaveBmFile(file, CameraCardActivity.this, 500f, 500f);
        tvOk.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
