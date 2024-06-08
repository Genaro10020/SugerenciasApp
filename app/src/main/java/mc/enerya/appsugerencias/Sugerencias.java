package mc.enerya.appsugerencias;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.enerya.appsugerencias.R;

import java.io.File;
import java.io.IOException;

public class Sugerencias extends AppCompatActivity {

    private String currentPhotoPath;
    private static final int IMAGE_PICK_CODE=1000;
    private  static  final int PERMISSION_CODE=1001;
    private  static  final int REQUEST_IMAGE_CAPTURE=1;
    int fotografiaTomada =0;
    ImageView imagen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView myWebView = findViewById(R.id.webview);
        imagen = findViewById(R.id.imageView);

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TakePhoto();
            }
        });
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setSupportZoom(false);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://vvnorth.com/Sugerencia/juntasArranque.php")) {
                    // Verificar si la URL contiene el parámetro "id_equipo"
                    Log.e("URL", "igual");
                    Uri uri = Uri.parse(url);
                    String idEquipo = uri.getQueryParameter("id_equipo");
                    if (idEquipo != null) {
                        Log.e("ID_EQUIPO", idEquipo); // Imprimir el valor de id_equipo en la consola
                        TakePhoto();

                    }else
                    // Aquí puedes agregar cualquier otra lógica que necesites
                    return false; // o false según tu lógica
                }else{
                    imagen.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });
        myWebView.loadUrl("https://vvnorth.com/Sugerencia/");
    }



    public void TakePhoto()
    {
        String fileName="photo";
        File StorageDirectory= getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile=File.createTempFile(fileName,".jpg",StorageDirectory);
            currentPhotoPath=imageFile.getAbsolutePath();
            Uri imageUri=  FileProvider.getUriForFile(Sugerencias.this,
                    "mc.enerya.appsugerencias",imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(intent,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pickImageFromGallery()
    {
// int to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGallery();
                } else {
//permision was denied

                }

            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

// Leer la información de rotación de la imagen desde los metadatos Exif
            ExifInterface exifInterface = null;
            try {
                exifInterface = new ExifInterface(currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    // No se necesita rotación
                    break;
            }

// Aplicar la rotación a la imagen
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

// Escalar la imagen si es necesario
            int newWidth = 1000;
            int newHeight = 1000;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, false);

// Mostrar la imagen en el ImageView
            imagen.setImageBitmap(resizedBitmap);
            imagen.setVisibility(View.VISIBLE);
        }


    }



}