package mc.enerya.appsugerencias;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;*/

public class Sugerencias extends AppCompatActivity {

    private String currentPhotoPath;
    private static final int IMAGE_PICK_CODE=1000;
    private  static  final int PERMISSION_CODE=1001;
    private  static  final int REQUEST_IMAGE_CAPTURE=101;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    int fotografiaTomada =0;
    ImageView imagen;
    String WhoTakePhoto="";
    String textoPorVoz="";
    String idEquipo,UltimoID,NumeroNomina;
    Bitmap bitmapf;
    WebView myWebView;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean isRequestingAudioPermission = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = findViewById(R.id.webview);
        imagen = findViewById(R.id.imageView);

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setSupportZoom(false);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 if (url.startsWith("https://vvnorth.com/Sugerencia/principalColaborador.php")) {
                     Uri uri = Uri.parse(url);
                     if (uri.getQueryParameter("app") == null || uri.getQueryParameter("version") == null  ) {
                         // Si no contiene el parámetro "app", añadirlo a la URL
                         Uri.Builder builder = uri.buildUpon();
                         builder.appendQueryParameter("app", "true");
                         builder.appendQueryParameter("version", "2.1.0");
                         String newUrl = builder.build().toString();
                         view.loadUrl(newUrl);
                         return true;
                     }
                }else if (url.startsWith("https://vvnorth.com/Sugerencia/juntasArranque.php")) {
                    // Verificar si la URL contiene el parámetro "id_equipo"
                    //Log.e("URL", "igual");
                    Uri uri = Uri.parse(url);
                    idEquipo = uri.getQueryParameter("id_equipo");
                    if (uri.getQueryParameter("app") == null) {
                        // Si no contiene el parámetro "app", añadirlo a la URL
                        Uri.Builder builder = uri.buildUpon();
                        builder.appendQueryParameter("app", "true");
                        String newUrl = builder.build().toString();
                        view.loadUrl(newUrl);
                        return true;
                    }
                }else if (url.startsWith("https://vvnorth.com/Sugerencia/seguridadColaborador.php")) {
                    Uri uri = Uri.parse(url);
                    if (uri.getQueryParameter("app") == null) {
                        // Si no contiene el parámetro "app", añadirlo a la URL
                        Uri.Builder builder = uri.buildUpon();
                        builder.appendQueryParameter("app", "true");
                        // Si textoPorVoz tiene un valor, agregarlo como parámetro "dictado"
                        String newUrl = builder.build().toString();

                        view.loadUrl(newUrl);
                        return true;
                    }
                }else if(url.startsWith("https://vvnorth.com/Sugerencia/ejecutarCamaraMovil.php")){
                    tomarFoto();
                    return false;
                }else if(url.startsWith("https://vvnorth.com/Sugerencia/ejecutarCamaraMovilSeguridad.php")){
                    Uri uri = Uri.parse(url);
                    UltimoID = uri.getQueryParameter("UltimoID");
                    NumeroNomina = uri.getQueryParameter("NumeroNomina");
                    WhoTakePhoto = "Seguridad";
                    tomarFoto();
                    return false;
                }else if(url.startsWith("https://vvnorth.com/Sugerencia/ejecutarDictadoVozSeguridad.php")){
                    startSpeechToText();
                    return false;
                }
                return false;
            }
        });

        // Cargar la URL en el WebView
        myWebView.loadUrl("https://vvnorth.com/Sugerencia/");
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...");
        try {
            startActivityForResult(intent, REQUEST_RECORD_AUDIO_PERMISSION);

        } catch (Exception e) {
            Toast toas = Toast.makeText(getApplicationContext(),"Dispositivo no soporta esta opción",Toast.LENGTH_SHORT);
            toas.show();
        }
    }


    public void solicitudMicrofonowWeb(){
        // Solicitar permiso de micrófono si no está concedido
        if (ContextCompat.checkSelfPermission(Sugerencias.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            isRequestingAudioPermission = true; // Indicar que se está solicitando el permiso
            ActivityCompat.requestPermissions(Sugerencias.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else if (!isRequestingAudioPermission) {
            // Permiso ya concedido y no se está solicitando actualmente
            Toast.makeText(Sugerencias.this, "Dicta tu hallazgo", Toast.LENGTH_SHORT).show();
        }
    }

    public void tomarFoto(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido, lanzar la cámara directamente
            lanzarCamara();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, lanzar la cámara
                lanzarCamara();
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(this, "Es necesario conceder el permiso de cámara", Toast.LENGTH_LONG).show();
               // myWebView.loadUrl("https://vvnorth.com/Sugerencia/principalColaborador.php");
            }
        }
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de micrófono encendidos.", Toast.LENGTH_SHORT).show();

            } else {
                // Permiso denegado, muestra un mensaje al usuario
                Toast.makeText(this, "Permiso de micrófono denegado.", Toast.LENGTH_SHORT).show();
            }
            isRequestingAudioPermission = false; // Restablecer el estado
        }

    }

    private void lanzarCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Procesar la imagen capturada
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


            int newWidth = 1000;
            int newHeight = 1000;

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);
            // Mostrar la imagen en un ImageView, por ejemplo
            imagen.setImageBitmap(resizedBitmap);
            //imagen.setVisibility(View.VISIBLE);
            bitmapf = resizedBitmap;
            if(WhoTakePhoto.equals("Seguridad")){
                ejecutarservicioSeguridad("https://vvnorth.com/Sugerencia/app/guardarFotografiaSeguridad.php");
            }else{
                //Log.e("Respuesta","Es junta de arranque");
                ejecutarservicio("https://vvnorth.com/Sugerencia/app/guardarFotografia.php");
            }
        }
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                    Toast.makeText(this, "Se grabo esto: "+spokenText, Toast.LENGTH_SHORT).show();
                    textoPorVoz = spokenText;
                // Crear la URL con los parámetros
                String url = "https://vvnorth.com/Sugerencia/seguridadColaborador.php";

                Uri uri = Uri.parse(url);
                Uri.Builder builder = uri.buildUpon();

                // Agregar el parámetro "app" si no está presente
                builder.appendQueryParameter("app", "true");

                // Agregar el parámetro "dictado" si el texto grabado no está vacío
                if (textoPorVoz != null && !textoPorVoz.isEmpty()) {
                    builder.appendQueryParameter("dictado", Uri.encode(textoPorVoz));
                }

                // Construir la nueva URL
                String newUrl = builder.build().toString();

                // Redirigir al WebView con la nueva URL
                myWebView.loadUrl(newUrl);
            }
        }
    }


    private void ejecutarservicio(String URL)
    {
        StringRequest stringRequest=new  StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Respuesta del servidor",response);
                if(response.equals("Foto Guardada")){
                    myWebView.loadUrl("https://vvnorth.com/Sugerencia/principalColaborador.php");
                    Toast.makeText(Sugerencias.this, "La fotografía se tomo con éxito", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(Sugerencias.this, "La fotografía no se guardo con éxito ", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
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


    private void ejecutarservicioSeguridad(String URL)
    {
        StringRequest stringRequest=new  StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Respuesta del servidor",response);
                if(response.equals("Foto Guardada")){

                    myWebView.loadUrl("https://vvnorth.com/Sugerencia/seguridadColaborador.php?app=app&&FotoTomada='true'");

                    Toast.makeText(Sugerencias.this, "La fotografía se tomo con éxito", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(Sugerencias.this, "La fotografía no se guardo con éxito ", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros =new HashMap<String,String>();
                String imageData= imageToString(bitmapf);
                parametros.put("numero_nomina",NumeroNomina);
                parametros.put("id_hallazgo",UltimoID);
                parametros.put("fotografia",imageData);
                return parametros;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Reducir la calidad de compresión de la imagen
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        String encodeImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodeImage;
    }





}