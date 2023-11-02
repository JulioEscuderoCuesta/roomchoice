package com.example.roomchoice.salaaptitudes.juego1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.roomchoice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * La clase EscucharExposicionSalaAptitudes permite a los jugadores que no están exponiendo escuchar la exposición
 * de aquel que expone en ese turno.
 * @author
 * @author
 * @author
 * @author
 */
public class EscucharExposicionSalaAptitudes extends AppCompatActivity {

    private String idPartida;
    private String idJugadorQueExpone;
    private String roomCode;
    private int texto;
    private String nombreJugadorExponiendo;

    private TextView tituloEscucharExposicion;
    private TextView textoInformacionEsperandoExposicion;
    private ProgressBar progressBarEsperandoExposicion;
    private Button escucharExposicionButton;
    private Button terminarEscucharButton;

    private FirebaseDatabase database;
    private DatabaseReference jugadorQueHaExpuesto;
    private StorageReference exposicion;
    private DatabaseReference textoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = this.getIntent().getStringExtra("idPartida");
        roomCode = this.getIntent().getStringExtra("roomCode");
        texto = this.getIntent().getIntExtra("texto", 1);
        idJugadorQueExpone = this.getIntent().getStringExtra("idJugadorQueExpone");
        setContentView(R.layout.activity_escuchar_exposicion_sala_aptitudes);
        tituloEscucharExposicion = findViewById(R.id.tituloEscucharExposicion);
        textoInformacionEsperandoExposicion = findViewById(R.id.textoInformacionEsperandoExposicion);
        progressBarEsperandoExposicion = findViewById(R.id.progressBarEsperandoExposicion);
        escucharExposicionButton = findViewById(R.id.escucharExposicionButton);
        terminarEscucharButton = findViewById(R.id.terminarEscucharButton);
        escucharExposicionButton.setVisibility(View.INVISIBLE);
        terminarEscucharButton.setVisibility(View.INVISIBLE);

        obtenerNombreJugadorExponiendo();
        esperarExposicion();

    }

    /**
     * Registra el evento de pulsar el botón "Escuchar Exposición"
     * Se muestra un diálogo de confirmación y en caso de seleccionar Si,
     * se descarga un archivo mp4 con la exposición.
     * @param view
     */
    public void onClickReproducir(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Se descargará la exposición en formato mp4. ¿Desea continuar?")
                .setPositiveButton("Si", (dialog, which) -> {
                    exposicion = FirebaseStorage.getInstance("gs://roomchoice-2f8fd.appspot.com").getReference().child("ExposicionesSalaAptitudes").child(idPartida).child(idJugadorQueExpone).child("exposicion.mp4");
                    exposicion.getDownloadUrl().addOnSuccessListener(uri -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    });
                    terminarEscucharButton.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Registra el evento de pulsar el botón "Terminar" en activity_escuchar_exposicion_sala_aptitudes.xml
     * Se muestra un breve diálogo de confirmación y en caso de seleccionar Si,
     * se procede a cargar la actividad TestJuego1SalaAptitudes.
     * @param view
     */
    public void onClickTerminarEscuchar(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Desea terminar de escuchar la exposición?")
                .setPositiveButton("Si", (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), TestJuego1SalaAptitudes.class);
                    intent.putExtra("texto", texto);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("idPartida", idPartida);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /* Se obtiene el nombre del jugador que está exponiendo para mostrar en pantalla
     * si está exponiendo o si ya ha terminado. */
    private void obtenerNombreJugadorExponiendo() {
        database.getReference().child("JugadoresEnPartida").child(idPartida)
                .child(idJugadorQueExpone).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nombreJugadorExponiendo = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /* Se comprueba si el jugador al que le toca exponer esté turno está exponiendo o ha terminado.
     * Cuando termina, se habilita el botón que permite bajarse el archivo con la exposición.*/
    private void esperarExposicion() {
        jugadorQueHaExpuesto = database.getReference().child("JugadoresEnPartida").child(idPartida).child(idJugadorQueExpone);
        jugadorQueHaExpuesto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotDataChange) {
                for(DataSnapshot s : snapshotDataChange.getChildren()){
                    if (s.getKey().equals("haExpuesto")) {
                        if(s.getValue(Boolean.class).equals(false)) {
                            textoInformacionEsperandoExposicion.setText(nombreJugadorExponiendo + " está exponiendo...");
                        }
                        else if(s.getValue(Boolean.class).equals(true)) {
                            textoInformacionEsperandoExposicion.setText(nombreJugadorExponiendo + " terminó");
                            tituloEscucharExposicion.setText("Exposición lista");
                            progressBarEsperandoExposicion.setVisibility(View.INVISIBLE);
                            escucharExposicionButton.setVisibility(View.VISIBLE);
                        }
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