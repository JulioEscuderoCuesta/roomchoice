package com.example.roomchoice;

import com.example.roomchoice.modelo.Estado;
import com.example.roomchoice.modelo.JugadorEnPartida;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * La clase UnirASala permite al usuario de tipo empleado unirse a una partida mediante un código
 * de partida.
 * @author
 * @author
 * @author
 * @author
 */
public class UnirASala extends AppCompatActivity {

    private EditText editText;
    String roomCode = "";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database;
    DatabaseReference partidas;
    DatabaseReference jugadoresEnPartida;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_unir_asala);
        editText = findViewById(R.id.codeText);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");


    }

    /**
     * Registra el evento de pulsar el botón "Acceder" en activity_creacion_sala.xml
     * Obtiene el código introducido y procede a buscar la partida en la base de datos
     * @param view El botón pulsado
     */
    public void joinRoom(View view) {
        roomCode = editText.getText().toString();
        editText.setText("");
        buscarPartida();

    }

    /* Busca en las partidas de la base de datos si el código introducido por el
     * usuario que se quiere unir coincide con el código de alguna partida.
     * Si es así, procede a añadir el jugador a la partida. */
    private void buscarPartida() {
        partidas = database.getReference().child("Partidas");
        partidas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                boolean partidaExiste = false;
                for(DataSnapshot singleSnapshot : snapshot1.getChildren()){
                    String codigo = singleSnapshot.child("codigo").getValue(String.class);
                    if(codigo.equals(roomCode)) {
                        partidaExiste = true;
                        comprobarEstadoPartida(singleSnapshot);
                    }
                }
                if(!partidaExiste)
                    Toast.makeText(getApplicationContext(), "No existe una partida con el código introducido", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO
            }
        });

        }

    /* Comprueba si la partida no está completa o si no ha finalizado.
     * Si ambas se cumplen, procede a añadir un jugador a la partida. */
    private void comprobarEstadoPartida(DataSnapshot partida){
        jugadoresEnPartida = database.getReference().child("JugadoresEnPartida");
        jugadoresEnPartida.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot partidasJugSnap) {
                if(partida.child("maxParticipantes").getValue(Integer.class).equals(partida.child("jugActuales").getValue(Integer.class)))
                    Toast.makeText(getApplicationContext(), "La sala a la que ha intentado unirse está llena", Toast.LENGTH_SHORT).show();
                else if(partida.child("estado").equals(Estado.FINALIZADA))
                    Toast.makeText(getApplicationContext(), "Se produjo un error. Pruebe otra partida", Toast.LENGTH_SHORT).show();
                else
                    annadirJugador(partida);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Añade 1 al número de jugadores que se encuentran en la partida y además,
     * introduce los datos necesarios del usuario en la partida en juego.
     * También comienza la actividad con la clase CreacionSala. */
    private void annadirJugador(DataSnapshot partida){
        partidas.child(partida.getKey()).child("jugActuales").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) currentData.setValue(1);
                else{
                    int numJug = currentData.getValue(Integer.class);
                    currentData.setValue(numJug + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                JugadorEnPartida jugadorEnPartida = new JugadorEnPartida(user.getEmail(), false, false, false, null, user.getDisplayName());
                Map<String, Object> postValues = jugadorEnPartida.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/JugadoresEnPartida/" + partida.getKey() +"/"+user.getUid(), postValues);

                database.getReference().updateChildren(childUpdates);

                Intent intent = new Intent(getApplicationContext(), CreacionSala.class);
                intent.putExtra("roomCode", roomCode);
                intent.putExtra("idPartida", partida.getKey());
                intent.putExtra("esRRHH", getIntent().getBooleanExtra("esRRHH", false));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), LoggedInActivity.class));
    }

}
