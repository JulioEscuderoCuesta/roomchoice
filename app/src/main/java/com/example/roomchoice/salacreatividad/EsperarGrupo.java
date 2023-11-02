package com.example.roomchoice.salacreatividad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.roomchoice.LoggedInActivity;
import com.example.roomchoice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Gestiona la espera de los demás empleados tras enviar una historia
 */
public class EsperarGrupo extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference partida;
    String idPartida;
    boolean noTerminarCambios = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esperar_grupo);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = getIntent().getStringExtra("idPartida");
        partida = database.getReference().child("JugadoresEnPartida").child(idPartida);
        esperarJugadores();
    }

    /**
     * Los jugadores esperan hasta que todos los demas de a misma sala hayan enviado su historia
     */
    private void esperarJugadores() {
        partida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotJugadoresEnPartida) {
                if (noTerminarCambios) {
                    boolean hanEnviadoTodos = true;
                    for (DataSnapshot snapshotJugadoresEnPartida : dataSnapshotJugadoresEnPartida.getChildren()) {
                        if (snapshotJugadoresEnPartida.child("historia").getValue() == null) {
                            hanEnviadoTodos = false;
                            break;
                        }
                    }
                    if (hanEnviadoTodos) {
                        prepararEvaluacion();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Obtiene todos los identifcadores de los empleados en la sala salvo el suyo propio
     */
    private void prepararEvaluacion(){
        partida.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d("firebase", "Error getting data", task.getException());
                } else {
                    ArrayList<String> jugadoresEnPartidaID = new ArrayList<>();
                    String jugadorKey;
                    for (DataSnapshot dataSnapshotJugadorEnPartida : task.getResult().getChildren()) {
                        jugadorKey = dataSnapshotJugadorEnPartida.getKey();
                        if (!jugadorKey.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            jugadoresEnPartidaID.add(jugadorKey);
                    }
                    noTerminarCambios = false;
                    cambiarAEvaluar(jugadoresEnPartidaID);
                }
            }
        });
    }

    /**
     * Se cambia a la actividad puntuar texto pasando los identificadores de los demas empleados
     * en la sala
     * @param list
     */
    private void cambiarAEvaluar(ArrayList<String> list) {
        Intent intent;
        if(list.isEmpty()){
            intent = new Intent(getApplicationContext(), LoggedInActivity.class);
        }else {
            intent = new Intent(getApplicationContext(), PuntuarTexto.class);
            intent.putExtra("idPartida", idPartida);
            intent.putStringArrayListExtra("jugadoresEnPartidaIDs", list);
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

}