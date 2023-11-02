package com.example.roomchoice;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

/**
 * La clase PantallaDeArranque es parte del SplashScreen y su función es mostrar el icono
 * de la aplicación al arrancarla
 * @author
 * @author
 * @author
 * @author
 */
public class PantallaDeArranque extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, AuthActivity.class));
    }
}