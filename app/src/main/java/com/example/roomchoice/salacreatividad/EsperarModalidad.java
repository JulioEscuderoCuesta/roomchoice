package com.example.roomchoice.salacreatividad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.roomchoice.R;
import com.example.roomchoice.modelo.Modalidad;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Gestiona la espera de los empleados. Estos esperan a que el encargado de recursos humanos o RRHH
 * haya elegido modalidad.
 */
public class EsperarModalidad extends AppCompatActivity {

    String idPartida;

    FirebaseDatabase database;

    DatabaseReference partida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esperar_modalidad);

        idPartida = getIntent().getStringExtra("idPartida");

        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");

        partida = database.getReference().child("Partidas").child(idPartida);
        partida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent = new Intent (getApplicationContext(), WordsRoom.class);
                Log.d("ID partida", idPartida);
                if(snapshot.child("modalidad").getValue() != null) {
                    if (snapshot.child("modalidad").getValue().equals(Modalidad.FWORDS.toString())) {
                        intent.putExtra("mode", 0);
                    } else if (snapshot.child("modalidad").getValue().equals(Modalidad.SWORDS.toString())) {
                        intent.putExtra("mode", 1);
                    }
                    intent.putExtra("idPartida", idPartida);
                    startActivity(intent);
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