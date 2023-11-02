package com.example.roomchoice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roomchoice.modelo.Usuario;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

/**
 * La clase AuthActivity permite al usuario registrarse en la aplicación con una cuenta de Google e iniciar sesión
 * en la aplicación si ya se ha registrado previamente
 * @author
 * @author
 * @author
 * @author
 */

public class AuthActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usuarios;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    private final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build());

        private static final String TAG = "FirebaseAuthActivity";
    private static final String ERROR_LOG_IN = "Error login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_activity);
        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
    }

    /**
     * Registra el evento de pulsar el botón "Iniciar Sesión" en activity_auth_activity.xml.
     * Lanza la activitidad que gestiona el inicio de sesión de un usuario en su cuenta de Google
     * @param view El botón pulsado
     */
    public void onClickIniciarSesion(View view){
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_AppCompat_Light_NoActionBar)
                .setLogo(R.mipmap.room_choice)
                .build();
        signInLauncher.launch(signInIntent);
    }

    /*Comprueba que el inicio de sesión se haya realizado de manera correcta y, en caso
    * contrario, devuelve un error*/
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            comprobarNuevoUsuario();
        } else {
            if(result.getResultCode() == RESULT_CANCELED) {
                Log.d(TAG, ERROR_LOG_IN);
                Intent intent = new Intent(this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }
    }

    //Comprueba si el usuario estaba registrado en la base de datos
    private void comprobarNuevoUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        usuarios = database.getReference().child("Usuarios");
        usuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean existe = false;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Usuario usuario = singleSnapshot.getValue(Usuario.class);
                    if(user.getEmail().equals(usuario.getEmail()))
                        existe = true;
                }
                if(!existe)
                    introducirUsuarioAFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO
            }
        });
        startActivity(new Intent(this, LoggedInActivity.class));
    }

    /*Introduce un nuevo usuario en la base de datos con los datos del usuario que
    está actualmente usando la aplicación*/
    private void introducirUsuarioAFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Usuario user = new Usuario(currentUser.getDisplayName(), currentUser.getEmail(), currentUser.getPhoneNumber());
        database.getReference().child("Usuarios/"+currentUser.getUid()).setValue(user);
    }

}