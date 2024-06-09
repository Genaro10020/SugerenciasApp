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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.enerya.appsugerencias.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Sugerencias extends AppCompatActivity {

    private String currentPhotoPath;
    private static final int IMAGE_PICK_CODE=1000;
    private  static  final int PERMISSION_CODE=1001;
    private  static  final int REQUEST_IMAGE_CAPTURE=1;
    int fotografiaTomada =0;
    ImageView imagen;
    String idEquipo;
    Bitmap bitmapf;
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
                    idEquipo = uri.getQueryParameter("id_equipo");
                    if (uri.getQueryParameter("app") == null) {
                        // Si no contiene el parámetro "app", añadirlo a la URL
                        Uri.Builder builder = uri.buildUpon();
                        builder.appendQueryParameter("app", "true");
                        String newUrl = builder.build().toString();
                        view.loadUrl(newUrl);
                        TakePhoto();
                        // Devolver true para indicar que la navegación debe ser manejada por esta función
                        return true;
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
 // Asignar el Bitmap a bitmapf
            bitmapf = resizedBitmap;
            ejecutarservicio("https://vvnorth.com/Sugerencia/app/guardarFotografia.php");
        }


    }

    private void ejecutarservicio(String URL)
    {
        StringRequest stringRequest=new  StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Respuesta del servidor",response);
                //VolverANuevaAuditoria();
                // buscarProducto("https://vvnorth.com/comparacion_auditorf.php",NPlanta);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                //VolverANuevaAuditoria();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros =new HashMap<String,String>();
                String imageData= imageToString(bitmapf);

                parametros.put("id_equipo",idEquipo);
                parametros.put("fotografia",imageData);

                return parametros;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String imageToString(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20, outputStream);
        byte[] imageBytes= outputStream.toByteArray();
        String encodeImage= Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodeImage;
    }



}