package com.example.roomchoice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomchoice.modelo.Estado;
import com.example.roomchoice.salaaptitudes.ElegirJuegoSalaAptitudes;
import com.example.roomchoice.salaaptitudes.EsperarJuegoSalaAptitudes;
import com.example.roomchoice.salacreatividad.ElegirModalidadSalaCreatividad;
import com.example.roomchoice.salacreatividad.EsperarModalidad;
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

/**
 * La clase CreacionSala crea una nueva sala para jugar y gestiona qué modalidad se jugará
 * @author
 * @author
 * @author
 * @author
 */
public class CreacionSala extends AppCompatActivity {

    private ListView listView;
    private TextView labelCod;
    private Button salaCreatividadButton;
    private Button salaAptitudesButton;
    private ArrayList<String> listaJugadores;
    private boolean esRRHH;
    private boolean haEntrado;

    private String roomCode ="";
    private FirebaseDatabase database;
    private DatabaseReference partidas;
    private DatabaseReference jugadoresEnPartida;
    private DatabaseReference comenzarPartida;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String key = "";
    private String idPartida = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creacion_sala);

        listView = findViewById(R.id.listOfParticipants);
        labelCod = findViewById(R.id.labelCode);
        salaCreatividadButton = findViewById(R.id.salaCreatividadButton);
        salaAptitudesButton = findViewById(R.id.salaAptitudesButton);
        roomCode = this.getIntent().getStringExtra("roomCode");
        esRRHH = this.getIntent().getBooleanExtra("esRRHH", false);
        haEntrado = false;
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        showPlayersListener();
        if(!esRRHH) {
            ocultarBotones();
            esperarInicioPartida();
        }
    }

    /**
     * Registra el evento de pulsar el botón "Sala Creatividad" en activity_creacion_sala.xml
     * Marca como seleccionado el botón y como no seleccionado el botón "Sala Aptitudes"
     * e inicia el procedimiento para comenzar la partida
     * @param view El botón pulsado
     */
    public void optionSalaCreatividadClicked(View view) {
        salaCreatividadButton.setSelected(true);
        salaAptitudesButton.setSelected(false);
        onClickStart();

    }

    /**
     * Registra el evento de pulsar el botón "Sala Aptitudes" en activity_creacion_sala.xml
     * Marca como seleccionado el botón y como no seleccionado el botón "Sala Creatividad"
     * e inicia el procedimiento para comenzar la partida
     * @param view El botón pulsado
     */
    public void optionSalaAptitudesClicked(View view) {
        salaAptitudesButton.setSelected(true);
        salaCreatividadButton.setSelected(false);
        onClickStart();
    }

    //En la base de datos, se marca a qué sala se jugará
    private void onClickStart() {
        if(listaJugadores.size() > 1) {
            partidas = database.getReference().child("Partidas");
            partidas.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String idPartida;
                    for (DataSnapshot snapshotPartida : snapshot.getChildren()) {
                        HashMap<String, Object> partida = (HashMap<String, Object>) snapshotPartida.getValue();
                        String codigo = String.valueOf(partida.get("codigo"));
                        if (codigo.equals(roomCode)) {
                            //Marcar la partida como COMENZADA para que los jugadores vayan a la siguiente pantalla
                            idPartida = snapshotPartida.getKey();
                            HashMap<String, Object> comienzaPartida = new HashMap<>();
                            if (salaCreatividadButton.isSelected())
                                comienzaPartida.put("estado", Estado.COMENZADASALACREATIVIDAD);
                            else if (salaAptitudesButton.isSelected())
                                comienzaPartida.put("estado", Estado.COMENZADASALAAPTITUDES);
                            comenzarPartida = database.getReference().child("Partidas").child(idPartida);
                            comenzarPartida.updateChildren(comienzaPartida);
                            passExtras(idPartida);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "No hay suficientes jugadores", Toast.LENGTH_SHORT).show();
        }

    }

    //Se obtiene el id de la partida creada para posteriormente pasarlo a las siguientes actividades
    private void getIdPartida(){
        partidas = database.getReference().child("Partidas");
        partidas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotPartida : snapshot.getChildren()) {
                    HashMap<String, Object> partida = (HashMap<String, Object>) snapshotPartida.getValue();
                    String codigo = String.valueOf(partida.get("codigo"));
                    if (codigo.equals(roomCode)) {
                        //Marcar la partida como COMENZADA para que los jugadores vayan a la siguiente pantalla
                        idPartida = snapshotPartida.getKey();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Dependiendo de qué tipo de sala se haya seleccionado, se avanza a una actividad o a otra
    private void passExtras(String idPartida){
        if(salaCreatividadButton.isSelected()) {
            Intent intent = new Intent(getApplicationContext(), ElegirModalidadSalaCreatividad.class);
            intent.putExtra("esRRHH", esRRHH);
            intent.putExtra("roomCode", roomCode);
            intent.putExtra("idPartida", idPartida);
            startActivity(intent);
        }
        else if(salaAptitudesButton.isSelected()) {
            Intent intent = new Intent(getApplicationContext(), ElegirJuegoSalaAptitudes.class);
            intent.putExtra("inicio", true);
            intent.putExtra("roomCode", roomCode);
            intent.putExtra("idPartida", idPartida);
            startActivity(intent);
        }
    }

    /* Un listener que se crea nada más el usuario entra en la clase que actualiza en pantalla
     * la lista de jugadores que se han unido a la sala*/
    private void showPlayersListener() {
        labelCod.append(roomCode);
        partidas = database.getReference().child("Partidas");

        partidas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotPartidas) {

                for (DataSnapshot snapshotPartidas : dataSnapshotPartidas.getChildren()) {
                    HashMap<String, Object> partida = (HashMap<String, Object>) snapshotPartidas.getValue();
                    String codigo = String.valueOf(partida.get("codigo"));
                    if (codigo.equals(roomCode)) {
                        key = snapshotPartidas.getKey();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        jugadoresEnPartida = database.getReference().child("JugadoresEnPartida");
        jugadoresEnPartida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotJugadoresEnPartida) {
                String datosJugador;
                listaJugadores = new ArrayList<>();
                for(DataSnapshot snapshotJugadoresEnPartida: dataSnapshotJugadoresEnPartida.getChildren()) {
                    if (snapshotJugadoresEnPartida.getKey().equals(key)) {
                        for(DataSnapshot jugadorSnapshot : snapshotJugadoresEnPartida.getChildren()){
                            datosJugador = jugadorSnapshot.child("nombre").getValue() +": "
                                    + jugadorSnapshot.child("email").getValue();
                            listaJugadores.add(datosJugador);
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreacionSala.this,
                        android.R.layout.simple_list_item_1, listaJugadores){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        TextView item = (TextView) super.getView(position,convertView,parent);
                        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        return item;
                    }
                };
                listView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /* Oculta los botones de selección Sala Creatividad y Sala Aptitudes para que el usuario
     * que no sea de recursos humanos no pueda verlos*/
    private void ocultarBotones() {
        salaCreatividadButton.setVisibility(View.GONE);
        salaAptitudesButton.setVisibility(View.GONE);
    }

    /* Un listener que se crea si el usuario no es de recursos humanos y que se mantiene
     * a la espera de detectar un cambio en el estado de la partida.
     * Si se detecta que la partida será en la sala de creatividad, se cargará la correspondiente activity.
     * Si se detecta que la partida será en la sala de aptitudes, se cargará la correspondiente activity.*/
    private void esperarInicioPartida() {
        partidas = database.getReference().child("Partidas");
        partidas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!haEntrado){
                    String idPartida;
                    for(DataSnapshot snapshotPartida: snapshot.getChildren()) {
                        HashMap<String, Object> partida = (HashMap<String, Object>)snapshotPartida.getValue();
                        String codigo = String.valueOf(partida.get("codigo"));
                        if(codigo.equals(roomCode)) {
                            idPartida = snapshotPartida.getKey();
                            comenzarPartida = database.getReference().child("Partidas").child(idPartida);
                            String finalIdPartida = idPartida;
                            comenzarPartida.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    HashMap<String, Object> atributosPartida = (HashMap<String, Object>)snapshot.getValue();
                                    String estadoPartida = String.valueOf(atributosPartida.get("estado"));
                                    if(Estado.valueOf(estadoPartida).equals(Estado.COMENZADASALACREATIVIDAD)) {
                                        haEntrado = true;
                                        Intent intent = new Intent(getApplicationContext(), EsperarModalidad.class);
                                        intent.putExtra("roomCode", roomCode);
                                        intent.putExtra("idPartida", finalIdPartida);
                                        startActivity(intent);
                                    }
                                    else if(Estado.valueOf(estadoPartida).equals(Estado.COMENZADASALAAPTITUDES)) {
                                        haEntrado = true;
                                        Intent intent = new Intent(getApplicationContext(), EsperarJuegoSalaAptitudes.class);
                                        intent.putExtra("roomCode", roomCode);
                                        intent.putExtra("idPartida", finalIdPartida);
                                        startActivity(intent);
                                    } else if(Estado.valueOf(estadoPartida).equals(Estado.DESTRUIDA)) {
                                        startActivity(new Intent(getApplicationContext(), LoggedInActivity.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Gestiona el pulsado del botón atrás en el móvil
     * Muestra un diálogo de confirmación y si se selecciona "si", se abandona la sala
     * Si se selecciona no, no sucede nada.
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Desea salir de la sala?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        abandonarSala();
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

    // Elimina el usuario en la partida de la base de datos y recarga la activity anterior
    private void abandonarSala() {
        getIdPartida();
        String idPartida = this.idPartida;
        if(!esRRHH) {
            database.getReference().child("JugadoresEnPartida").child(idPartida+ "/" + user.getUid()).removeValue();
            database.getReference().child("Partidas").child(idPartida).child("jugActuales").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer numJug = currentData.getValue(Integer.class);
                    currentData.setValue(numJug - 1);
                    Intent intent = new Intent(getApplicationContext(), LoggedInActivity.class);
                    startActivity(intent);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    startActivity(new Intent(getApplicationContext(), LoggedInActivity.class));
                }
            });
        } else {
            database.getReference().child("Partidas").child(idPartida).child("estado").setValue(Estado.DESTRUIDA);
            startActivity(new Intent(getApplicationContext(), LoggedInActivity.class));
        }
    }
}
