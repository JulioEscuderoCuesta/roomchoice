package com.example.roomchoice.salaaptitudes.juego1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomchoice.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * La clase HacerExposicionSalaAptitudes muestra primero una pantalla en la
 * que se explica brevemente qué va a realizar el jugador y luego muestra
 * el texto sobre el que el jugador realizará la exposición y le permite comenzarla.
 * @author
 * @author
 * @author
 * @author
 */
public class HacerExposicionSalaAptitudes extends AppCompatActivity {

    private static final long  TIEMPOINICIAL = 600000; //10 minutos en milisegundos
    private static final long TIEMPOEXPOSICION = 90000; //1 minuto y medio en milisegundos


    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference actualizarJugadorHaExpuesto;
    private DatabaseReference pregunta1;
    private DatabaseReference jugadorHaSidoEvaluado;
    private StorageReference exposicion;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private String idPartida;
    private long numJugadores;
    private String roomCode;
    private int texto;

    //temporizador
    private CountDownTimer temporizador;

    private TextView tituloJuegoTexto;

    //Elementos de activity_exposicion_texto LEER
    private TextView temporizadorPrepararExposicion;
    private TextView textoExposicion;
    private TextView tituloTextoJuego1;
    private Button terminarLeerButton;

    //Elementos de activity_exposicion_texto EXPONER
    private Button terminarGrabarButton;
    private MediaRecorder grabadora;
    private String outputFile = null;

