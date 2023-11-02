package com.example.roomchoice.salaaptitudes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.roomchoice.LoggedInActivity;
import com.example.roomchoice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * La clase EnviarCorreoSalaAptitudes permite al usuario enviar un correo electrónico con la
 * puntuación obtenida en la sala y con un texto explicando el funcionamiento de la modalidad jugada.
 */
public class EnviarCorreoSalaAptitudes extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference puntuacionJugador;
    private Intent intent;
    private FirebaseUser user;
    private double puntuacion;

    private String roomCode;
    private String idPartida;
    private int juego;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_correo_sala_aptitudes);

        user = FirebaseAuth.getInstance().getCurrentUser();
        intent = getIntent();
        roomCode = intent.getStringExtra("roomCode");
        idPartida = intent.getStringExtra("idPartida");
        juego = intent.getIntExtra("juego", 1);
        Log.d("juegoEnviarAntes", String.valueOf(juego));

        obtenerPuntuacion();
    }

    private void obtenerPuntuacion(){
        Log.d("juegoEnviarCorreo", String.valueOf(juego));
        if(juego == 1){
            puntuacionJugador = database.getReference().child("Puntuaciones").child(idPartida).child(user.getUid()).child("HacerExposicionSalaAptitudes");
        }
        else{
            puntuacionJugador = database.getReference().child("Puntuaciones").child(idPartida).child(user.getUid()).child("LeerTextoSalaAptitudes");
        }
        puntuacionJugador.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("ruta", puntuacionJugador.toString());
                Log.d("resultado", task.getResult().toString());
                Log.d("resultado2", task.getResult().child("puntuacion").toString());
                if(task.isSuccessful()) {
                    puntuacion = task.getResult().child("puntuacion").getValue(Double.class);
                }
            }
        });
    }

    /**
     * Registra el evento de pulsar el botón "Enviar email" en activity_enviar_correo_sala_aptitudes.xml
     * Selecciona una clasificación (de muy baja a muy alta) dependiendo de la puntuación obtenida y abre
     * el cliente del correo.
     * @param view El botón pulsado
     */
    public void onClickSendEmail(View view){
        int puntInteger = (int)Math.round(puntuacion);
        String nota = "";
        switch (puntInteger){
            case 1:
                nota = "muy baja";
                break;
            case 2:
                nota = "baja";
                break;
            case 3:
                nota = "media";
                break;
            case 4:
                nota = "alta";
                break;
            case 5:
                nota = "muy alta";
                break;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_CC, user.getEmail());
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Results from evaluation of RoomChoice from user " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String textoCorreo = generarInforme(nota);
        emailIntent.putExtra(Intent.EXTRA_TEXT, textoCorreo);
        try {
            startActivity(Intent.createChooser(emailIntent, "Enviando email..."));
            Toast.makeText(getApplicationContext(), "Email enviado", Toast.LENGTH_SHORT).show();
            /*finish();*/
            Log.d("finish", "he hecho el finish");
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No email client installed", Toast.LENGTH_SHORT).show();
        }

    }

    //Dependiendo de la modadlidad jugada, se genera un informe u otro.
    private String generarInforme(String nota) {
        String texto;
        SimpleDateFormat formato = new SimpleDateFormat("MM/dd/yyyy");
        if(juego == 1) {
            texto = "Juego 1: Exposicion de texto\n" +
                    "En este juego analizamos la habilidad oral del jugador. Para ello, se tiene en cuenta la expresión oral, es decir, el conjunto de técnicas que determinan las pautas generales que deben seguirse para comunicarse oralmente de manera efectiva. El mensaje transmitido tiene que ser fluido, explicado con claridad, coherencia y con un vocabulario que sea entendible para el resto de usuarios.\n" +
                    "\n" +
                    "La puntuación de la comprensión lectora se valora con muy baja, baja, media, alta o muy alta.\n" +
                    "\n" +
                    "La nota obtenida por el empleado en la lectura de texto es de: " + puntuacion + ", lo que equivaldría a una puntuación de la comprensión lectora: "+ nota + ".\n";
        } else {
            texto = "INFORME FINAL\n" +
                    "Este es el informe de resultados del empleado " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + " obtenido en la app ROOMCHOICE el" + formato.format(new Date()) + "en el que se valoran ciertas capacidades y habilidades relevantes en el mundo laboral.\n" +
                    "Los resultados obtenidos en cada uno de los aspectos analizados son:\n" +
                    "Juego 2: Lectura de texto:\n" +
                    "Se va a valorar la comprensión lectora que se define como la habilidad para entender, evaluar, utilizar e implicarse con textos escritos, participar en la sociedad, alcanzar las metas propuestas y desarrollar el mayor conocimiento y potencial posibles. El análisis de la capacidad de comprensión lectora por parte del jugador ha sido estudiada con la lectura de un texto por parte del jugador realizando posteriormente preguntas sobre dicho texto.\n" +
                    "\n" +
                    "La puntuación de la comprensión lectora se valora con muy baja, baja, media, alta o muy alta.\n" +
                    "\n" +
                    "La nota obtenida por el empleado en la lectura de texto es de "+puntuacion+", lo que equivaldría a una puntuación de la comprensión lectora: "+ nota+".\n";
        }
        return texto;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere salir de la partida?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), LoggedInActivity.class);
                i.putExtra("roomCode", roomCode);
                i.putExtra("idPartida", idPartida);
                i.putExtra("inicio", false);
                startActivity(i);
            }}).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }});
        builder.show();
    }
}