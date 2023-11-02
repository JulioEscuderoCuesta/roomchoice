package com.example.roomchoice.salaaptitudes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.roomchoice.CreacionSala;
import com.example.roomchoice.R;
import com.example.roomchoice.modelo.Estado;
import com.example.roomchoice.modelo.JuegoSalaAptitudes;
import com.example.roomchoice.salaaptitudes.juego1.DecidirQuienExpone;
import com.example.roomchoice.salaaptitudes.juego2.LeerTextoSalaAptitudes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * La clase EsperarJuegoSalaAptitudes proporciona una interfaz de espera para los jugadores
 * hasta que el empleado de recursos humanos decida qué modalidad jugar en la sala aptitudes.
 * @author
 * @author
 * @author
 * @author
 */
public class EsperarJuegoSalaAptitudes extends AppCompatActivity {

    private String idPartida;
    private boolean inicio;
    private String roomCode;

    private FirebaseDatabase database;
    private DatabaseReference partida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esperar_juego_sala_aptitudes);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");

        idPartida = getIntent().getStringExtra("idPartida");
        inicio = getIntent().getBooleanExtra("juegoTerminado", true);
        roomCode = getIntent().getStringExtra("roomCode");
        esperarSiguienteJuego();

    }

    /* Los jugadores avanzan a la modalidad seleccionada por el empleado de recursos humanos.
     * Si este decide volver hacia atrás, los jugadores también vuelven hacia atrás. */
    private void esperarSiguienteJuego() {
        partida = database.getReference().child("Partidas").child(idPartida);
        partida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(inicio) {
                    Intent intent = null;
                    Log.d("ID partida", idPartida);
                    //Si la partida está finalizada, se va a la sala previa
                    if (snapshot.child("estado").getValue().equals(Estado.FINALIZADA) || snapshot.child("estado").getValue().equals(Estado.CREADA)) {
                        intent = new Intent(getApplicationContext(), CreacionSala.class);
                        intent.putExtra("roomCode", roomCode);
                        startActivity(intent);
                    } else {
                        //Si no está finalizada, se mira a ver a qué sala se avanza
                        if (snapshot.child("juegoSalaAptitudes").getValue() != null) {
                            if (snapshot.child("juegoSalaAptitudes").getValue().equals(JuegoSalaAptitudes.EXPOSICIONTEXTO.toString())) {
                                intent = new Intent(getApplicationContext(), DecidirQuienExpone.class);
                                intent.putExtra("juego", 1);
                                intent.putExtra("correoEnviado", false);
                            } else if (snapshot.child("juegoSalaAptitudes").getValue().equals(JuegoSalaAptitudes.LECTURATEXTO.toString()))
                                intent = new Intent(getApplicationContext(), LeerTextoSalaAptitudes.class);
                            intent.putExtra("roomCode", roomCode);
                            intent.putExtra("idPartida", idPartida);
                            startActivity(intent);
                        }
                    }
                }
                else{
                    inicio = true;
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