    private ProgressBar progressBarExposicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        storage = FirebaseStorage.getInstance("gs://roomchoice-2f8fd.appspot.com/");
        idPartida = this.getIntent().getStringExtra("idPartida");
        numJugadores = this.getIntent().getLongExtra("numJugadores", 0);
        roomCode = this.getIntent().getStringExtra("roomCode");
        texto = this.getIntent().getIntExtra("texto", 0);
        setContentView(R.layout.pantalla_previa_juego1_leer_texto_sala_aptitudes);
    }

    /**
     * Registra el evento de pulsar el botón "Listo" en activity_pantalla_previa_juego1_leer_texto_sala_aptitudes.xml
     * Carga un nuevo layout en el que se leerá el texto de la exposición.
     * @param view El botón pulsado.
     */
    public void onClickEmpezarALeerExposicionButton(View view) {
        setContentView(R.layout.activity_exposicion_texto);
        tituloJuegoTexto = findViewById(R.id.tituloJuegoTexto);
        tituloTextoJuego1 = findViewById(R.id.tituloTextoJuego1);
        textoExposicion = findViewById(R.id.textoExposicion);
        temporizadorPrepararExposicion = findViewById(R.id.temporizadorPrepararExposicion);
        terminarLeerButton = findViewById(R.id.terminarLeerButton);
        terminarGrabarButton = findViewById(R.id.terminarGrabarButton);
        progressBarExposicion = findViewById(R.id.progressBarExposicion);
        seleccionarTexto();
        progressBarExposicion.setVisibility(View.GONE);
        terminarGrabarButton.setVisibility(View.GONE);
        textoExposicion.setMovementMethod(new ScrollingMovementMethod());
        iniciarTemporizador(TIEMPOINICIAL);
    }

    /**
     * Registra el evento de pulsar el botón "Listo" en activity_pantalla_exposicion_texto.xml
     * Detiene le temporizador de lectura y carga un nuevo layout en el que se le indicará
     * al jugador que a continuación tendrá que exponer.
     * @param view El botón pulsado.
     */
    public void onClickTerminarLeer(View view) {
        temporizador.cancel();
        setContentView(R.layout.pantalla_previa_exponer);
    }

    /**
     * Registra el evento de pulsar el botón "Listo" en activity_pantalla_exposicion_texto.xml
     * Detiene le temporizador de lectura y carga un nuevo layout en el que se le indicará
     * al jugador que a continuación tendrá que exponer.
     * @param view El botón pulsado.
     */
    public void onClickEmpezarAExponerButton(View view) {
        setContentView(R.layout.activity_exposicion_texto);
        tituloJuegoTexto = findViewById(R.id.tituloJuegoTexto);
        textoExposicion = findViewById(R.id.textoExposicion);
        temporizadorPrepararExposicion = findViewById(R.id.temporizadorPrepararExposicion);
        terminarLeerButton = findViewById(R.id.terminarLeerButton);
        terminarGrabarButton = findViewById(R.id.terminarGrabarButton);
        progressBarExposicion = findViewById(R.id.progressBarExposicion);
        textoExposicion.setVisibility(View.GONE);
        tituloTextoJuego1.setVisibility(View.GONE);
        terminarLeerButton.setVisibility(View.GONE);
        progressBarExposicion.setVisibility(View.INVISIBLE);
        terminarGrabarButton.setVisibility(View.VISIBLE);

        hacerExposicion();
    }

    /**
     * Registra el evento de pulsar el botón "Terminar" en activity_pantalla_exposicion_texto.xml
     * Detiene le temporizador de exposición, detiene la grabación de voz y procede a subir la
     * exposición.
     * @param view El botón pulsado.
     */
    public void onClickTerminarGrabar(View view) {
        temporizador.cancel();
        if (grabadora != null) {
            grabadora.stop();
            grabadora.release();
            grabadora = null;
            Toast.makeText(getApplicationContext(), "Exposición finalizada", Toast.LENGTH_SHORT).show();
        }
        tituloJuegoTexto.setText("Esperando evaluaciones...");
        temporizadorPrepararExposicion.setVisibility(View.GONE);
        terminarGrabarButton.setVisibility(View.GONE);
        tituloTextoJuego1.setVisibility(View.GONE);
        progressBarExposicion.setVisibility(View.VISIBLE);
        subirExposicion();
    }

    //Se asigna el texto según el número de texto escogido de manera aleatoria previamente
    private void seleccionarTexto() {
        switch (texto) {
            case 1:
                tituloTextoJuego1.setText(R.string.tituloTexto1Juego1);
                textoExposicion.setText(R.string.texto1Juego1);
                break;
            case 2:
                tituloTextoJuego1.setText(R.string.tituloTexto2Juego1);
                textoExposicion.setText(R.string.texto2Juego1);
                break;
            case 3:
                tituloTextoJuego1.setText(R.string.tituloTexto3Juego1);
                textoExposicion.setText(R.string.texto3Juego1);
                break;
            case 4:
                tituloTextoJuego1.setText(R.string.tituloTexto4Juego1);
                textoExposicion.setText(R.string.texto4Juego1);
                break;
        }
    }


    //Método para setear el temporizador
    private void iniciarTemporizador(long tiempo) {
        temporizador = new CountDownTimer(tiempo, 1000) {
            @Override
            public void onTick(long l) {
                long tiempo = l / 1000;
                int minutos = (int) tiempo / 60;
                int segundos = (int) tiempo % 60;
                String minutosAMostrar = String.format("%02d", minutos);
                String segundosAMostrar = String.format("%02d", segundos);
                temporizadorPrepararExposicion.setText(minutosAMostrar + ":" + segundosAMostrar);
            }

            @Override
            public void onFinish() {
                Toast.makeText(HacerExposicionSalaAptitudes.this, "Se acabó el tiempo", Toast.LENGTH_SHORT).show();
                temporizadorPrepararExposicion.setText("0:00");
                textoExposicion.setVisibility(View.GONE);
            }
        }.start();
    }

    //Método para grabar la exposición
    private void hacerExposicion() {
        outputFile = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() +  "/exposicion.mp4 ";
        //outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/exposicion.mp3 ";
        grabadora = new MediaRecorder();
        grabadora.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        grabadora.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        grabadora.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        grabadora.setOutputFile(outputFile);

        try {
            grabadora.prepare();
            Toast.makeText(getApplicationContext(), "Grabando exposición", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        grabadora.start();
        iniciarTemporizador(TIEMPOEXPOSICION);
    }

    //Método para subir la exposición a la BD
    private void subirExposicion() {

        exposicion = storage.getReference().child("ExposicionesSalaAptitudes").child(idPartida).child(user.getUid()).child("exposicion.mp4");
        Uri uri = Uri.fromFile(new File(outputFile));
        try {
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("mp4").build();
            exposicion.putFile(uri,metadata).addOnSuccessListener(taskSnapshot ->
                    Toast.makeText(getApplicationContext(), "Exposición subida", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Se señala en la BD que el jugador ha expuesto.
        HashMap<String, Object> jugadorHaExpuesto = new HashMap<>();
        jugadorHaExpuesto.put("haExpuesto", true);
        actualizarJugadorHaExpuesto = database.getReference().child("JugadoresEnPartida").child(idPartida).child(user.getUid());        actualizarJugadorHaExpuesto.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if(currentData.getValue() != null)
                    actualizarJugadorHaExpuesto.updateChildren(jugadorHaExpuesto);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                actualizarJugadorHaExpuesto.child("textoExposicion").setValue(texto);
                esperarEvaluaciones();
            }
        });

    }

    /*Cuando el que expone termina de grabar, se queda esperando a que todos los demás jugadores le evalúen.
      Cuando esto suceda, pasará a estar a la espera a que el siguiente jugador realice la exposición*/
    private void esperarEvaluaciones() {
        pregunta1 = database.getReference().child("Puntuaciones").child(idPartida).child(user.getUid()).child("HacerExposicionSalaAptitudes");
        pregunta1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot s : snapshot.getChildren()){
                    if (s.getKey().equals("numVecesEvaluado") && s.getValue(Integer.class) == numJugadores - 1){
                        jugadorHaSidoEvaluado = database.getReference().child("JugadoresEnPartida").child(idPartida).child(user.getUid());
                        HashMap<String, Object> jugadorEvaluado = new HashMap<>();
                        jugadorEvaluado.put("haSidoEvaluado", true);
                        jugadorHaSidoEvaluado.updateChildren(jugadorEvaluado);

                        Intent intent = new Intent(getApplicationContext(), DecidirQuienExpone.class);
                        intent.putExtra("roomCode", roomCode);
                        intent.putExtra("idPartida", idPartida);
                        intent.putExtra("juego", 1);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}

