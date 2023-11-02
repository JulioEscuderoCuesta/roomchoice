package com.example.roomchoice.salacreatividad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomchoice.LoggedInActivity;
import com.example.roomchoice.R;
import com.firebase.ui.auth.ui.email.EmailLinkCatcherActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * Gestiona la espera de que un empleado haya sido evaluado por todos los demas de la sala
 */
public class EsperandoEvaluacionCreatividad extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference jugadorEnPartida;
    long numJugadoresEnPartida;
    Button sendEmail;
    ProgressBar progress;
    String idPartida;
    int modalidad;
    FirebaseUser user;
    ImageView logoEsperandoEvaluacion;
    TextView tituloEsperandoEvaluacionCreatividad;
    double puntuacion;
    TextView consejo2TextView;

    DatabaseReference partida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esperando_evaluacion_creatividad);
    }

    @Override
    protected void onStart() {
        super.onStart();
        idPartida = getIntent().getStringExtra("idPartida");
        user = FirebaseAuth.getInstance().getCurrentUser();
        obtenerNumeroJugadores();
        tituloEsperandoEvaluacionCreatividad = findViewById(R.id.tituloEsperandoEvaluacionCreatividad);
        progress = findViewById(R.id.progressBarDecidirQuienExpone);
        sendEmail = findViewById(R.id.buttonSendEmail);
        logoEsperandoEvaluacion = findViewById(R.id.logoEsperandoEvaluacion);
        logoEsperandoEvaluacion.setVisibility(View.INVISIBLE);
        consejo2TextView = findViewById(R.id.consejo2TextView);
        consejo2TextView.setVisibility(View.GONE);
        sendEmail.setEnabled(false);

        puntuacion = 1.0;

    }

    private void obtenerModalidad(){
        partida = database.getReference().child("Partidas").child(idPartida).child("modalidad");
        partida.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String mod = task.getResult().getValue(String.class);
                switch (mod){
                    case "FWORDS":
                        modalidad = 1;
                        break;
                    case "SWORDS":
                        modalidad = 2;
                        break;
                    default:
                        modalidad = -1;
                }
                esperarEvaluacionCompleta();
            }
        });
    }

    private void obtenerNumeroJugadores(){

        database.getReference().child("JugadoresEnPartida").child(idPartida).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    numJugadoresEnPartida = task.getResult().getChildrenCount();
                    obtenerModalidad();
                }
            }
        });
    }

    /**
     * Cuando un empleado a sido evaluado por todos menos el en la sala, se habilita el boton que
     * permite enviar el correo
     */
    private void esperarEvaluacionCompleta(){
        jugadorEnPartida = database.getReference().child("JugadoresEnPartida").child(idPartida).child(user.getUid());
        jugadorEnPartida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot s : snapshot.getChildren()){
                    if (s.getKey().equals("numVecesEvaluado")){
                        if(s.getValue(Integer.class) == numJugadoresEnPartida - 1){
                            obtenerPuntuacion();
                            tituloEsperandoEvaluacionCreatividad.setVisibility(View.GONE);
                            consejo2TextView.setVisibility(View.VISIBLE);
                            logoEsperandoEvaluacion.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.INVISIBLE);
                            sendEmail.setEnabled(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void obtenerPuntuacion(){
        jugadorEnPartida.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    double puntuacionObjetiva = task.getResult().child("puntuacionObjetiva").getValue(Double.class);
                    double puntuacionSubjetiva = task.getResult().child("puntuacion").getValue(Double.class) / (numJugadoresEnPartida - 1);
                    puntuacion = puntuacionObjetiva * 0.5 + puntuacionSubjetiva * 0.5;
                }
            }
        });
    }

    public void onClickSendEmail(View view) {

        int puntInteger = (int)Math.round(puntuacion);

        String creatividad = "media";

        switch (puntInteger){
            case 1:
                creatividad = "muy baja";
                break;
            case 2:
                creatividad = "baja";
                break;
            case 3:
                creatividad = "media";
                break;
            case 4:
                creatividad = "alta";
                break;
            case 5:
                creatividad = "muy alta";
                break;
        }

        String from = user.getEmail();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_CC, from);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Results from evaluation of RoomChoice from user " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        SimpleDateFormat formato = new SimpleDateFormat("MM/dd/yyyy");
        String textoCorreo = "INFORME FINAL \n"+
                "Este es el informe de resultados del empleado" + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "obtenido en la app ROOMCHOICE el" + formato.format(new Date()) + "en el que se valoran ciertas capacidades y habilidades relevantes en el mundo laboral.\n" +
                "Los resultados obtenidos en cada uno de los aspectos analizados son:\n" +
                "\n" +
                "Sala de creatividad: estudiada a través del juego: Creación de historias\n" +
                "Modalidad utilizada " + modalidad + "\n" +
                "\n" +
                "Modalidad 1: El jugador recibe 5 palabras en pantalla las cuales debe utilizar para elaborar una historia.\n" +
                "\n" +
                "Se estudia en este caso la creatividad narrativa, es decir, la capacidad que tiene una persona para crear y contar historias. \n" +
                "Aquella persona que sea creativa, tendrá un estilo cognitivo productivo, que plantea problemas y soluciones, así como la capacidad de sintetizar ideas en distintas combinaciones y el conocimiento como forma de generar nuevos aportes. Todo ello desemboca en una mayor flexibilidad en la forma de pensar, considerada como la capacidad de establecer distintas formas de plantear una situación, que en nuestro caso es, con las palabras ofrecidas al principio del juego.\n" +
                "\n" +
                "La puntuación de la creatividad narrativa se valora con muy baja, baja, media, alta o muy alta. \n" +
                "\n" +
                "\n" +
                "Modalidad 2: El jugador recibe palabras de forma secuencial de tal forma que deberá incluirlas en una historia, cada vez que logra incluir una palabra, le aparecerá otra en pantalla, hasta un total de 5.\n" +
                "\n" +
                "Se estudia la creatividad bisociativa, que se produce cuando nuestra mente racional consciente conecta pensamientos racionales con otros más intuitivos, este tipo de creatividad se asocia con la capacidad de relacionar conceptos, tener una mente despierta y abierta a ideas. \n" +
                "\n" +
                "La creatividad bisociativa está basada en la dinámica de las tres F de:\n" +
                " \n" +
                "Fluidez: Es más productivo tener muchas ideas sin pulir que pocas «buenas» ideas porque entre mayor sea la diversidad de ideas, es mayor el rango de posibles soluciones.\n" +
                " \n" +
                "Flexibilidad: A menudo tenemos la idea «correcta» pero la hemos colocado en el lugar «equivocado», así que tenemos que moverla por ahí para ver donde se ajusta mejor para cumplir con nuestros desafíos.\n" +
                " \n" +
                "Flujo: Tenemos que estar tanto estimulados como relajados para extraer la energía necesaria para crear. Las ideas se vuelcan sin problemas cuando comenzamos a disfrutarlo\n" +
                "La puntuación de la creatividad bisociativa se valora con muy baja, baja, media, alta o muy alta. \n" +
                "\n" +
                "La nota obtenida por el empleado en la Modalidad " + modalidad + " es de " + puntuacion + ", lo que equivaldría a una puntuación de la creatividad " + ((modalidad == 1) ? "narrativa" : "bisociativa") + ": " + creatividad + ".\n";


        emailIntent.putExtra(Intent.EXTRA_TEXT, textoCorreo);
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No email client installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere salir de la partida?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), LoggedInActivity.class);
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