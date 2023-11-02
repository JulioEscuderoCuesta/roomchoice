package com.example.roomchoice.salacreatividad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.roomchoice.CreacionSala;
import com.example.roomchoice.R;
import com.example.roomchoice.modelo.Estado;
import com.example.roomchoice.modelo.Modalidad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Se encarga de fijar la modalidad elegida por el rol RRHH en la base de datos.
 */
public class ElegirModalidadSalaCreatividad extends AppCompatActivity {

    FirebaseDatabase database;
    String idPartida;
    String roomCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir_modalidad_sala_creatividad);
        //Log.d("ELEGIR MODALIDAD roomCode", this.getIntent().getStringExtra("roomCode"));
        //Log.d("ELEGIR MODALIDAD idPartida",this.getIntent().getStringExtra("idPartida"));

        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        idPartida = this.getIntent().getStringExtra("idPartida");
        roomCode = this.getIntent().getStringExtra("roomCode");

    }

    /**
     * Elige la modalidad 5 palabras o FiveWORDS y si el usuario tiene rol RRHH se le muestra
     * un mensaje, si es empleado se le envía a WordsRoom con el modo a 0
     * @param view
     */
    public void startFiveWords(View view){
        database.getReference().child("Partidas").child(idPartida).child("modalidad").setValue(Modalidad.FWORDS);
        if(!this.getIntent().getBooleanExtra("esRRHH",false)) {
            Intent intent = new Intent(getApplicationContext(), WordsRoom.class);
            intent.putExtra("mode", 0);
            intent.putExtra("roomCode", this.getIntent().getStringExtra("roomCode"));
            intent.putExtra("idPartida", this.getIntent().getStringExtra("idPartida"));
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Sus empleados acabaran y enviaran un correo", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Elige la modalidad 5 palabras o ShootingWORDS y si el usuario tiene rol RRHH se le muestra
     * un mensaje, si es empleado se le envía a WordsRoom con el modo a 1
     * @param view
     */
    public void startShootingWords(View view){
        database.getReference().child("Partidas").child(idPartida).child("modalidad").setValue(Modalidad.SWORDS);
        if(!this.getIntent().getBooleanExtra("esRRHH",false)) {
            Intent intent = new Intent(getApplicationContext(), WordsRoom.class);
            intent.putExtra("mode", 1);
            intent.putExtra("roomCode", this.getIntent().getStringExtra("roomCode"));
            intent.putExtra("idPartida", this.getIntent().getStringExtra("idPartida"));
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Sus empleados acabaran y enviaran un correo", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
    }
}