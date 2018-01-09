package com.example.dell.number_plate_recognition;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission_group.CAMERA;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {

    private static  Camera mCamera;
    private CameraPreview mPreview;
    static byte byteArray[];
    private  RequestQueue queue;
    EditText editText;
    Button captureButton;
    final int Camera_Request=1;
    final String URL="http://www.hopaka.com/rest/b2b/detectNumberPlate";
    FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView=(ImageView)findViewById(R.id.icon);
        Iconify.with(new FontAwesomeModule());
        imageView.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_automobile).colorRes(R.color.white).sizeDp(25));
        captureButton=(Button)findViewById(R.id.button);
        editText=(EditText)findViewById(R.id.editText);
        preview = (FrameLayout) findViewById(R.id.frameLayout);
        queue = Volley.newRequestQueue(getApplicationContext());

//        if(checkCameraHardware(this))
//        {
//            checkPermission();
//        }
        mCamera = getCameraInstance();

        mPreview = new CameraPreview(this, mCamera);

        preview.addView(mPreview);
        final Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {

                byteArray=data;
                if(isNetworkAvailable())
                getNumber(URL);
                else {
                    Toast.makeText(MainActivity.this, "Please Connect to the internet", Toast.LENGTH_LONG).show();
                    mCamera.startPreview();
                    captureButton.setText("Scan");
                    captureButton.setEnabled(true);

                }
//                File pictureFile = getOutputMediaFile();
//
//                if (pictureFile == null){
//                    return;
//                }
//
//                try {
//                    FileOutputStream fos = new FileOutputStream(pictureFile);
//                    fos.write(data);
//                    fos.close();
//                    MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
//                } catch (FileNotFoundException e) {
//
//                } catch (IOException e) {
//
//                }

            }
        };

        captureButton.setOnClickListener(

                new View.OnClickListener() {

                    public void onClick(View v) {
                        mCamera.takePicture(null, null, mPicture);
                        captureButton.setText("Please Wait...");
                        captureButton.setEnabled(false);



                    }
                }
        );
    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {

            c = Camera.open();
            if(c==null) {
                checkPermission();
                c = Camera.open();
            }
        }
        catch (Exception e){

        }
        return c;
    }
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){

            return true;
        } else {

            return false;
        }
    }

    private void getNumber(String URL)
    {

        VolleyMultipartRequest postrequest = new VolleyMultipartRequest(Request.Method.POST, URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Log.i("no. ",obj.getString("plateNumber"));
                            if(obj.getString("plateNumber").equalsIgnoreCase("null"))
                                editText.setText("Please try again...");
                            else
                                editText.setText(obj.getString("plateNumber"));
                            captureButton.setText("Scan");
                            captureButton.setEnabled(true);
                            mCamera.startPreview();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Error ",error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(byteArray));
                if(byteArray==null)
                {
                    return null;
                }
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        //adding the request to volley
        queue.add(postrequest);;
    }
//    private File getOutputMediaFile()
//    {
//        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()+"/NumberPlateAPP");
//        if (! mediaStorageDir.exists()){
//            if (! mediaStorageDir.mkdirs()){
//                return null;
//            }
//        }
//
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File mediaFile;
//
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_"+ timeStamp + ".jpg");
//
//
//        return mediaFile;
//
//    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    private void checkPermission()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        Camera_Request);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            String permissions[], int[] grantResults) {
            if(requestCode== Camera_Request)
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                }

                else {

                    //Snackbar.make(MainActivity.this, "Permission Denied, You cannot access location data and camera.", Snackbar.LENGTH_LONG).show();
                    Toast.makeText(this, "Please provide the required Permissions", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            showMessageOKCancel("You need to allow access camera permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                checkPermission();
                                            }
                                        }
                                    });
                        }
                    }



                }
            }

    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
        .setMessage(message)
        .setPositiveButton("OK", okListener)
        .setNegativeButton("Cancel", null)
        .create()
        .show();
    }
}

