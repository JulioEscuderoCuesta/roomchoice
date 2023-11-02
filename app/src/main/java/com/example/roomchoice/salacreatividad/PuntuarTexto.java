package com.example.roomchoice.salacreatividad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.roomchoice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;

/**
 * Obtiene la puntuacion subjetiva dadas unos criterios de evauluacion sobre la creatividad
 */
public class PuntuarTexto extends AppCompatActivity {

    TextView historia;
    TextView palabras;
    SeekBar puntuadorPalabras;
    SeekBar puntuadorCohesion;
    SeekBar puntuadorFluidez;
    SeekBar puntuadorNaturalidad;

    FirebaseDatabase database;
    DatabaseReference partida;
    ArrayList<String> jugadoresEnPartidaIDs;
    String idPartida;
    String usuarioAEvaluarID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puntuar_texto);

        historia = (TextView) findViewById(R.id.textViewHistoriaAEvaluar);
        palabras = (TextView) findViewById(R.id.palabras);
        puntuadorPalabras = (SeekBar) findViewById(R.id.seekbarPuntuacionPalabras);
        puntuadorCohesion = (SeekBar) findViewById(R.id.seekbarPuntuacionCohesion);
        puntuadorFluidez = (SeekBar) findViewById(R.id.seekbarPuntuacionFluidez);
        puntuadorNaturalidad = (SeekBar) findViewById(R.id.seekbarPuntuacionNaturalidad);


        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = getIntent().getStringExtra("idPartida");
        jugadoresEnPartidaIDs = getIntent().getStringArrayListExtra("jugadoresEnPartidaIDs");
        usuarioAEvaluarID = jugadoresEnPartidaIDs.get(0);   //ONSTART
        partida = database.getReference().child("JugadoresEnPartida").child(idPartida);
        mostrarPalabras();
        mostrarHistoria();
    }

    /**
     * Te mustra las palabras asignadas al empleado a evaular
     */
    private void mostrarPalabras() {
        database.getReference().child("JugadoresEnPartida").child(idPartida)
                .child(usuarioAEvaluarID).child("palabras").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    palabras.setText(task.getResult().getValue(String.class));
                }
            }
        });
    }

    /**
     * Muestra la historia del empleado a evaluar
     */
    private void mostrarHistoria() {
        partida.child(usuarioAEvaluarID).child("historia").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d("firebase", "Error getting data", task.getException());
                }
                else {
                    historia.setText(task.getResult().getValue(String.class));
                }
            }
        });
    }

    /**
     * Guarda en la base de datos la puntuacion subjetiva del empleado a evaluar
     * @param view
     */
    public void onClickEvaluar(View view) {
        partida.child(usuarioAEvaluarID).child("puntuacion").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer puntuacionPalabras = puntuadorPalabras.getProgress();
                Integer puntuacionCohesion = puntuadorCohesion.getProgress();
                Integer puntuacionFluidez = puntuadorFluidez.getProgress();
                Integer puntuacionNaturalidad= puntuadorNaturalidad.getProgress();
                double puntuacion = (puntuacionNaturalidad+puntuacionFluidez+puntuacionCohesion+puntuacionPalabras) / 4.0;

                if (currentData.getValue() == null) currentData.setValue(puntuacion);

                else {
                    double puntuacionAcumulada = currentData.getValue(Double.class);
                    currentData.setValue(puntuacion + puntuacionAcumulada);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                incrementarNumVecesEvaluado();
            }
        });
    }

    private void incrementarNumVecesEvaluado(){
        database.getReference().child("JugadoresEnPartida").child(idPartida)
                .child(usuarioAEvaluarID).child("numVecesEvaluado").runTransaction(new Transaction.Handler(){

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) currentData.setValue(1);

                else {
                    Integer numVecesEvaluado = currentData.getValue(Integer.class);
                    currentData.setValue(numVecesEvaluado + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                jugadoresEnPartidaIDs.remove(0);
                Intent intent;
                if (jugadoresEnPartidaIDs.isEmpty()){
                    intent = new Intent(getApplicationContext(), EsperandoEvaluacionCreatividad.class);
                    intent.putExtra("idPartida", idPartida);
                }else{
                    intent = new Intent(getApplicationContext(), PuntuarTexto.class);
                    intent.putExtra("idPartida", idPartida);
                    intent.putStringArrayListExtra("jugadoresEnPartidaIDs", jugadoresEnPartidaIDs);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

}