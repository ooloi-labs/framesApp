package packag.shoaib.com.posegrapher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static packag.shoaib.com.posegrapher.MainActivity.params;

public class PictureActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE = 1;
    private Intent newintent;
    private ImageView imageView;
    private static final String IMAGE_DIRECTORY = "/PoseGrapher";
    private Matrix matrix;
    private ImageView img1;
    private ImageView img2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        imageView = findViewById(R.id.img);
        img1 = findViewById(R.id.cam1);
        img2 = findViewById(R.id.cam2);

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newintent = new Intent();
                newintent.setAction(Intent.ACTION_PICK);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + File.separator + "PoseGrapher" + File.separator);
                newintent.setDataAndType(uri, "image/*");
                startActivityForResult(newintent, SELECT_IMAGE);
            }
        });

        imageView.setImageBitmap(MainActivity.bitmap);
        if (MainActivity.callsaveImage) {
            imageView.setRotation(90);
            saveImage(MainActivity.bitmap);
        } else {
            imageView.setRotation(0);
            imageView.setImageBitmap(MainActivity.pickbitmap);
        }
    }

    public String saveImage(Bitmap myBitmap) {
        matrix = new Matrix();
        matrix.postRotate(90);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        Log.d("TAG", "Image Captured");
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs());
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        MainActivity.mCamera.setParameters(params);
        return "";

    }
}