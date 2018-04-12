package com.wey46.mealadvisor;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    String email;
    ImageView imageView;
    private String mFileLoc = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //TODO: from stackoverflow to solve uri exposure problem for api level higher than 23 (can't use uri.fromFile() method) but not a good solution
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());

        email = getIntent().getExtras().getString("EMAIL");
        TextView tv = findViewById(R.id.tvHello);
        String hello = "Hello, " + email;
        tv.setText(hello);

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
            startActivityForResult(intent, 0);

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            //Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            //Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Bitmap captured = BitmapFactory.decodeFile(mFileLoc);
            imageView.setImageBitmap(captured);
        }
    }

    File createImageFile() throws IOException {
        String timestap = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        System.out.println(timestap);
        String imageFileName = "Image_"+timestap+"_";
        File storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storage);

        mFileLoc = image.getAbsolutePath();
        return image;
    }
}
