package com.example.roomchoice.salaaptitudes.juego2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomchoice.R;
import com.example.roomchoice.salaaptitudes.EnviarCorreoSalaAptitudes;
import com.example.roomchoice.salaaptitudes.EsperarJuegoSalaAptitudes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * La clase TestJuego2SalaAptitudes muestra una serie de preguntas sobre el texto que se
 * ha leído previamente y calcula la puntuación obtenida teniendo en cuenta el tiempo tardado
 * en leer el texto
 */
public class TestJuego2SalaAptitudes extends AppCompatActivity {

    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    private TextView tituloTestJuego2SalaAptitudes;
    private RadioGroup pregunta1RadioGroup;
    private RadioGroup pregunta2RadioGroup;
    private RadioGroup pregunta3RadioGroup;
    private RadioGroup pregunta4RadioGroup;
    private RadioGroup pregunta5RadioGroup;
    private TextView pregunta1Texto;
    private TextView pregunta2Texto;
    private TextView pregunta3Texto;
    private TextView pregunta4Texto;
    private TextView pregunta5Texto;
    private Button terminarEvaluarButton;
    private ProgressBar progressBar;

    private String idPartida;
    private int texto;
    private String roomCode;
    private double puntuacionSinTiempo;
    private double puntuacion;

    private FirebaseDatabase database;
    private DatabaseReference esperarJugadores;
    private DatabaseReference calcularPuntuacion;

