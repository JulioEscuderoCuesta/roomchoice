package com.example.roomchoice.salaaptitudes.juego1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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

import java.util.ArrayList;
import java.util.HashMap;

public class TestJuego1SalaAptitudes extends AppCompatActivity {

    private String idPartida;
    private String roomCode;
    private int texto;

    private SeekBar seekBarPregunta1TestSalaAptitudes, seekBarPregunta2TestSalaAptitudes, seekBarPregunta3TestSalaAptitudes, seekBarPregunta4TestSalaAptitudes;
    private ProgressBar progressBar;
    private TextView tituloTestEvaluarExposicionTexto;
    private TextView pregunta1ExponerTextoTexto, pregunta2ExponerTextoTexto, pregunta3ExponerTextoTexto, pregunta4ExponerTextoTexto, pregunta5ExponerTextoTexto;
    private TextView textoPregunta1SeekBarExponerTextoSalaAptitudes, textoPregunta2SeekBarExponerTextoSalaAptitudes, textoPregunta3SeekBarExponerTextoSalaAptitudes, textoPregunta4SeekBarExponerTextoSalaAptitudes;
    private RadioGroup pregunta1ExponerTextoRadioGroup, pregunta2ExponerTextoRadioGroup,pregunta3ExponerTextoRadioGroup, pregunta4ExponerTextoRadioGroup, pregunta5ExponerTextoRadioGroup;
    private TextView deAcuerdo;
    private TextView noDeAcuerdo;


    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database;
    private DatabaseReference jugadorExponiendo;
    private DatabaseReference pregunta1;
    private DatabaseReference pregunta2;

