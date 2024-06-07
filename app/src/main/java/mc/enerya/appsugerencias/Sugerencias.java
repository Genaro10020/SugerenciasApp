package mc.enerya.appsugerencias;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.enerya.appsugerencias.R;

import java.io.File;
import java.io.IOException;

public class Sugerencias extends AppCompatActivity {

    private String currentPhotoPath;
    private static final int IMAGE_PICK_CODE=1000;
    private  static  final int PERMISSION_CODE=1001;
    private  static  final int REQUEST_IMAGE_CAPTURE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView myWebView = findViewById(R.id.webview);

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
                    }
                    // Aquí puedes agregar cualquier otra lógica que necesites
                    return false; // o false según tu lógica
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



}