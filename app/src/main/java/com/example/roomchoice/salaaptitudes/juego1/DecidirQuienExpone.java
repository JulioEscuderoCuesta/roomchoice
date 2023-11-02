package com.example.roomchoice.salaaptitudes.juego1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.roomchoice.R;
import com.example.roomchoice.salaaptitudes.EnviarCorreoSalaAptitudes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * La clase DecidirQuienExpone
 * @author
 * @author
 * @author
 * @author
 */
public class   DecidirQuienExpone extends AppCompatActivity {

    private String idPartida;
    private boolean tieneQueExponer;
    private String idJugadorQueExpone;
    private long numJugadores;
    private String roomCode;

    private FirebaseDatabase database;
    private DatabaseReference avanzarJuego;
    private DatabaseReference partidaEnJuego;
    private DatabaseReference prueba;
    private FirebaseUser user;

    private int juego;
    private ArrayList<String> listaJugadoresID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decidir_quien_expone);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        user = FirebaseAuth.getInstance().getCurrentUser();
        idPartida = this.getIntent().getStringExtra("idPartida");
        roomCode = this.getIntent().getStringExtra("roomCode");
        juego = this.getIntent().getIntExtra("juego", -1);

        comprobarEstadoPartida();
    }

    /* Se comprueba cuántos jugadores han sido evaluados.
     * Si todos han sido evaluados, todos avanzan a la actividad de enviar el
     * correo con los resultados. */
    private void comprobarEstadoPartida() {
        avanzarJuego = database.getReference().child("JugadoresEnPartida").child(idPartida);
        avanzarJuego.addListenerForSingleValueEvent(new ValueEventListener() {
            int evaluados;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotAvanzarJuego) {
                evaluados = 0;
                HashMap<String, Object> atributosJugador;
                for(DataSnapshot snapshotComprobarAvanzarJuego: snapshotAvanzarJuego.getChildren()) {
                    atributosJugador = (HashMap<String, Object> )snapshotComprobarAvanzarJuego.getValue();
                    if(atributosJugador.get("haSidoEvaluado").equals(true))
                        evaluados++;
                }
                if(evaluados == snapshotAvanzarJuego.getChildrenCount()) {
                    Intent intent = new Intent(getApplicationContext(), EnviarCorreoSalaAptitudes.class);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("idPartida", idPartida);
                    intent.putExtra("juego", juego);
                    startActivity(intent);

                } else {
                    establecerPantallas();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Se busca a qué jugador le toca realizar la exposición y dicho jugador avanza a la actividad
     * de HacerExposicionSalaAptitudes. Para ello se comprueba que el jugador actual no ha expuesto y
     * que el usuario anterior a él ya ha sido evaluado. Si el jugador actual coincide con el usuario
     * que está ahora mismo en la aplicación, este pasa a exponer.
     * El resto avanza a EscucharExposicionSalaAptitudes. */
    private void establecerPantallas() {
        int texto = seleccionarTexto();
        partidaEnJuego = database.getReference().child("JugadoresEnPartida").child(idPartida);
        partidaEnJuego.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotJugadoresEnPartida) {
                HashMap<String, Object> atributosJugadorActual;
                HashMap<String, Object> atributosJugadorAnterior = new HashMap<>();
                int contador = 0;
                numJugadores = snapshotJugadoresEnPartida.getChildrenCount();
                for (DataSnapshot jugadoresSnapshot : snapshotJugadoresEnPartida.getChildren()) {
                    atributosJugadorActual = (HashMap<String, Object>) jugadoresSnapshot.getValue(); //Coger un jugador
                    if (atributosJugadorActual.get("haExpuesto").equals(false) && (contador == 0 || atributosJugadorAnterior.get("haSidoEvaluado").equals(true))) {
                        idJugadorQueExpone = jugadoresSnapshot.getKey();
                        if (atributosJugadorActual.get("email").equals(user.getEmail())) {
                            tieneQueExponer = true;
                            Intent intent = new Intent(getApplicationContext(), HacerExposicionSalaAptitudes.class);
                            intent.putExtra("texto", texto);
                            intent.putExtra("roomCode", roomCode);
                            intent.putExtra("idPartida", idPartida);
                            intent.putExtra("numJugadores", numJugadores);
                            startActivity(intent);
                        }

                    }
                    contador++;
                    atributosJugadorAnterior = atributosJugadorActual;
                }

                if (!tieneQueExponer) {
                    Intent intent = new Intent(getApplicationContext(), EscucharExposicionSalaAptitudes.class);
                    intent.putExtra("texto", texto);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("idPartida", idPartida);
                    intent.putExtra("idJugadorQueExpone", idJugadorQueExpone);
                    startActivity(intent);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /* Se selecciona un texto aleatorio sobre el que se realizará la exposición y sobre
     * el que se basará las preguntas del test */
    private int seleccionarTexto() {
        Random r = new Random();
        int textoNumero = 1 + r.nextInt(3);
        return textoNumero;
    }

    @Override
    public void onBackPressed() {

    }
}