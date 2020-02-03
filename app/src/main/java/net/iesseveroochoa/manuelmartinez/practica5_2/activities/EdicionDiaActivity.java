package net.iesseveroochoa.manuelmartinez.practica5_2.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import net.iesseveroochoa.manuelmartinez.practica5_2.R;
import net.iesseveroochoa.manuelmartinez.practica5_2.fragments.DatePickerFragment;
import net.iesseveroochoa.manuelmartinez.practica5_2.modelo.DiaDiario;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class EdicionDiaActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String EXTRA_DIA_A_GUARDAR = "net.iessochoa.manuelmartinez.practica5.activities.dia_guardar";

    //Constante que indica la posicion por defecto del spinner de valoracion
    private final int VALORACION_POR_DEFECTO = 5;


    EditText etFecha;
    Button btFecha;
    EditText etResumen;
    Spinner spValorarVida;
    EditText etContenido;
    FloatingActionButton fabGuardar;

    /**
     * Constantes necesarias para la imagen y la cámara
     */
    //seleccion del directorio principal
    private static String APP_DIRECTORY = "MyPictureApp/";
    //subcarpeta donde se guarda las imagenes
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    //para la imagen, el boton y el layout
    private ImageView ivImagenEA;
    private Button btImagen;
    private ConstraintLayout clEdicionActivity;

    //ruta donde se guardo la imagen
    private String mPath;

    /**
     * Método que genera un dialogo el cual indica que hay algunos campos incompletos y se deben rellenar
     */
    private void dialogoCamposIncompletos() {
        AlertDialog.Builder dlgAcercaDe = new AlertDialog.Builder(this);
        //Establecemos el título y el mensaje que queremos
        dlgAcercaDe.setTitle(getResources().getString(R.string.tituloError));
        dlgAcercaDe.setMessage(getResources().getString(R.string.mensajeErrorCamposIncompletos));
        // agregamos botón de aceptar al dialogo
        dlgAcercaDe.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cuando hagan click en el boton saldremos automaticamente,de la misma forma que si pulsa fuera del cuadro de dialogo
                onBackPressed();
            }
        });
        //Mostramos el dialogo
        dlgAcercaDe.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicion_dia);
        etFecha = findViewById(R.id.etFecha);
        btFecha = findViewById(R.id.btFecha);
        etResumen = findViewById(R.id.etResumenBreve);
        spValorarVida = findViewById(R.id.spValorarVida);
        etContenido = findViewById(R.id.etResumenGeneral);
        fabGuardar = findViewById(R.id.fabGuardar);

        ivImagenEA = findViewById(R.id.ivImagenEA);
        btImagen = findViewById(R.id.btImagen);
        clEdicionActivity = findViewById(R.id.clEdicionActivity);

        this.setTitle(getResources().getText(R.string.TituloEDA));
        //flecha en el menu para volver atras
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Por defecto mostraremos como día la fecha de hoy,por lo que la mostramos en el textView puesto para ello
        etFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        //Establecemos el adaptador del spinner de las valoraciones a un adaptador el cual obtiene los datos del archivo de recursos de array
        spValorarVida.setAdapter(ArrayAdapter.createFromResource(this, R.array.valores, android.R.layout.simple_spinner_dropdown_item));
        //Establecemos la posición por defecto del spinner por la que viene dada en la constante
        spValorarVida.setSelection(VALORACION_POR_DEFECTO);

        //Le establecemos un click al boton fecha para que habra el Dialogo de fecha
        btFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), getResources().getString(R.string.fecha));
            }
        });

        //Metodo del boton guardar, para guardar y mandar la nueva entrada del diario al MainActivity
        fabGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Quitamos los espacios de los EditText del resumen y el contenido
                etResumen.setText(etResumen.getText().toString().trim());
                etContenido.setText(etContenido.getText().toString().trim());
                //Miramos si estos EditText estan vacios,en cuyo caso mostraremos un dialgo indicando que los deben rellenar
                if ((getResources().getString(R.string.vacio).equals(etResumen.getText().toString())) ||
                        (getResources().getString(R.string.vacio).equals(etResumen.getText().toString()))) {
                    //Llamamos al metodo dialogoCamposIncompletos el cual genera el dialogo
                    dialogoCamposIncompletos();
                    //En caso de que ambos campos esten completos entramos en este else
                } else {
                    //Creamos un intent
                    Intent resultado = new Intent();
                    //Obtenemos la fecha del textView y le ponemos el formato que queremos
                    Date fecha = null;
                    try {
                        fecha = new SimpleDateFormat("dd/MM/yyyy").parse(etFecha.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //Metemos en el intent un nuevo DiaDiario con los datos que hay en los componentes
                    resultado.putExtra(EXTRA_DIA_A_GUARDAR, new DiaDiario(fecha, Integer.parseInt((String) spValorarVida.getSelectedItem()),
                            etResumen.getText().toString(), etContenido.getText().toString()));
                    //Establecemos el resultado como bueno y pasamos el intent
                    setResult(RESULT_OK, resultado);
                    //Terminamos la actividad
                    finish();
                }
            }

        });


        //pedimos ver si los permisos estan o no aceptados para desbloquear el boton
        if (mayRequestStoragePermission()) {
            btImagen.setEnabled(true);
        } else {
            btImagen.setEnabled(false);
        }

        //listener del boton imagen
        btImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opcionesBotonImagen();
            }
        });
    }

    /**
     * Dialogo para cuando pulses el boton de imagen te muestre las opciones, tomar foto, galeria o cancelar
     */

    private void opcionesBotonImagen() {
        //conjunto que tendra todas las opciones
        final CharSequence[] opcionesCamara = {"Tomar foto", "Elegir de galeria", "Cancelar"};
        //mostramos las opciones en un alert dialog que se mostrara en esta actividad
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //titulo
        builder.setTitle("Elige una opción");
        //opciones
        builder.setItems(opcionesCamara, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (opcionesCamara[i] == "Tomar foto") {
                    abrirCamara();
                } else if (opcionesCamara[i] == "Elegir de galeria") {
                    //Intent para abrir la aplicacion y mostrar una serie de opciones
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    //abre todas las aplicaciones que contienen imagenes en un cuadro de dialogo, al seleccionarla se guarda en SELECT_PICTURE
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                } else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    /**
     * Metodo para abrir la camara para tomar fotos y guardarlas
     */
    private void abrirCamara() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        //boleana para comprobar si el directorio existe
        boolean isDirectoryCreated = file.exists();

        //comprobar si el directorio existe o no, si no existe lo creamos
        if (!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        //Establecemos un nombre unico para cada imagen, para ello con el timestamp cogemos una fecha, hora, minutos y segundos
        //y se lo concatenamos al nombre de la imagen, de esta manera sera unico
        if (isDirectoryCreated) {
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            //donde vamos a guardar las fotos
            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);

            //intent para abrir la camara
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //abrimos un uri, le mandamos el uri del newFile
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    /**
     * guardamos los path para que no sean nulos
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    /**
     * recuperamos los path para que no sean nulos
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    /**
     * metodo para recibir la respuesta de los metodos anteriores
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    //log de información para nosotros
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });

                    //una vez tomada la foto la mostraremos en el imageView, y en la galeria
                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    ivImagenEA.setImageBitmap(bitmap);
                    break;
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    ivImagenEA.setImageURI(path);
                    break;

            }
        }
    }


    /**
     * Metodo para volver atras al pulsar sobre la flecha del menu
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    /**
     * Metodo para obtener una fecha del dialogo datepicker
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        EdicionDiaActivity.this.etFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(c.getTime()));
    }

    /**
     * Metodo para comprobar que los permisos estan dados, en caso contrario los solicita de nuevo
     */
    private boolean mayRequestStoragePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        //para saber si los permisos estan aceptados, a partir de android 6, necesitamos los dos permisos,
        // el resto se autoacepta al aceptar el WRITE_EXTERNAL_STORAGE
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;
        //Mostramos un mensaje para los permisos cuando el usuario deniega los permisos por primera vez
        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))) {
            Snackbar.make(clEdicionActivity, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }

    /**
     * Actividad resultante de aceptar o no los permisos de la camara, parecido al activity result, si acepta
     * mostramos un mensaje y habilitamos el boton, si los deniega abrimos un alert dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                btImagen.setEnabled(true);
            }
        } else {
            dialogoPermisosDenegados();
        }
    }

    /**
     * Dialogo para pedir los permisos al usuario cuando han sido denegados
     */
    private void dialogoPermisosDenegados() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //abrir la aplicacion de configuraciones justo en detalle de esta aplicacion para habilitar los permisos
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }


}
