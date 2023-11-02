package com.example.roomchoice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.roomchoice.modelo.Partida;
import com.example.roomchoice.modelo.Rol;
import com.example.roomchoice.modelo.Usuario;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * La clase LoggedInActivity surge tras iniciar sesión en la aplicación y posibilita la opción
 * de crear una nueva sala y de acceder a los registros de otras partidas al empleado de recursos humanos.
 * Permite al usuario que no es de recursos humanos acceder a una activity para unirse a una sala.
 * Ambos pueden acceder a su perfil para ver sus datos.
 */
public class LoggedInActivity extends AppCompatActivity {

    private static final Integer RecordAudioRequestCode = 1;
    private static final Integer WriteExternalRequestCode = 2;

    private Button createRoomButton;
    private Button joinRoomButton;
    private Button perfilButton;
    private Button showResultsButton;
    private TextView nombreBienvenida;

    private final boolean[] esRRHH = {true};

    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference usuarios;
    private DatabaseReference partidas;

    private String roomCode = "";
    private boolean codigoUnico = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");

        setContentView(R.layout.activity_logged_in);
        nombreBienvenida = findViewById(R.id.nombre);
        createRoomButton = findViewById(R.id.createRoom);
        joinRoomButton = findViewById(R.id.joinRoom);
        perfilButton = findViewById(R.id.perfilButton);
        showResultsButton = findViewById(R.id.showResults);
        user = FirebaseAuth.getInstance().getCurrentUser();
        nombreBienvenida.setText("Hola, "+user.getDisplayName()+".");
        tipoEmpleado();
    }

    /**
     * Registra el evento de pulsado en el botón "Perfil" en activity_logged.xml
     * Comienza una nueva actividad con la clase Perfil
     * @param view El botón pulsado
     */
    public void onClickLogOut(View view) {
        AuthUI.getInstance().signOut(this);
        startActivity(new Intent(this, AuthActivity.class));
    }

    //Devuelve un código aleatorio compuesto por caracteres alfanuméricos sin incluir mayúsculas
    private String generarCodigo(int length){
        return RandomStringUtils.random(length, "0123456789abcdefghijklmnopqrstuwxyz");
    }

    /**
     * Registra el evento de pulsado en el botón "Crear Sala" en activity_logged.xml
     * Comprueba que el código generado para la sala es único y procede a crear una nueva partida.
     * @param view El botón pulsado
     */
    public void onClickCreateRoom(View view) {
        partidas = database.getReference().child("Partidas");
            partidas.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshotPartidas) {
                    while(!codigoUnico){
                        roomCode = generarCodigo(5);
                        codigoUnico = true;
                        for (DataSnapshot snapshotPartidas : dataSnapshotPartidas.getChildren()) {
                            Log.d("codigo","partidas");
                            String codigo = snapshotPartidas.child("codigo").getValue(String.class);
                            Log.d("codigo",codigo);
                            if (codigo.equals(roomCode)) {
                                codigoUnico = false;
                                break;
                            }
                        }
                    }
                    crearPartida();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    //Crea una nueva partida en la base de datos y avanza a la actividad con la clase CreacionSala
    private void crearPartida(){
        String key = database.getReference().child("Partidas").push().getKey();

        ArrayList<Usuario> jugadores = new ArrayList<>();

        Partida partida = new Partida(roomCode, LocalDate.now().toString(), 4, 0, jugadores);
        Map<String, Object> postValues = partida.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Partidas/" + key, postValues);

        database.getReference().updateChildren(childUpdates);

        Intent intent = new Intent(getApplicationContext(), CreacionSala.class);
        intent.removeExtra("roomCode");
        intent.removeExtra("esRRHH");
        intent.putExtra("roomCode", roomCode);
        intent.putExtra("esRRHH", esRRHH[0]);
        startActivity(intent);
    }

    /**
     * Registra el evento de pulsado en el botón "Unirse a Sala" en activity_logged.xml
     * Comprueba que el código generado para la sala es único y procede a crear una nueva partida.
     * @param view El botón pulsado
     */
    public void onClickJoinRoom(View view) {
        Intent intent = new Intent(getApplicationContext(), UnirASala.class);
        intent.putExtra("esRRHH", esRRHH[0]);
        startActivity(intent);

    }

    /* Si el usuario es un empleado de recursos humanos, muestra los botones para
     * crear nueva partida y para mostrar resultados de otras partidas.
     * Si no es de recursos humanos, muestra solo un botón para unirse a una partida
     * y además, comprueba que la aplicación tiene permisos para acceder al micrófono
     * y al almacenamiento externo */
    private void tipoEmpleado() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        usuarios = database.getReference().child("Usuarios");
        usuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Usuario usuario = singleSnapshot.getValue(Usuario.class);
                    if(usuario.getEmail().equals(user.getEmail())) {
                        if(usuario.getRol().equals(Rol.EMPLEADO)) {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                                checkPermission();
                            esRRHH[0] = false;
                            createRoomButton.setVisibility(View.INVISIBLE);
                            joinRoomButton.setVisibility(View.VISIBLE);
                        }
                        else if (usuario.getRol().equals(Rol.RRHH)){
                            createRoomButton.setVisibility(View.VISIBLE);
                            showResultsButton.setVisibility(View.VISIBLE);
                            joinRoomButton.setVisibility(View.INVISIBLE);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO
            }
        });
    }

    /**
     * Registra el evento de pulsado en el botón "VER RESULTADOS" en activity_logged.xml
     * Avanza a la actividad con la clas ShowResults que mostrará resultados de otras partidas.
     * @param view El botón pulsado
     */
    public void onClickShowResults(View view){
        Intent intent = new Intent(getApplicationContext(), ShowResults.class);
        intent.removeExtra("roomCode");
        intent.removeExtra("esRRHH");
        intent.putExtra("roomCode", roomCode);
        intent.putExtra("esRRHH", esRRHH[0]);
        startActivity(intent);
    }

    /* Si la versión del SDK lo permite, proporciona persmisos a la aplicación para acceder al micrófono
     * y al almacenamiento externo */
    private void checkPermission () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WriteExternalRequestCode);
        }
    }


}
