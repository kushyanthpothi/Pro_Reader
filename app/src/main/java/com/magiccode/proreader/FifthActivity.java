package com.magiccode.proreader;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class FifthActivity extends AppCompatActivity {

    private TextView scannedtext;
    private ImageView iv,copy;
    private Button scan,recognize;

    private static final String TAG = "TAG_MAIN";
    private Uri imageUri = null;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private ProgressDialog progressDialog;
    private TextRecognizer textRecognizer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fifth);

        scannedtext = findViewById(R.id.text1);
        iv=findViewById(R.id.image);
        copy=findViewById(R.id.copytxt);
        scan=findViewById(R.id.scan);
        recognize=findViewById(R.id.decode);
        scannedtext.setInputType(InputType.TYPE_NULL);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        getSupportActionBar().hide();

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showinputimageDialog();
            }
        });
        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri==null) {
                    Toast.makeText(FifthActivity.this, "Pick Image First...", Toast.LENGTH_SHORT).show();
                }
                else {
                    recognizeTextfromImage();
                }
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output = scannedtext.getText().toString();
                if (output.isEmpty())
                    Toast.makeText(FifthActivity.this,"Please Scan any photo",Toast.LENGTH_LONG).show();
                else {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("MyData",output);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(FifthActivity.this, "Your Text is Copied!!", Toast.LENGTH_SHORT).show();

                }
            }
        });



    }

    private void recognizeTextfromImage() {
        Log.d(TAG, "recognizeTextfromImage");
        progressDialog.setMessage("Preparing Image...");
        progressDialog.show();

        try {
            InputImage inputImage= InputImage.fromFilePath(this,imageUri);
            progressDialog.setMessage("Recognizing Text...");
            Task<Text> textTaskResult = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            progressDialog.dismiss();
                            String RecognizedText = text.getText();
                            Log.d(TAG, "onSuccess: RecognizedText: "+RecognizedText);
                            scannedtext.setText(RecognizedText);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e(TAG, "onFailure:",e);
                            Toast.makeText(FifthActivity.this, "Failed Recognizing Text Due To"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "recognizeTextfromImage: ",e);
            Toast.makeText(this, "Failed Preparing Image Due To "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showinputimageDialog() {
        PopupMenu popupMenu = new PopupMenu(this,scan);
        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id ==1){
                    Log.d(TAG,"onMenuItemClick: Camera Clicked...");
                    if (checkCameraPermissions()) {
                        pickImageCamera();
                    }
                    else {
                        requestCameraPermissions();
                    }
                }
                else if (id ==2){
                    Log.d(TAG,"onMenuItemClick: Gallery Clicked...");
                    if (CheckStoragePermission()){
                        pickImageGallery();
                    }
                    else {
                        RequiredStoragePermissions();

                    }
                }
                return true;
            }
        });

    }
    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");
        Intent i = new Intent(Intent.ACTION_PICK);

        i.setType("image/*");
        galleryactivityresultLauncher.launch(i);

    }
    private ActivityResultLauncher<Intent> galleryactivityresultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        imageUri=data.getData();
                        Log.d(TAG, "onActivityResult: imageUri"+imageUri);
                        iv.setImageURI(imageUri);

                    }
                    else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(FifthActivity.this, "You get error while scanning",Toast.LENGTH_LONG).show();
                    }


                }
            }
    );
    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "SAMPLE TITLE");
        values.put(MediaStore.Images.Media.DESCRIPTION , "SAMPLE DESCRIPTION");
        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);


    }
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: imageUri"+imageUri);
                        iv.setImageURI(imageUri);
                    }
                    else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(FifthActivity.this, "Canceled", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );
    private boolean CheckStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void  RequiredStoragePermissions(){
        ActivityCompat .requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){

        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return cameraResult && storageResult;
    }
    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera & Storage Permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickImageGallery();
                    }
                    else {
                        Toast.makeText(this, "Storage Permission are required!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }
}
