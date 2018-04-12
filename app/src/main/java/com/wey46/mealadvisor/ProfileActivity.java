package com.wey46.mealadvisor;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private String email;
    private ImageView imageView;
    private TextView hint;
    private String mFileLoc = "";
    final int TAKE_PHOTO = 0;
    final int OPEN_GALLERY = 1;
    final String BEFORE_UPDATE = "Pick Your Avatar or Take a Picture!";
    final String AFTER_UPDATE = "Not Satisfied? 'Shake it off' and Do It Again :-)";
    Uri pickedUri;
    boolean iv_changed = false;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //TODO: from stackoverflow: to solve uri exposure problem for api level higher than 23 (can't use uri.fromFile() method) but not a good solution
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());

        email = getIntent().getExtras().getString("EMAIL");
        TextView tv = findViewById(R.id.tvHello);
        String hello = "Hello, " + email;
        tv.setText(hello);

        hint = findViewById(R.id.tv_hint);
        hint.setText(BEFORE_UPDATE);

        Button btn = findViewById(R.id.btn_camera);
        imageView = findViewById(R.id.iv_head);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;

            try{
                photoFile = createImageFile();
            } catch (IOException e){
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(intent, TAKE_PHOTO);

        });

        Button btn2 = findViewById(R.id.btn_gallery);
        btn2.setOnClickListener(v -> {
            openGallery();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TAKE_PHOTO && resultCode == RESULT_OK){
            //Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            //Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            //Bitmap captured = BitmapFactory.decodeFile(mFileLoc);
            //imageView.setImageBitmap(captured);
            setImageView();
            iv_changed = true;
        } else if(requestCode == OPEN_GALLERY && resultCode == RESULT_OK){
            //TODO: can also compress image
            pickedUri = data.getData();
            imageView.setImageURI(pickedUri);
            iv_changed = true;
        }
        if(iv_changed){
            hint.setText(AFTER_UPDATE);
            openSensorListener();
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        //replaces the default 'Back' button action
//        if(keyCode==KeyEvent.KEYCODE_BACK)
//        {
//
//            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
//            finish();
//            startActivity(intent);
//
//        }
//        return true;
//    }

    private File createImageFile() throws IOException {
        String timestap = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        System.out.println(timestap);
        String imageFileName = "Image_"+timestap+"_";
        File storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storage);

        mFileLoc = image.getAbsolutePath();
        return image;
    }

    //reduce the size of bitmap to be just fit into image view:
    private void setImageView(){
        //1. get reduce factor
        int viewHeight = imageView.getHeight();
        int viewWidth = imageView.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mFileLoc,options);
        //2. set image
        options.inSampleSize = Math.min(options.outWidth/viewWidth, options.outHeight/viewHeight);
        options.inJustDecodeBounds = false;
        Bitmap reducedImage = BitmapFactory.decodeFile(mFileLoc, options);
        imageView.setImageBitmap(reducedImage);
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, OPEN_GALLERY);
    }

    private void openSensorListener(){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> list= sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(list.size()>0){
            sensorManager.registerListener(listener, list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(getBaseContext(), "Sensor listener activated", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }

    }

    SensorEventListener listener = new SensorEventListener() {

        long lastUpdate = -1;
        float x,y,z,last_x,last_y,last_z;
        int SHAKE_THRESHOLD = 800;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                long curTime = System.currentTimeMillis();
                // only allow one update every 100ms.
                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];

                    if(Round(x,4)>10.0000){
                        Log.d("sensor", "X Right axis: " + x);
                        Toast.makeText(getApplicationContext(), "Right shake detected", Toast.LENGTH_SHORT).show();
                    }
                    else if(Round(x,4)<-10.0000){
                        Log.d("sensor", "X Left axis: " + x);
                        Toast.makeText(getApplicationContext(), "Left shake detected", Toast.LENGTH_SHORT).show();
                    }

                    float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
                    if (speed > SHAKE_THRESHOLD) {
                        Log.d("sensor", "shake detected w/ speed: " + speed);
                        Toast.makeText(getApplicationContext(), "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();

                        // Reset to default avatar
                        InputStream inputStream = null;
                        try {
                            String imageFile = "default_head.png";
                            inputStream = getAssets().open(imageFile);
                            Drawable d = Drawable.createFromStream(inputStream, null);
                            imageView.setImageDrawable(d);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        //reset hint
                        hint.setText(BEFORE_UPDATE);

                        //reset iv_changed
                        iv_changed = false;

                        //close sensor
                        if (sensorManager != null) {
                            sensorManager.unregisterListener(listener);
                            sensorManager = null;
                        }

                    }
                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    protected void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
            sensorManager = null;
        }
        super.onPause();
    }

    //TODO: why need round?
    private static float Round(float R, int P) {
        float pf = (float)Math.pow(10,P);
        R = R * pf;
        float tmp = Math.round(R);
        return tmp/P;
    }

}
