package com.example.roomchoice.salaaptitudes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.roomchoice.CreacionSala;
import com.example.roomchoice.R;
import com.example.roomchoice.modelo.Estado;
import com.example.roomchoice.modelo.JuegoSalaAptitudes;
import com.google.firebase.database.FirebaseDatabase;

/**
 * La clase ElegirJuegoSalaAptitudes permite al empleado de recursos humanos elegir qué modalidad
 * de la sala de aptitudes se jugará
 * @author
 * @author
 * @author
 * @author
 */
public class ElegirJuegoSalaAptitudes extends AppCompatActivity {

    private String idPartida;
    private String roomCode;
    private Intent intent;
    private FirebaseDatabase database;

    private TextView textoExplicacionJuego;
    private Button juegoExposicionBoton;
    private Button juegoLecturaTextoBoton;
    private Button comenzarJuegoSalaAptitudesButton;
    private int duracionAnimacion;
    private boolean botonComenzarAnimado = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir_juego_sala_aptitudes);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = this.getIntent().getStringExtra("idPartida");
        roomCode = this.getIntent().getStringExtra("roomCode");
        textoExplicacionJuego = findViewById(R.id.textoExplicacionJuego);
        juegoExposicionBoton = findViewById(R.id.juegoExposicionBoton);
        juegoLecturaTextoBoton = findViewById(R.id.juegoLecturaTextoBoton);
        comenzarJuegoSalaAptitudesButton = findViewById(R.id.comenzarJuegoSalaAptitudesButton);
        textoExplicacionJuego.setVisibility(View.GONE);
        comenzarJuegoSalaAptitudesButton.setVisibility(View.GONE);
        duracionAnimacion = getResources().getInteger(android.R.integer.config_mediumAnimTime);

    }

    /**
     * Registra el evento de pulsar el botón "Exposición" en activity_elegir_juego_sala_aptitudes.xml
     * Asigna un texto explicativo del juego al Textview correspondiente
     * @param view El botón pulsado
     */
    public void onClickJuegoExposicionBoton(View view) {
        textoExplicacionJuego.setText(R.string.explicacionJuegoExposicionTexto);
        animarTextoExplicacion(view);
    }

    /**
     * Registra el evento de pulsar el botón "Lectura Texto" en activity_elegir_juego_sala_aptitudes.xml
     * Asigna un texto explicativo del juego al Textview correspondiente
     * @param view El botón pulsado
     */
    public void onClickJuegoLecturaTextoBoton(View view) {
        textoExplicacionJuego.setText(R.string.explicacionJuegoLecturaTexto);
        animarTextoExplicacion(view);
    }

    /**
     * Registra el evento de pulsar el botón "Comenzar" en activity_elegir_juego_sala_aptitudes.xml
     * Asigna la modalidad seleccionada a la sala y carga la actividad SalaAptitudesRRHH para el
     * empleado de recursos humanos.
     * @param view El botón pulsado
     */
    public void onClickComenzarSalaAptitudes(View view) {
        intent = new Intent(getApplicationContext(), SalaAptitudesRRHH.class);
        intent.putExtra("roomCode", roomCode);
        intent.putExtra("idPartida", idPartida);
        if(juegoExposicionBoton.isHovered()) {
            intent.putExtra("juego", 1);
            database.getReference().child("Partidas").child(idPartida).child("juegoSalaAptitudes").setValue(JuegoSalaAptitudes.EXPOSICIONTEXTO);
        }
        else {
            intent.putExtra("juego", 2);
            database.getReference().child("Partidas").child(idPartida).child("juegoSalaAptitudes").setValue(JuegoSalaAptitudes.LECTURATEXTO);
        }
        startActivity(intent);
    }

    /**
     * Registra el evento de pulsar el botón "Terminar Sala" en activity_elegir_juego_sala_aptitudes.xml
     * Marca la partida como finalizada y vuelve a CreacionSala
     * @param view El botón pulsado
     */
    public void onClickTerminarSalaBoton(View view) {
        database.getReference().child("Partidas").child(idPartida).child("estado").setValue(Estado.FINALIZADASALAAPTITUDES);
        intent = new Intent(getApplicationContext(), CreacionSala.class);
        intent.putExtra("roomCode", roomCode);
        intent.putExtra("esRRHH", true);
        startActivity(intent);
    }

    /* Crea una breve animación para el texto explicativo de cada modalidad.
     * Este texto aparece en pantalla poco a poco.*/
    private void animarTextoExplicacion(View view) {
        textoExplicacionJuego.setAlpha(0f);
        textoExplicacionJuego.setVisibility(View.VISIBLE);
        textoExplicacionJuego.animate()
                .alpha(1f)
                .setDuration(duracionAnimacion)
                .setListener(null);
        if(view.getId()==R.id.juegoExposicionBoton) {
            juegoLecturaTextoBoton.animate().alpha(1F);
            juegoExposicionBoton.setEnabled(false);
            juegoLecturaTextoBoton.setEnabled(true);
            animarBotonJuego(juegoExposicionBoton);
        }
        else {
            juegoExposicionBoton.animate().alpha(1F);
            juegoLecturaTextoBoton.setEnabled(false);
            juegoExposicionBoton.setEnabled(true);
            animarBotonJuego(juegoLecturaTextoBoton);

        }
        if (botonComenzarAnimado == false) {
            comenzarJuegoSalaAptitudesButton.setAlpha(0f);
            comenzarJuegoSalaAptitudesButton.setVisibility(View.VISIBLE);
            comenzarJuegoSalaAptitudesButton.animate()
                    .alpha(1F)
                    .setDuration(duracionAnimacion);
            botonComenzarAnimado = true;
        }
    }

    /* Crea una animación para los botones de selección de modadlidad
     * Los botones pasan a estar brevemente difuminados para indicar que
     * han sido seleccionados. */
    private void animarBotonJuego(Button b) {
        b.animate()
                .alpha(0.5f)
                .setDuration(duracionAnimacion)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        juegoLecturaTextoBoton.setHovered(true);
                    }
                });
    }



    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Desea volver a la sala?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), CreacionSala.class);
                i.putExtra("esRRHH", true);
                i.putExtra("roomCode", roomCode);
                startActivity(i);
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}