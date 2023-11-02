package com.example.roomchoice.salaaptitudes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomchoice.LoggedInActivity;
import com.example.roomchoice.R;
import com.example.roomchoice.modelo.Estado;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class SalaAptitudesRRHH extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference partidaEnJuego;
    private DatabaseReference jugadoresEnPartida;

    private TextView tituloPantalla;
    private TextView tituloListaJugadores;
    private TextView informacionJuegoSalaAptitudes;
    private ListView listaJugadores;

    private String roomCode;
    private String idPartida;
    private int juego;
    private int numJugadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_aptitudes_rrhh);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");

        roomCode = this.getIntent().getStringExtra("roomCode");
        idPartida = this.getIntent().getStringExtra("idPartida");
        juego = this.getIntent().getIntExtra("juego", 0);
        jugadoresEnPartida = database.getReference().child("JugadoresEnPartida").child(idPartida);

        tituloPantalla = findViewById(R.id.tituloPantalla);
        tituloListaJugadores = findViewById(R.id.tituloListaJugadores);
        informacionJuegoSalaAptitudes = findViewById(R.id.informacionJuegoSalaAptitudes);
        listaJugadores = findViewById(R.id.listaJugadores);
        tituloPantalla.append(" " + roomCode);

        if(juego==1)
            tituloListaJugadores.setText(R.string.tituloListaJugadoresEvaluadosSalaAptitudesRRHH);
        else
            tituloListaJugadores.setText(R.string.tituloListaJugadoresQueTerminanSalaAptitudesRRHH);
        estadoPartida();
    }

    /**
     * Registra el evento de pulsar el botón "Terminar Juego" en activity_elegir_juego_sala_aptitudes.xml
     * Si todos los jugadores han terminado, se muestra un diálogo de confirmacion para terminar la partida.
     * Si no han terminado todos, se indica y no sucede nada.
     * empleado de recursos humanos.
     * @param view El botón pulsado
     */
    public void onClickTerminarJuego(View view) {
        if(numJugadores==listaJugadores.getCount()) {
            mostrarDialogoDeConfirmacion();
        } else {
            Toast.makeText(getApplicationContext(), "Aún no han terminado todos los jugadores", Toast.LENGTH_SHORT).show();
        }
    }

    /* Se revisa qué jugador ha expuesto, quién está exoniendo y quíen está siendo evaluado y
     * y se indica brevemente.*/
    private void estadoPartida() {
        jugadoresEnPartida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotPartidaEnJuego) {
                ArrayList<String> listaJugadoresArrayList = new ArrayList<>();
                HashMap<String, Object> jugadorAnterior = new HashMap<>();
                HashMap<String, Object> jugador;
                int contador = 0;
                numJugadores = (int)snapshotPartidaEnJuego.getChildrenCount();
                informacionJuegoSalaAptitudes.setText("");
                for(DataSnapshot jugadorSnapshot: snapshotPartidaEnJuego.getChildren()) {
                    jugador = (HashMap<String, Object>) jugadorSnapshot.getValue();
                    if(juego == 1) {
                        if((contador==0 && jugador.get("haExpuesto").equals(false)) ||
                                (jugador.get("haExpuesto").equals(false) && jugadorAnterior.get("haSidoEvaluado").equals(true))) {
                            informacionJuegoSalaAptitudes.append(jugador.get("nombre") + " está exponiendo...");
                        }
                        if(jugador.get("haExpuesto").equals(true) && jugador.get("haSidoEvaluado").equals(false)) {
                            informacionJuegoSalaAptitudes.append(jugador.get("nombre") + " está siendo evaluado...");
                        }
                        if(jugador.get("haExpuesto").equals(true) && jugador.get("haSidoEvaluado").equals(true)) {
                            listaJugadoresArrayList.add(String.valueOf(jugador.get("nombre")));
                        }
                        jugadorAnterior = jugador;
                        contador++;
                    } else {
                        for (DataSnapshot jS : jugadorSnapshot.getChildren()) {
                            if (jS.getKey().equals("test2Terminado") && jugador.get("test2Terminado").equals(true)) {
                                informacionJuegoSalaAptitudes.append(jugador.get("nombre") + " ha terminado");
                                listaJugadoresArrayList.add(String.valueOf(jugador.get("nombre")));
                            }
                        }
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SalaAptitudesRRHH.this,
                        android.R.layout.simple_list_item_1, listaJugadoresArrayList){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        TextView item = (TextView) super.getView(position,convertView,parent);
                        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
                        return item;
                    }
                };
                listaJugadores.setAdapter(adapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Muestra un breve diálogo de confirmación.
     * Si se selecciona Si, se termina la partida y se vuelve a la pantalla principal
     * Si no, no sucede nada.*/
    private void mostrarDialogoDeConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere salir de la partida?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), LoggedInActivity.class);
                i.putExtra("roomCode", roomCode);
                i.putExtra("idPartida", idPartida);
                i.putExtra("inicio", false);
                startActivity(i);
            }}).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }});
        builder.show();
    }

    @Override
    public void onBackPressed() {

    }
}

