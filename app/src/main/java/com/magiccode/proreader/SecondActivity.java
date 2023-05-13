package com.magiccode.proreader;


import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

import androidmads.library.qrgenearator.QRGEncoder;


public class SecondActivity extends AppCompatActivity {
    private ImageView qrCodeIV;
    private EditText dataEdt;
    private Button generateQrBtn,saveqr;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // initializing all variables.
        qrCodeIV = findViewById(R.id.idIVQrcode);
        dataEdt = findViewById(R.id.idEdt);
        generateQrBtn = findViewById(R.id.idBtnGenerateQR);
        saveqr=findViewById(R.id.savethis);
        getSupportActionBar().hide();

        // initializing onclick listener for button.
        generateQrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dataEdt.getText().toString())) {

                    // if the edittext inputs are empty then execute
                    // this method showing a toast message.
                    Toast.makeText(SecondActivity.this, "Enter some text to generate QR Code", Toast.LENGTH_SHORT).show();
                }
                else {
                    // below line is for getting
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

                    // initializing a variable for default display.
                    Display display = manager.getDefaultDisplay();

                    // creating a variable for point which
                    // is to be displayed in QR Code.
                    Point point = new Point();
                    display.getSize(point);

                    // getting width and
                    // height of a point
                    int width = point.x;
                    int height = point.y;

                    // generating dimension from width and height.
                    int dimen = width < height ? width : height;
                    dimen = dimen * 3 / 4;

                    // setting this dimensions inside our qr code
                    // encoder to generate our qr code.
                    try {
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(dataEdt.getText().toString(), BarcodeFormat.QR_CODE, 400, 400);
                        ImageView imageViewQrCode = (ImageView) findViewById(R.id.idIVQrcode);
                        imageViewQrCode.setImageBitmap(bitmap);
                        SaveToGallery(bitmap);
                    } catch(Exception e) {

                    }
                    ActivityCompat.requestPermissions(SecondActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    ActivityCompat.requestPermissions(SecondActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    saveqr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(SecondActivity.this,"File Saved",Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
    }
    public void SaveToGallery(Bitmap bitmap) {
//                            BitmapDrawable bitmapDrawable = (BitmapDrawable) qrCodeIV.getDrawable();
//                            Bitmap bitmap= bitmapDrawable.getBitmap();
        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsoluteFile()+"/Download");
        dir.mkdirs();

        String filename = String.format("%d.png",System.currentTimeMillis());
        File outFile = new File(dir,filename);
        try {
            outputStream = new FileOutputStream(outFile);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        try {
            outputStream.flush();

        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
