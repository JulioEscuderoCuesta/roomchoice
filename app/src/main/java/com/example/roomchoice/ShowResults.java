package com.example.roomchoice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * La clase ShowResult muestra los resultados de las partidas registradas en la base de datos.
 * @author
 * @author
 * @author
 * @author
 */
public class ShowResults extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usuarios;
    private DatabaseReference partidas;
    private DatabaseReference jugadoresEnPartida;

    private ListView listResults;
    private ArrayAdapter<String> adapter;
    private ConcurrentHashMap<String, String> infoPartida;
    private ArrayList<String> listaResultadosPartidas;

    private String info;
    private String email;
    private String codigo;
    private String estado;
    private Double puntuacion = null;
    private String puntuacionString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_results);

        listResults = findViewById(R.id.listViewResults);

        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        usuarios = database.getReference().child("Usuarios");
        partidas = database.getReference().child("Partidas");
        jugadoresEnPartida = database.getReference().child("JugadoresEnPartida");

        listaResultadosPartidas = new ArrayList<>();
        infoPartida = new ConcurrentHashMap<>();
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, listaResultadosPartidas);
        listResults.setAdapter(adapter);
        mostrarResultados();
    }


    private void mostrarResultados(){
        partidas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot todasLasPartidas) {
                HashSet<String> existe = new HashSet<>();
                for (DataSnapshot part : todasLasPartidas.getChildren()) {
                    existe.add(part.getKey());
                }
                for(String s: infoPartida.keySet()){
                    if(!existe.contains(s)){
                        infoPartida.remove(s);
                        listaResultadosPartidas.remove(s);
                        adapter.notifyDataSetChanged();
                    }
                }
                for (DataSnapshot part : todasLasPartidas.getChildren()) {
                    encontrarJugadoresEnPartida(part);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Recoge la información de los jugadores en la partida y la añade a la lista
     * que se va a mostrar en pantalla */
    private void encontrarJugadoresEnPartida(DataSnapshot part){
        jugadoresEnPartida.child(part.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot jugadores) {
                listaResultadosPartidas.remove(infoPartida.get(part.getKey()));
                for(DataSnapshot partidaAttributos : part.getChildren()){
                    if (partidaAttributos.getKey().equals("codigo")){
                        codigo = partidaAttributos.getValue(String.class);
                    }else if(partidaAttributos.getKey().equals("estado")){
                        estado = partidaAttributos.getValue(String.class);
                    }
                }
                info = "Sala " + codigo + "\nEstado: " + estado.toLowerCase(Locale.ROOT) +"\n";
                for(DataSnapshot jugador : jugadores.getChildren()) {
                    for (DataSnapshot jugadorAttributos : jugador.getChildren()){
                        if(jugadorAttributos.getKey().equals("puntuacion")){
                            puntuacion = jugadorAttributos.getValue(Double.class);
                        }else if(jugadorAttributos.getKey().equals("email")){
                            email = jugadorAttributos.getValue(String.class);
                        }
                    }
                    if(puntuacion == null) puntuacionString = "No existe aún";
                    else puntuacionString = puntuacion.toString();
                    info += email + ": " + puntuacionString + "\n";
                }
                infoPartida.put(part.getKey(), info);
                listaResultadosPartidas.add(info);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Registra el evento de pulsar el botón "Volver" en activity_creacion_sala.xml
     * Carga la actividad con la clase LoggedInActivity.
     * @param view El botón pulsado
     */
    public void onClickVolverMenu(View view) {
        startActivity(new Intent(getApplicationContext(), LoggedInActivity.class));
    }
}