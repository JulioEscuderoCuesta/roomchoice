package com.example.roomchoice.salaaptitudes.juego2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
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
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;

/**
 * La clase LeerTextoSalaAptitudes primero muestra una pantalla en la que se explica en qué va a
 * consistir la modalida que se va a jugar y luego carga el juego.
 * @author
 * @author
 * @author
 * @author
 */
public class LeerTextoSalaAptitudes extends AppCompatActivity {

    private static final long TIEMPOLEERTEXTO = 120000; //2 minutos en milisegundos
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    //temporizador
    private CountDownTimer temporizador;

    //pantalla_previa_juego2_leer_texto_sala_aptitudes
    private TextView tituloPantallaPreviaJuego2LeerTextoSalaAptitudes;
    private TextView avisoEmpezarALeer;
    private Button listoButton;
    private ProgressBar previaJuego2;
    private TextView textoProgressBar;

    //activity_leer_texto_juego2_sala_aptitudes
    private TextView temporizadorJuegoLeerTextoSalaAptitudes;
    private TextView tituloTextoJuego2;
    private TextView textoJuego2;

    private String idPartida;
    private int textoNumero;
    private String roomCode;
    private FirebaseDatabase database;
    private DatabaseReference jugadorListo;
    private DatabaseReference tiempoLectura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_previa_juego2_leer_texto_sala_aptitudes);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = this.getIntent().getStringExtra("idPartida");
        tituloPantallaPreviaJuego2LeerTextoSalaAptitudes = findViewById(R.id.tituloPantallaPreviaJuego2LeerTextoSalaAptitudes);
        roomCode = getIntent().getStringExtra("roomCode");
        avisoEmpezarALeer = findViewById(R.id.avisoEmpezarALeer);
        listoButton = findViewById(R.id.listoButton);
        previaJuego2 = findViewById(R.id.progressBarPreviaJuego2);
        textoProgressBar = findViewById(R.id.textoProgressBar);
        previaJuego2.setVisibility(View.GONE);
        textoProgressBar.setVisibility(View.GONE);
    }

    /**
     * Registra el evento de pulsar el botón "Listo" en activity_pantalla_previa_juego2_leer_texto_sala_aptitudes.xml
     * Marca al jugador como preparado para jugar y pasa a esperar al resto,
     * @param view El botón pulsado.
     */
    public void onClickComenzarALeer(View view) {
        jugadorListo = database.getReference().child("JugadoresEnPartida").child(idPartida).child(USER.getUid());
        HashMap<String,Object> jugadorListoHashMap = new HashMap<>();
        jugadorListoHashMap.put("listoParaLeer", true);
        jugadorListo.updateChildren(jugadorListoHashMap);
        esperarRestoJugadores();
    }

    /**
     * Registra el evento de pulsar el botón "Terminar" en activity_leer_texto_juego2_sala_aptitudes.xml
     * Calcula el tiempo que ha tardado el usuario en leer el texto en segundos y procede a cargar el
     * test.
     * @param view El botón pulsado.
     */
    public void onClickTerminarLeerTextoJuego2(View view) {
        String temporizador = temporizadorJuegoLeerTextoSalaAptitudes.getText().toString();

        int segundosTemporizador = Integer.valueOf(temporizador.substring(3, 5));
        int minutosTemporizador = Integer.valueOf(String.valueOf(temporizador.charAt(1)));

        //Calcular los milisegundos a partir del tiempo que marca el temporizador.
        long milisegundosEnLeer = TIEMPOLEERTEXTO - (segundosTemporizador * 1000 + minutosTemporizador * 1000 * 60);

        //Pasar los milisegundos a segundos
        long segundosLeerTexto = milisegundosEnLeer / 1000;

        //Meter el tiempo en la base de datos
        HashMap<String, Object> tiempo = new HashMap<>();
        tiempo.put("tiempoLeerTexto", segundosLeerTexto);
        tiempoLectura = database.getReference().child("Puntuaciones").child(idPartida).child(USER.getUid()).child("LeerTextoSalaAptitudes");
        tiempoLectura.updateChildren(tiempo);

        //Pasar a hacer el test
        Intent intent = new Intent(getApplicationContext(), TestJuego2SalaAptitudes.class);
        intent.putExtra("roomCode", roomCode);
        intent.putExtra("idPartida", idPartida);
        intent.putExtra("texto", textoNumero);
        startActivity(intent);

    }

    //Se escoge un texto aleatorio
    private void seleccionarTexto() {
        Random r = new Random();
        textoNumero = 1 + r.nextInt(3);
        switch (textoNumero) {
            case 1:
                tituloTextoJuego2.setText(R.string.tituloTexto1Juego2);
                textoJuego2.setText(R.string.texto1Juego2);
                break;
            case 2:
                tituloTextoJuego2.setText(R.string.tituloTexto2Juego2);
                textoJuego2.setText(R.string.texto2Juego2);
                break;
            case 3:
                tituloTextoJuego2.setText(R.string.tituloTexto3Juego2);
                textoJuego2.setText(R.string.texto3Juego2);
                break;
            case 4:
                tituloTextoJuego2.setText(R.string.tituloTexto4Juego2);
                textoJuego2.setText(R.string.texto4Juego2);
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
                temporizadorJuegoLeerTextoSalaAptitudes.setText(minutosAMostrar + ":" + segundosAMostrar);
            }

            @Override
            public void onFinish() {
                Toast.makeText(LeerTextoSalaAptitudes.this, "Se acabó el tiempo", Toast.LENGTH_SHORT).show();
                temporizadorJuegoLeerTextoSalaAptitudes.setText("0:00");
                textoJuego2.setVisibility(View.GONE);
            }
        }.start();
    }

    //Se espera a que el resto de jugadores esté listo para comenzar la partida
    private void esperarRestoJugadores() {
        tituloPantallaPreviaJuego2LeerTextoSalaAptitudes.setVisibility(View.GONE);
        avisoEmpezarALeer.setVisibility(View.GONE);
        listoButton.setVisibility(View.GONE);
        previaJuego2.setVisibility(View.VISIBLE);
        textoProgressBar.setVisibility(View.VISIBLE);
        jugadorListo.getParent().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotDataChange) {
                HashMap<String,Object> jugadorHashMap;
                int jugadoresListos = 0;
                for(DataSnapshot jugador: snapshotDataChange.getChildren()) {
                    for(DataSnapshot s: jugador.getChildren()){
                        if(s.getKey().equals("listoParaLeer") && s.getValue(Boolean.class).equals(true)){
                            jugadoresListos++;
                        }
                    }
                }
                if(jugadoresListos == snapshotDataChange.getChildrenCount()) {
                    setContentView(R.layout.activity_leer_texto_juego2_sala_aptitudes);
                    temporizadorJuegoLeerTextoSalaAptitudes = findViewById(R.id.temporizadorJuegoLeerTextoSalaAptitudes);
                    tituloTextoJuego2 = findViewById(R.id.tituloTextoJuego2);
                    textoJuego2 = findViewById(R.id.textoJuego2);
                    textoJuego2.setMovementMethod(new ScrollingMovementMethod());
                    seleccionarTexto();
                    iniciarTemporizador(TIEMPOLEERTEXTO);
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