package com.association.punchclock;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.association.punchclock.Models.Clock;
import com.association.punchclock.Models.CurLocation;
import com.association.punchclock.Models.User;
import com.association.punchclock.Utils.ApiService;
import com.association.punchclock.Utils.Utils;
import com.association.punchclock.Views.AutoFitTextureView;
import com.association.punchclock.Views.CodeInput;
import com.association.punchclock.Views.KeyboardView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseActivity {
    @BindView(R.id.mKeyboard)
    KeyboardView mKeyboard;
    @BindView(R.id.verify_code)
    CodeInput verify_code;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.autoFitTextureView)
    AutoFitTextureView textureView;
    @BindView(R.id.txt_active) TextView txt_active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDrawerLayout.setScrimColor(getColor(R.color.drawer_back));

        mKeyboard.SetKeyboardListener(new KeyboardView.KeyboardListener() {
            @Override
            public void OnKeyboardPress(int keycode) {
                verify_code.sendKey(keycode);
            }
        });
        verify_code.SetOnCompleteListener(new CodeInput.CompleteListener() {
            @Override
            public void OnComplete(String key) {
                sendPunchCode(key);
            }
        });
        if (!MainApplication.mDeviceInfo.isActive()) {
            txt_active.setVisibility(View.VISIBLE);
        } else {
            txt_active.setVisibility(View.GONE);
        }
        checkPermisson();
    }

    private boolean enablingLocation = false;

    private void checkPermisson() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }
        initCamera();
        checkGPS();
    }

    private void initLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!statusOfGPS) {
            MainApplication.cur_location = new CurLocation();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                CurLocation cur_location = new CurLocation();
                cur_location.setLatitude(location.getLatitude());
                cur_location.setLongitude(location.getLongitude());

                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address adr = addresses.get(0);
                        cur_location.setArea(adr.getAdminArea());
                        cur_location.setCity(adr.getLocality());
                        cur_location.setCountry(adr.getCountryName());
                        cur_location.setPostal_code(adr.getPostalCode());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainApplication.cur_location = cur_location;
            }
        });
    }

    private void checkGPS() {
        boolean hasGps = getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        if (!hasGps) return;
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Manager");
            builder.setMessage("Would you like to enable GPS?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    enablingLocation = true;
                    startActivity(i);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.create().show();
        } else {
            initLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        initCamera();
        checkGPS();
    }

    public void sendPunchCode(String key) {
        boolean isTablet = Utils.isTablet(this);
        if (!isTablet) {
            showErrorDialog("Please try with iPhone device!");
            return;
        }
        verify_code.clear(0);
        showLoading();
        ApiService.checkClockState(key, new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoading();
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        int id = 0;
                        String in_date = "";
                        int state = 1;
                        if (response.isNull("punch_clock")) {
                        } else {
                            JSONObject punch_clock = response.getJSONObject("punch_clock");
                            id = punch_clock.getInt("id");
                            in_date = punch_clock.getString("in_date");
                            state = punch_clock.getInt("state");
                        }

                        JSONObject worker = response.getJSONObject("worker");
                        int workerid = worker.getInt("id");
                        String firstname = worker.getString("firstname");
                        String middlename = worker.getString("middlename");
                        String lastname = worker.getString("lastname");
                        String name = firstname + " " + middlename + " " + lastname;

                        showClock(id, in_date, state, name, workerid);

                    } else {
                        showErrorDialog(response.getString("message"));
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                    showErrorDialog("Something went wrong!");
                }
            }

            @Override
            public void onError(ANError anError) {
                hideLoading();
                showErrorDialog("Something went wrong, Check your network!");
            }
        });
    }

    public void updateClock() {
        ApiService.updateClock(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoading();
                Toast.makeText(MainActivity.this, "Sucessfully updated your clock", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(ANError anError) {
                hideLoading();
            }
        });
    }

    public void showClock(int id, String in_date, int state, String name, int workerid) {
        String title = "Welcome to <b>" + name + "</b>";
        String content = "You are currently Clocked Out <br/><br/> Please Click below to Clock In ";
        String confirm = "Clock In";
        if (state == 0) {
            confirm = "Clock Out";
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getDefault());
                Date date1 = format.parse(in_date);
                Date date2 = new Date();
                long mills = date2.getTime() - date1.getTime();
                int hours = (int) (mills / (1000 * 60 * 60));
                content = "You have been Clocked In since: <br/>" + Utils.parseDate(in_date) + " for total of " + hours + (hours > 1 ? " Hour" : " Hours");
            } catch (Exception err) {
                err.printStackTrace();
                content = "You have been Clocked In since: <br/>" + Utils.parseDate(in_date);
            }
        }
        content += "<br/>";
        showCancelableDialog(title, content, confirm, new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                MainApplication.selectedClock = new Clock(id, state, workerid, "");
                takePicture();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.img_navicon, R.id.lay_logout})
    protected void OnViewClick(View v) {
        switch (v.getId()) {
            case R.id.img_navicon:
                openDrawer();
                break;
            case R.id.lay_logout:
                logout();
                break;
            default:
                break;
        }
    }

    protected void logout() {
        MainApplication.user = new User();
        Utils.saveToken();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void openDrawer() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }


//    camera  --------------------------------------------------------------------------

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    private File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + "/" + System.currentTimeMillis() + ".jpg");

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private int mSensorOrientation;

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (null != textureView || null == imageDimension) {
                textureView.setTransform(Utils.configureTransform(width, height, imageDimension, MainActivity.this));
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {

            Log.e("tag", "onOpened");
            mCameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    public void initCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        textureView.setSurfaceTextureListener(textureListener);
    }

    private void openCamera(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e("tag", "is camera open");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e("tag", "permission deny");
            return;
        }
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new Utils.CompareSizesByArea());


            int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e("tag", "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            imageDimension = Utils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(
                        imageDimension.getWidth(), imageDimension.getHeight());
            } else {
                textureView.setAspectRatio(
                        imageDimension.getHeight(), imageDimension.getWidth());
            }

            if (null != textureView || null == imageDimension) {
                textureView.setTransform(Utils.configureTransform(width, height, imageDimension, MainActivity.this));
            }

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("tag", "openCamera X");
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e("tag", "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera(){
        try {
            mCameraOpenCloseLock.acquire();
            if (null != cameraCaptureSessions) {
                cameraCaptureSessions.close();
                cameraCaptureSessions = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (enablingLocation) {
            enablingLocation = false;
            initLocation();

        }
        Log.e("tag", "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.e("tag", "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
    protected void takePicture() {
        showLoading();
        if(null == cameraDevice) {
            Log.e("tag", "cameraDevice is null");
            updateClock();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e("tag", "permission deny");
            updateClock();
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));



            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    uploadImage(file);
                    createCameraPreview();
                }

            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
        private void save(byte[] bytes) throws IOException {
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
            } finally {
                if (null != output) {
                    output.close();
                }
            }
        }
    };
    public void uploadImage(File file) {
        ApiService.uploadFile(file, new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    boolean success = response.getBoolean("success");
                    if(success){
                        String name = response.getString("name");
                        MainApplication.selectedClock.setImage(name);
                    }
                }catch (Exception err) {
                    err.printStackTrace();
                }
                updateClock();
            }
            @Override
            public void onError(ANError anError) {
                anError.printStackTrace();
                updateClock();
            }
        });
    }

}