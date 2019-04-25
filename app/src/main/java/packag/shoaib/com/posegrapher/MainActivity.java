package packag.shoaib.com.posegrapher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.Px;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.security.Policy;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String IMAGE_DIRECTORY = "/PoseGrapher";
    private static final int SELECT_IMAGE = 1;
    private static final int RESULT_LOAD_IMG = 1;
    public static Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private Button capture, switchFlash, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    public static Bitmap bitmap;
    public static Bitmap pickbitmap;

    private ImageView img;
    private ViewGroup rootLayout;

    private Intent newintent;

    public static ViewGroup rootBackup;
    public static LinearLayout camBackup;
    SeekBar sk1;
    private int _xDelta;
    private int _yDelta;
    public int i_overlaychange;
    public static Camera.Parameters params;
    private boolean isFlashOn = false;
    private int ii;

    private static final int WIDTH_SCALE_RATIO = 15;
    private static final int HEIGHT_SCALE_RATIO = 15;
    private int previousProcess = 0;
    public static boolean callsaveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (ViewGroup) findViewById(R.id.view_root);

        img = (ImageView) rootLayout.findViewById(R.id.overlay_imageView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;


        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        cameraPreview = (LinearLayout) findViewById(R.id.cPreview);
        mPreview = new CameraPreview(myContext, mCamera);

        params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        cameraPreview.addView(mPreview);

        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);

        icon_foot = (Button) findViewById(R.id.btn1);
        icon_hand = (Button) findViewById(R.id.btn2);
        icon_teeth = (Button) findViewById(R.id.btn3);

        sk1 = (SeekBar) findViewById(R.id.seekBar);
        sk1.setVisibility(View.INVISIBLE);
        sk1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        capture = (Button) findViewById(R.id.btnCam);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);

            }
        });


        mCamera.startPreview();

    }

    private void resizeImageView(int width, int height) {

        RelativeLayout.LayoutParams layoParams = new RelativeLayout.LayoutParams(width, height);
        img.setLayoutParams(layoParams);
    }

    private void scaleImage(ImageView img, int scale) {
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        width += scale * WIDTH_SCALE_RATIO;
        height += scale * HEIGHT_SCALE_RATIO;
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, true);
        img.setImageBitmap(bitmap);

    }

    private int findFrontFacingCamera() {

        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;

    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public void onResume() {

        super.onResume();
        if (mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            Log.d("nu", "null");
        } else {
            Log.d("nu", "no null");
        }

    }

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                bitmap = null;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                newintent = new Intent(MainActivity.this, PictureActivity.class);
                callsaveImage = true;
                startActivity(newintent);
            }
        };
        return picture;
    }

    static int overlay_visible_images[] = new int[]{R.drawable.feet_compressed, R.drawable.client_hand_2_compressed, R.drawable.client_teeth_2_compressed};
    static int overlay_images_active[] = new int[]{R.drawable.foot_black, R.drawable.hand_black, R.drawable.teeth_black};
    static int overlay_images_unactive[] = new int[]{R.drawable.foot_gray, R.drawable.hand_gray, R.drawable.teeth_gray};

    Button icon_foot;
    Button icon_hand;
    Button icon_teeth;

    public void nClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:

                icon_foot.setBackgroundResource(overlay_images_active[0]);
                imageChanger(overlay_visible_images[0]);
                icon_hand.setBackgroundResource(overlay_images_unactive[1]);
                icon_teeth.setBackgroundResource(overlay_images_unactive[2]);
                break;
            case R.id.btn2:

                icon_hand.setBackgroundResource(overlay_images_active[1]);
                imageChanger(overlay_visible_images[1]);
                icon_foot.setBackgroundResource(overlay_images_unactive[0]);
                icon_teeth.setBackgroundResource(overlay_images_unactive[2]);
                break;
            case R.id.btn3:

                icon_teeth.setBackgroundResource(overlay_images_active[2]);
                imageChanger(overlay_visible_images[2]);
                icon_foot.setBackgroundResource(overlay_images_unactive[0]);
                icon_hand.setBackgroundResource(overlay_images_unactive[1]);
                break;
            case R.id.btnGallery:
                openGallery();
                break;
            case R.id.btnSwitchFlash:
                switchFlash = (Button) findViewById(R.id.btnSwitchFlash);
                switchFlash();
                break;
            default:
                throw new RuntimeException("Unknown Btn ID");
        }
    }

    private void switchFlash() {
        if (!isFlashOn) {
            switchFlash.setBackgroundResource(R.drawable.flash_on);
            params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
            isFlashOn = true;
        } else {
            switchFlash.setBackgroundResource(R.drawable.flash_off);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            isFlashOn = false;
        }
    }

    private void openGallery() {
        newintent = new Intent();
        newintent.setAction(Intent.ACTION_PICK);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + File.separator + "PoseGrapher" + File.separator);
        newintent.setDataAndType(uri, "image/*");
        startActivityForResult(newintent, SELECT_IMAGE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        callsaveImage = false;
                        pickbitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), data.getData());
                        newintent = new Intent(MainActivity.this, PictureActivity.class);
                        startActivity(newintent);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplication(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void imageChanger(int img_Res) {
        img.setImageResource(img_Res);
        i_overlaychange = img_Res;
    }

    private class ChoiceTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;
                    layoutParams.rightMargin = -250;
                    layoutParams.bottomMargin = -250;
                    v.setLayoutParams(layoutParams);
                    break;
            }
            rootLayout.invalidate();
            return true;
        }
    }
}