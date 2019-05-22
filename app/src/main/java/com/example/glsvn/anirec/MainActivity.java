package com.example.glsvn.anirec;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView imageview;
    ImageView imageview2;
    Button save;

    private Uri imageUri;
    private String imagelink;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference,databaseReference2;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageview = findViewById(R.id.imageview);
        imageview2 = findViewById(R.id.imageview2);
        save=findViewById(R.id.save);

        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference().child("Images");
        databaseReference2=firebaseDatabase.getReference().child("results");
        firebaseStorage=FirebaseStorage.getInstance();
        mStorageRef=firebaseStorage.getReference();
        save.setEnabled(false);
        internetControl();

        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kaydet();
            }
        });
    }


    public void kaydet() {

        if (imageUri != null) {

            final StorageReference imagePath = mStorageRef.child("images/"+"plantImg");

            imagePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
            imagePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imagePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isComplete()){
                        Uri downUri = task.getResult();
                        imagelink=downUri.toString();
                        databaseReference.setValue(imagelink);

                        Toast.makeText(getBaseContext(),"Kaydedildi.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }



    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Seciniz");
        String[] pictureDialogItems = {
                "Galeriden fotağraf seçiniz",
                "Kameradan fotağraf çekiniz"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();

    }

    void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,1);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, 1);
        }

    }

    void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            askForPermission(Manifest.permission.CAMERA,2);
            startActivityForResult(intent, 2);
        }

    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (reqCode) {
                case 1:
                    try {
                        imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageview2.setImageBitmap(selectedImage);
                        //txt.setVisibility(View.GONE);
                        save.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    imageview2.setImageBitmap(photo);

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                    //you can create a new file name "test.jpg" in sdcard folder.
                    File f = new File(Environment.getExternalStorageDirectory()
                            + File.separator + "Anirec.jpg");
                    try {

                        f.createNewFile();

                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());

                        fo.close();

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (f.exists()) {

                        Toast.makeText(this, "Image Found : "+f.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(this, "Image Not Found", Toast.LENGTH_SHORT).show();
                    }


                    //txt.setText("Resmi Galeriden seçiniz");
                    save.setEnabled(true);
                    break;
            }
        }

    }


    void internetControl() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected == false) {
            Toast.makeText(getBaseContext(), "İnternet bağlantısı yok!", Toast.LENGTH_SHORT).show();

        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " izin zaten verildi.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                //Location
                case 1:
                    askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,1);
                    break;
                //Call
                case 2:
                    askForPermission(Manifest.permission.CAMERA,2);
                    break;
            }

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    

}