    private int numJugadores = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_exposicion_texto_sala_aptitudes);
        idPartida = this.getIntent().getStringExtra("idPartida");
        roomCode = this.getIntent().getStringExtra("roomCode");
        texto = this.getIntent().getIntExtra("texto", 0);

        //Preguntas Checkbox
        tituloTestEvaluarExposicionTexto = findViewById(R.id.tituloTestEvaluarExposicionTexto);
        pregunta1ExponerTextoTexto = findViewById(R.id.pregunta1ExponerTextoTexto);
        pregunta2ExponerTextoTexto = findViewById(R.id.pregunta2ExponerTextoTexto);
        pregunta3ExponerTextoTexto = findViewById(R.id.pregunta3ExponerTextoTexto);
        pregunta4ExponerTextoTexto = findViewById(R.id.pregunta4ExponerTextoTexto);
        pregunta5ExponerTextoTexto = findViewById(R.id.pregunta5ExponerTextoTexto);
        pregunta1ExponerTextoRadioGroup = findViewById(R.id.pregunta1ExponerTextoRadioGroup);
        pregunta2ExponerTextoRadioGroup = findViewById(R.id.pregunta2ExponerTextoRadioGroup);
        pregunta3ExponerTextoRadioGroup = findViewById(R.id.pregunta3ExponerTextoRadioGroup);
        pregunta4ExponerTextoRadioGroup = findViewById(R.id.pregunta4ExponerTextoRadioGroup);
        pregunta5ExponerTextoRadioGroup = findViewById(R.id.pregunta5ExponerTextoRadioGroup);

        //Preguntas SeekBar
        textoPregunta1SeekBarExponerTextoSalaAptitudes = findViewById(R.id.textoPregunta1SeekBarExponerTextoSalaAptitudes);
        textoPregunta2SeekBarExponerTextoSalaAptitudes = findViewById(R.id.textoPregunta2SeekBarExponerTextoSalaAptitudes);
        textoPregunta3SeekBarExponerTextoSalaAptitudes = findViewById(R.id.textoPregunta3SeekBarExponerTextoSalaAptitudes);
        textoPregunta4SeekBarExponerTextoSalaAptitudes = findViewById(R.id.textoPregunta4SeekBarExponerTextoSalaAptitudes);
        seekBarPregunta1TestSalaAptitudes = findViewById(R.id.seekBarPregunta1TestSalaAptitudes);
        seekBarPregunta2TestSalaAptitudes = findViewById(R.id.seekBarPregunta2TestSalaAptitudes);
        seekBarPregunta3TestSalaAptitudes = findViewById(R.id.seekBarPregunta3TestSalaAptitudes);
        seekBarPregunta4TestSalaAptitudes = findViewById(R.id.seekBarPregunta4TestSalaAptitudes);
        deAcuerdo = findViewById(R.id.deacuerdo);
        noDeAcuerdo = findViewById(R.id.desacuerdo);

        progressBar = findViewById(R.id.progressBarTestExposicionTexto);
        progressBar.setVisibility(View.GONE);
        seleccionarPreguntas();
    }

    /**
     * Registra el evento de pulsar el botón "Enviar" en activity_test_exposicion_texto_sala_aptitudes.xml
     * Se comprueba que todas las preguntas se hayan respondido y en caso afirmativo, se coge la puntuación
     * obtenida, se hace un cálculo según el peso asignado a cada pregunta y se sube a la base de datos la
     * puntuación obtenida para el jugador que ha realizado al exposición.
     * @param view El botón pulsado.
     */
    public void onClickTerminarEvaluarButton(View view) {
        if (pregunta1ExponerTextoRadioGroup.getCheckedRadioButtonId() == - 1  || pregunta2ExponerTextoRadioGroup.getCheckedRadioButtonId() == - 1  ||
                pregunta3ExponerTextoRadioGroup.getCheckedRadioButtonId() ==  - 1  || pregunta4ExponerTextoRadioGroup.getCheckedRadioButtonId() == - 1 ||
                    pregunta5ExponerTextoRadioGroup.getCheckedRadioButtonId() == - 1){
            Toast.makeText(getApplicationContext(), "Por favor conteste a todas las preguntas", Toast.LENGTH_SHORT).show();
        } else {
            int puntuacion1 = seekBarPregunta1TestSalaAptitudes.getProgress();
            int puntuacion2 = seekBarPregunta2TestSalaAptitudes.getProgress();
            int puntuacion3 = seekBarPregunta3TestSalaAptitudes.getProgress();
            int puntuacion4 = seekBarPregunta4TestSalaAptitudes.getProgress();
            double puntuacionSeekBar = (puntuacion1+puntuacion2+puntuacion3+puntuacion4) / 4.0;
            double puntuacion = puntuacionSeekBar * 0.5 + calcularPuntuacion() * 0.5 ;
            jugadorExponiendo = database.getReference().child("JugadoresEnPartida").child(idPartida);
            jugadorExponiendo.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshotJugadoresDataChange) {
                    String idJugador = "";
                    numJugadores = (int) snapshotJugadoresDataChange.getChildrenCount();
                    for (DataSnapshot jugadoresSnapshot : snapshotJugadoresDataChange.getChildren()) {
                        HashMap<String, Object> jugador = (HashMap<String, Object>) jugadoresSnapshot.getValue();
                        if (jugador.get("haExpuesto").equals(true) && jugador.get("haSidoEvaluado").equals(false)) {
                            idJugador = jugadoresSnapshot.getKey();
                            pregunta1 = database.getReference().child("Puntuaciones").child(idPartida).child(idJugador).child("HacerExposicionSalaAptitudes").child("puntuacion");

                            String finalIdJugador = idJugador;
                            pregunta1.runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                    if (currentData.getValue() != null) {
                                        double puntuacionAcumulada = currentData.getValue(Double.class);
                                        currentData.setValue(puntuacionAcumulada + puntuacion);
                                    } else
                                        currentData.setValue(puntuacion);
                                    return Transaction.success(currentData);
                                }
                                @Override
                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    incrementarNumVecesEvaluado(finalIdJugador);
                                }
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    /* Cada vez que se envía una evaluación, se incrementa el número de veces
     * que ha sido evaluado el jugador que acaba de exponer. */
    private void incrementarNumVecesEvaluado(String idJugador) {
        database.getReference().child("Puntuaciones").child(idPartida).child(idJugador).
                child("HacerExposicionSalaAptitudes").child("numVecesEvaluado").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() != null) {
                    double evaluaciones= currentData.getValue(Double.class);
                    currentData.setValue(evaluaciones + 1);
                } else
                    currentData.setValue(1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                esperarEvaluaciones(idJugador);
            }
        });

    }

    /* Según el texto del que se trate, se escogen determinadas preguntas para el test
     * y determinadas respuestas para cada pregunta. */
    private void seleccionarPreguntas() {
        ArrayList<Integer> respuestas = new ArrayList<>();
        switch (texto) {
            case 1:
                pregunta1ExponerTextoTexto.setText(R.string.pregunta1Texto1Juego1);
                pregunta2ExponerTextoTexto.setText(R.string.pregunta2Texto1Juego1);
                pregunta3ExponerTextoTexto.setText(R.string.pregunta3Texto1Juego1);
                pregunta4ExponerTextoTexto.setText(R.string.pregunta4Texto1Juego1);
                pregunta5ExponerTextoTexto.setText(R.string.pregunta5Texto1Juego1);

                respuestas.add(R.string.respuesta1Pregunta1Texto1Juego1);
                respuestas.add(R.string.respuesta2Pregunta1Texto1Juego1);
                respuestas.add(R.string.respuesta3Pregunta1Texto1Juego1);
                respuestas.add(R.string.respuesta4Pregunta1Texto1Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto1Juego1);
                respuestas.add(R.string.respuesta2Pregunta2Texto1Juego1);
                respuestas.add(R.string.respuesta3Pregunta2Texto1Juego1);
                respuestas.add(R.string.respuesta4Pregunta2Texto1Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto1Juego1);
                respuestas.add(R.string.respuesta2Pregunta3Texto1Juego1);
                respuestas.add(R.string.respuesta3Pregunta3Texto1Juego1);
                respuestas.add(R.string.respuesta4Pregunta3Texto1Juego1);;

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto1Juego1);
                respuestas.add(R.string.respuesta2Pregunta4Texto1Juego1);
                respuestas.add(R.string.respuesta3Pregunta4Texto1Juego1);
                respuestas.add(R.string.respuesta4Pregunta4Texto1Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto1Juego1);
                respuestas.add(R.string.respuesta2Pregunta5Texto1Juego1);
                respuestas.add(R.string.respuesta3Pregunta5Texto1Juego1);
                respuestas.add(R.string.respuesta4Pregunta5Texto1Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();
                break;
            case 2:
                pregunta1ExponerTextoTexto.setText(R.string.pregunta1Texto2Juego1);
                pregunta2ExponerTextoTexto.setText(R.string.pregunta2Texto2Juego1);
                pregunta3ExponerTextoTexto.setText(R.string.pregunta3Texto2Juego1);
                pregunta4ExponerTextoTexto.setText(R.string.pregunta4Texto2Juego1);
                pregunta5ExponerTextoTexto.setText(R.string.pregunta5Texto2Juego1);


                respuestas.add(R.string.respuesta1Pregunta1Texto2Juego1);
                respuestas.add(R.string.respuesta2Pregunta1Texto2Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1ExponerTextoRadioGroup.addView(radioButton);
                }
                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto2Juego1);
                respuestas.add(R.string.respuesta2Pregunta2Texto2Juego1);


                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto2Juego1);
                respuestas.add(R.string.respuesta2Pregunta3Texto2Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto2Juego1);
                respuestas.add(R.string.respuesta2Pregunta4Texto2Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto2Juego1);
                respuestas.add(R.string.respuesta2Pregunta5Texto2Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();
                break;
            case 3:
                pregunta1ExponerTextoTexto.setText(R.string.pregunta1Texto3Juego1);
                pregunta2ExponerTextoTexto.setText(R.string.pregunta2Texto3Juego1);
                pregunta3ExponerTextoTexto.setText(R.string.pregunta3Texto3Juego1);
                pregunta4ExponerTextoTexto.setText(R.string.pregunta4Texto3Juego1);
                pregunta5ExponerTextoTexto.setText(R.string.pregunta5Texto3Juego1);

                respuestas.add(R.string.respuesta1Pregunta1Texto3Juego1);
                respuestas.add(R.string.respuesta2Pregunta1Texto3Juego1);
                respuestas.add(R.string.respuesta3Pregunta1Texto3Juego1);
                respuestas.add(R.string.respuesta4Pregunta1Texto3Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto3Juego1);
                respuestas.add(R.string.respuesta2Pregunta2Texto3Juego1);
                respuestas.add(R.string.respuesta3Pregunta2Texto3Juego1);
                respuestas.add(R.string.respuesta4Pregunta2Texto3Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto3Juego1);
                respuestas.add(R.string.respuesta2Pregunta3Texto3Juego1);
                respuestas.add(R.string.respuesta3Pregunta3Texto3Juego1);
                respuestas.add(R.string.respuesta4Pregunta3Texto3Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto3Juego1);
                respuestas.add(R.string.respuesta2Pregunta4Texto3Juego1);
                respuestas.add(R.string.respuesta3Pregunta4Texto3Juego1);
                respuestas.add(R.string.respuesta4Pregunta4Texto3Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto3Juego1);
                respuestas.add(R.string.respuesta2Pregunta5Texto3Juego1);
                respuestas.add(R.string.respuesta3Pregunta5Texto3Juego1);
                respuestas.add(R.string.respuesta4Pregunta5Texto3Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                break;
            case 4:
                pregunta1ExponerTextoTexto.setText(R.string.pregunta1Texto4Juego1);
                pregunta2ExponerTextoTexto.setText(R.string.pregunta2Texto4Juego1);
                pregunta3ExponerTextoTexto.setText(R.string.pregunta3Texto4Juego1);
                pregunta4ExponerTextoTexto.setText(R.string.pregunta4Texto4Juego1);
                pregunta5ExponerTextoTexto.setText(R.string.pregunta5Texto4Juego1);

                respuestas.add(R.string.respuesta1Pregunta1Texto4Juego1);
                respuestas.add(R.string.respuesta2Pregunta1Texto4Juego1);
                respuestas.add(R.string.respuesta3Pregunta1Texto4Juego1);
                respuestas.add(R.string.respuesta4Pregunta1Texto4Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto4Juego1);
                respuestas.add(R.string.respuesta2Pregunta2Texto4Juego1);
                respuestas.add(R.string.respuesta3Pregunta2Texto4Juego1);
                respuestas.add(R.string.respuesta4Pregunta2Texto4Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto4Juego1);
                respuestas.add(R.string.respuesta2Pregunta3Texto4Juego1);
                respuestas.add(R.string.respuesta3Pregunta3Texto4Juego1);
                respuestas.add(R.string.respuesta4Pregunta3Texto4Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto4Juego1);
                respuestas.add(R.string.respuesta2Pregunta4Texto4Juego1);
                respuestas.add(R.string.respuesta3Pregunta4Texto4Juego1);
                respuestas.add(R.string.respuesta4Pregunta4Texto4Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto4Juego1);
                respuestas.add(R.string.respuesta2Pregunta5Texto4Juego1);
                respuestas.add(R.string.respuesta3Pregunta5Texto4Juego1);
                respuestas.add(R.string.respuesta4Pregunta5Texto4Juego1);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5ExponerTextoRadioGroup.addView(radioButton);
                }

                respuestas.clear();

                break;
        }
    }

    /* Se selecciona la respuesta que el usuario ha marcado y si es la respuesta correcta,
     * se suma la puntuación. */
    private double calcularPuntuacion() {
        double puntuacion = 1.0;
        switch (texto) {
            case 1:
                switch(pregunta1ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 4:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch(pregunta1ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                break;
            case 3:
                switch(pregunta1ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 4:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 4:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 4:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                break;
            case 4:
                switch(pregunta1ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 4:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 4:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5ExponerTextoRadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        puntuacion+=0.8;
                        break;
                    default:
                        break;
                }
                break;
        }
        return puntuacion;
    }

    /* Cuando el jugador ha terminado de evaluar, se queda a la espera de que el resto de jugadores
     * menos aquel que ha expuesto terminan de evaluar. Cuando esto suceda, se pasa a decidir quíen
     * expone. */
    private void esperarEvaluaciones(String idJugador) {
        ocultarElementos();
        pregunta2 = database.getReference().child("Puntuaciones").child(idPartida).child(idJugador).child("HacerExposicionSalaAptitudes").child("numVecesEvaluado");
        pregunta2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotPregunta2) {
                if (snapshotPregunta2.getValue(Integer.class) == numJugadores - 1) {
                    Intent intent = new Intent(getApplicationContext(), DecidirQuienExpone.class);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("idPartida", idPartida);
                    intent.putExtra("juego", 1);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Se ocultan todas las preguntas y respuestas del text y se habilita una barra de progreso
     * cuando el jugador espera a las demás evaluaciones. */
    private void ocultarElementos() {
        pregunta1ExponerTextoTexto.setVisibility(View.GONE);
        pregunta2ExponerTextoTexto.setVisibility(View.GONE);
        pregunta3ExponerTextoTexto.setVisibility(View.GONE);
        pregunta4ExponerTextoTexto.setVisibility(View.GONE);
        pregunta5ExponerTextoTexto.setVisibility(View.GONE);

        pregunta1ExponerTextoRadioGroup.setVisibility(View.GONE);
        pregunta2ExponerTextoRadioGroup.setVisibility(View.GONE);
        pregunta3ExponerTextoRadioGroup.setVisibility(View.GONE);
        pregunta4ExponerTextoRadioGroup.setVisibility(View.GONE);
        pregunta5ExponerTextoRadioGroup.setVisibility(View.GONE);

        textoPregunta1SeekBarExponerTextoSalaAptitudes.setVisibility(View.GONE);
        textoPregunta2SeekBarExponerTextoSalaAptitudes.setVisibility(View.GONE);
        textoPregunta3SeekBarExponerTextoSalaAptitudes.setVisibility(View.GONE);
        textoPregunta4SeekBarExponerTextoSalaAptitudes.setVisibility(View.GONE);

        seekBarPregunta1TestSalaAptitudes.setVisibility(View.GONE);
        seekBarPregunta2TestSalaAptitudes.setVisibility(View.GONE);
        seekBarPregunta3TestSalaAptitudes.setVisibility(View.GONE);
        seekBarPregunta4TestSalaAptitudes.setVisibility(View.GONE);

        deAcuerdo.setVisibility(View.GONE);
        noDeAcuerdo.setVisibility(View.GONE);

        tituloTestEvaluarExposicionTexto.setText(R.string.textoProgressBar);
        progressBar.setVisibility(View.VISIBLE);


    }

    @Override
    public void onBackPressed() {

    }
}