    private boolean noTerminarCambios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_lectura_texto_sala_aptitudes);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = this.getIntent().getStringExtra("idPartida");
        texto = this.getIntent().getIntExtra("texto", 1);
        roomCode = getIntent().getStringExtra("roomCode");
        noTerminarCambios = true;
        tituloTestJuego2SalaAptitudes = findViewById(R.id.tituloTestJuego2SalaAptitudes);
        pregunta1RadioGroup = findViewById(R.id.pregunta1LeerTextoRadioGroup);
        pregunta2RadioGroup = findViewById(R.id.pregunta2LeerTextoRadioGroup);
        pregunta3RadioGroup = findViewById(R.id.pregunta3LeerTextoRadioGroup);
        pregunta4RadioGroup = findViewById(R.id.pregunta4LeerTextoRadioGroup);
        pregunta5RadioGroup = findViewById(R.id.pregunta5LeerTextoRadioGroup);
        pregunta1Texto = findViewById(R.id.pregunta1LeerTextoTexto);
        pregunta2Texto = findViewById(R.id.pregunta2LeerTextoTexto);
        pregunta3Texto = findViewById(R.id.pregunta3LeerTextoTexto);
        pregunta4Texto = findViewById(R.id.pregunta4LeerTextoTexto);
        pregunta5Texto = findViewById(R.id.pregunta5LeerTextoTexto);
        progressBar = findViewById(R.id.progressBarTestLecturaTexto);
        terminarEvaluarButton = findViewById(R.id.terminarEvaluarButton);
        progressBar.setVisibility(View.GONE);

        seleccionarPreguntas();
    }

    /**
     * Registra el evento de pulsar el botón "Enviar" en activity_test_lectura_texto_sala_aptitudes.xml
     * Se comprueba que todas las preguntas se hayan respondido y en caso afirmativo, se coge la puntuación
     * obtenida, se sube a la base de datos y se procede a esperar a que el resto de jugadores terminen.
     * @param view El botón pulsado.
     */
    public void onClickEnviarRespuestas(View view) {
        if(pregunta1RadioGroup.getCheckedRadioButtonId() == -1 || pregunta2RadioGroup.getCheckedRadioButtonId() == -1 ||
                pregunta3RadioGroup.getCheckedRadioButtonId() == -1 || pregunta4RadioGroup.getCheckedRadioButtonId() == -1 ||
                pregunta5RadioGroup.getCheckedRadioButtonId() == -1)
            Toast.makeText(getApplicationContext(), "Por favor conteste a todas las preguntas", Toast.LENGTH_SHORT).show();
        else {
            database.getReference().child("JugadoresEnPartida").child(idPartida).child(USER.getUid()).child("test2Terminado").setValue(true);
            puntuacionSinTiempo = calcularPuntuacionSinTiempo();
            database.getReference().child("JugadoresEnPartida").child(idPartida).child(USER.getUid()).child("puntuacion").setValue(puntuacionSinTiempo);
            database.getReference().child("Puntuaciones").child(idPartida).child(USER.getUid()).child("LeerTextoSalaAptitudes").child("puntuacionSinTiempo").setValue(puntuacionSinTiempo);
            calcularPuntuacionReal();

            esperarRestoJugadores();
        }
    }

    /* A partir de la puntuación obtenida en el test y la penalización, se calcula la puntuación final y
     * se indica en la base de datos. */
    private void calcularPuntuacionReal() {
        calcularPuntuacion = database.getReference().child("Puntuaciones").child(idPartida).child(USER.getUid()).child("LeerTextoSalaAptitudes");
        calcularPuntuacion.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                long segundosLeerTexto = task.getResult().child("tiempoLeerTexto").getValue(Long.class);
                double penalizacion = calcularPenalizacion(segundosLeerTexto);
                puntuacion = puntuacionSinTiempo - (segundosLeerTexto * penalizacion);
                database.getReference().child("Puntuaciones").child(idPartida).child(USER.getUid()).child("LeerTextoSalaAptitudes").child("puntuacion").setValue(puntuacion);
            }
        });


    }

    /*Se calcula una penalización en base al tiempo tardado en leer el texto.*/
    private double calcularPenalizacion(long segundosLeerTexto) {
        double penalizacion = 0.0;
        if(segundosLeerTexto <= 20)
            penalizacion = 0.005;
        else if(segundosLeerTexto < 60 && segundosLeerTexto >40)
            penalizacion = 0.01;
        else if(segundosLeerTexto >=60)
            penalizacion = 0.015;
        return penalizacion;
    }

    /* Se selecciona la respuesta que el usuario ha marcado y si es la respuesta correcta,
     * se suma la puntuación. */
    private double calcularPuntuacionSinTiempo() {
        double x = 1.0;
        switch (texto) {
            case 1:
                switch(pregunta1RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2RadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4RadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5RadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch(pregunta1RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2RadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3RadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4RadioGroup.getCheckedRadioButtonId()) {
                    case 1:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                break;
            case 3:
                switch(pregunta1RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                break;
            case 4:
                switch(pregunta1RadioGroup.getCheckedRadioButtonId()) {
                    case 3:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta2RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta3RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta4RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                switch(pregunta5RadioGroup.getCheckedRadioButtonId()) {
                    case 2:
                        x+=0.8;
                        break;
                    default:
                        break;
                }
                break;
        }
        return x;
    }

    /* Según el texto del que se trate, se escogen determinadas preguntas para el test
     * y determinadas respuestas para cada pregunta. */
    private void seleccionarPreguntas() {
        ArrayList<Integer> respuestas = new ArrayList<>();
        switch (texto) {
            case 1:
                pregunta1Texto.setText(R.string.pregunta1Texto1Juego2);
                pregunta2Texto.setText(R.string.pregunta2Texto1Juego2);
                pregunta3Texto.setText(R.string.pregunta3Texto1Juego2);
                pregunta4Texto.setText(R.string.pregunta4Texto1Juego2);
                pregunta5Texto.setText(R.string.pregunta5Texto1Juego2);

                respuestas.add(R.string.respuesta1Pregunta1Texto1Juego2);
                respuestas.add(R.string.respuesta2Pregunta1Texto1Juego2);
                respuestas.add(R.string.respuesta3Pregunta1Texto1Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto1Juego2);
                respuestas.add(R.string.respuesta2Pregunta2Texto1Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto1Juego2);
                respuestas.add(R.string.respuesta2Pregunta3Texto1Juego2);
                respuestas.add(R.string.respuesta3Pregunta3Texto1Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto1Juego2);
                respuestas.add(R.string.respuesta2Pregunta4Texto1Juego2);
                respuestas.add(R.string.respuesta3Pregunta4Texto1Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto1Juego2);
                respuestas.add(R.string.respuesta2Pregunta5Texto1Juego2);
                respuestas.add(R.string.respuesta3Pregunta5Texto1Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5RadioGroup.addView(radioButton);
                }
                break;


            case 2:
                pregunta1Texto.setText(R.string.pregunta1Texto2Juego2);
                pregunta2Texto.setText(R.string.pregunta2Texto2Juego2);
                pregunta3Texto.setText(R.string.pregunta3Texto2Juego2);
                pregunta4Texto.setText(R.string.pregunta4Texto2Juego2);
                pregunta5Texto.setText(R.string.pregunta5Texto2Juego2);


                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta1Texto2Juego2);
                respuestas.add(R.string.respuesta2Pregunta1Texto2Juego2);
                respuestas.add(R.string.respuesta3Pregunta1Texto2Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto2Juego2);
                respuestas.add(R.string.respuesta2Pregunta2Texto2Juego2);
                respuestas.add(R.string.respuesta3Pregunta2Texto2Juego2);


                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto2Juego2);
                respuestas.add(R.string.respuesta2Pregunta3Texto2Juego2);
                respuestas.add(R.string.respuesta3Pregunta3Texto2Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3RadioGroup.addView(radioButton);
                }


                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto2Juego2);
                respuestas.add(R.string.respuesta2Pregunta4Texto2Juego2);
                respuestas.add(R.string.respuesta3Pregunta4Texto2Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto2Juego2);
                respuestas.add(R.string.respuesta2Pregunta5Texto2Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5RadioGroup.addView(radioButton);
                }
                break;

            case 3:
                pregunta1Texto.setText(R.string.pregunta1Texto3Juego2);
                pregunta2Texto.setText(R.string.pregunta2Texto3Juego2);
                pregunta3Texto.setText(R.string.pregunta3Texto3Juego2);
                pregunta4Texto.setText(R.string.pregunta4Texto3Juego2);
                pregunta5Texto.setText(R.string.pregunta5Texto3Juego2);


                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta1Texto3Juego2);
                respuestas.add(R.string.respuesta2Pregunta1Texto3Juego2);
                respuestas.add(R.string.respuesta3Pregunta1Texto3Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto3Juego2);
                respuestas.add(R.string.respuesta2Pregunta2Texto3Juego2);
                respuestas.add(R.string.respuesta3Pregunta2Texto3Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto3Juego2);
                respuestas.add(R.string.respuesta2Pregunta3Texto3Juego2);
                respuestas.add(R.string.respuesta3Pregunta3Texto3Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3RadioGroup.addView(radioButton);
                }


                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto3Juego2);
                respuestas.add(R.string.respuesta2Pregunta4Texto3Juego2);
                respuestas.add(R.string.respuesta3Pregunta4Texto3Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto3Juego2);
                respuestas.add(R.string.respuesta2Pregunta5Texto3Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5RadioGroup.addView(radioButton);
                }
                break;

            case 4:
                pregunta1Texto.setText(R.string.pregunta1Texto4Juego2);
                pregunta2Texto.setText(R.string.pregunta2Texto4Juego2);
                pregunta3Texto.setText(R.string.pregunta3Texto4Juego2);
                pregunta4Texto.setText(R.string.pregunta4Texto4Juego2);
                pregunta5Texto.setText(R.string.pregunta5Texto4Juego2);

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta1Texto4Juego2);
                respuestas.add(R.string.respuesta2Pregunta1Texto4Juego2);
                respuestas.add(R.string.respuesta3Pregunta1Texto4Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta1RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta2Texto4Juego2);
                respuestas.add(R.string.respuesta2Pregunta2Texto4Juego2);
                respuestas.add(R.string.respuesta3Pregunta2Texto4Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta2RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta3Texto4Juego2);
                respuestas.add(R.string.respuesta2Pregunta3Texto4Juego2);
                respuestas.add(R.string.respuesta3Pregunta3Texto4Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta3RadioGroup.addView(radioButton);
                }

                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta4Texto4Juego2);
                respuestas.add(R.string.respuesta2Pregunta4Texto4Juego2);
                respuestas.add(R.string.respuesta3Pregunta4Texto4Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta4RadioGroup.addView(radioButton);
                }
                respuestas.clear();

                respuestas.add(R.string.respuesta1Pregunta5Texto4Juego2);
                respuestas.add(R.string.respuesta2Pregunta5Texto4Juego2);

                for(int id = 1; id <= respuestas.size(); id++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(respuestas.get(id - 1));
                    radioButton.setId(id);
                    pregunta5RadioGroup.addView(radioButton);
                }
                break;

        }
    }

    /* Se espera a que el resto de jugadores terminen de realizar el test.
     * Cuando sea así, se pasa a la actividad de enviar el correo con los resultados. */
    private void esperarRestoJugadores() {
        tituloTestJuego2SalaAptitudes.setText(R.string.textoProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        ocultarElementos();
        esperarJugadores = database.getReference().child("Puntuaciones").child(idPartida);
        esperarJugadores.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (noTerminarCambios) {
                    boolean hanEnviadoTodos = true;
                    for (DataSnapshot jugador : snapshot.getChildren()) {
                        if (jugador.child("LeerTextoSalaAptitudes").child("puntuacion").getValue() == null) {
                            hanEnviadoTodos = false;
                        }
                    }

                    if (hanEnviadoTodos) {
                        noTerminarCambios = false;
                        Intent intent = new Intent(getApplicationContext(), EnviarCorreoSalaAptitudes.class);
                        intent.putExtra("juego", 2);
                        intent.putExtra("roomCode", roomCode);
                        intent.putExtra("idPartida", idPartida);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ocultarElementos() {
        pregunta1RadioGroup.setVisibility(View.GONE);
        pregunta2RadioGroup.setVisibility(View.GONE);
        pregunta3RadioGroup.setVisibility(View.GONE);
        pregunta4RadioGroup.setVisibility(View.GONE);
        pregunta5RadioGroup.setVisibility(View.GONE);
        pregunta1Texto.setVisibility(View.GONE);
        pregunta2Texto.setVisibility(View.GONE);
        pregunta3Texto.setVisibility(View.GONE);
        pregunta4Texto.setVisibility(View.GONE);
        pregunta5Texto.setVisibility(View.GONE);
        terminarEvaluarButton.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

    }
}