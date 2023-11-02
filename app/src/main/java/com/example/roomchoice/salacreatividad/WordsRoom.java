package com.example.roomchoice.salacreatividad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomchoice.R;
import com.example.roomchoice.modelo.Grupo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Hace la evaluacion de la creatividad independientemente de la modalidad elegida por el
 * usurario con el rol de RRHH.
 */
public class WordsRoom extends AppCompatActivity {

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);;
    FirebaseDatabase database;
    Button buttonHablar;
    Button buttonEnviar;
    EditText editTextTextMultiLine;
    TextView color;TextView pais;TextView verbo;TextView profesion;TextView objeto;TextView palabraSec;
    ArrayList<TextView> texts;
    private ArrayList<Grupo> grupos;
    private static final int numPalabrasMostrar = 5;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    int mode;
    long mom_comienzo;


    String idPartida;

    boolean primeraVez = true;

    private MediaRecorder grabacion;
    private String outputFile = null;

    List<String> palabrasClave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_words);
        mom_comienzo = System.currentTimeMillis();
        mode = this.getIntent().getIntExtra("mode", -1);
        idPartida = this.getIntent().getStringExtra("idPartida");
        grupos = new ArrayList<>();
        texts = new ArrayList<>();
        color = findViewById(R.id.palabra1TextView);
        pais = findViewById(R.id.palabra2TextView);
        objeto = findViewById(R.id.palabra3TextView);
        profesion = findViewById(R.id.palabra4TextView);
        verbo = findViewById(R.id.palabra5TextView);
        palabraSec = findViewById(R.id.palabra10sec);
        texts.add(color);texts.add(pais);texts.add(objeto);texts.add(profesion);texts.add(verbo);

        if(mode == 0){
            palabraSec.setText("");
        }
        else if(mode == 1){
            for(TextView t : texts)
                t.setText("");
        }
        buttonEnviar = findViewById(R.id.buttonEnviar);
        buttonHablar = findViewById(R.id.buttonHablar);
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine);
        setSpeechRecognizer();
        // Grabar la voz al mantener pulsado el boton
        buttonHablar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    //Log.d("BUTTON UP", "cierto");
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(primeraVez){
                        primeraVez = false;
                    }
                    //Log.d("BUTTON DOWN", "cierto");
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });


        database = FirebaseDatabase.getInstance("https://roomchoice-2f8fd-default-rtdb.europe-west1.firebasedatabase.app/");
        mostrarPalabras();


    }

    /**
     * Te muestra las palabras clave para evaluar la creatividad del empleado.
     */
    private void mostrarPalabras() {
        grupos = new ArrayList<>();
        DatabaseReference gruposRef = database.getReference().child("GruposSalaCreatividad");
        //Log.d("HOLA", gruposRef.toString());
        gruposRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("HOLAAA", "aaa");
                String palabra;
                ArrayList<String> palabras;
                for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                    palabras = new ArrayList<>();
                    for(DataSnapshot snap : singleSnapshot.getChildren()){
                        palabra = snap.getValue(String.class);
                        palabras.add(palabra);

                    }
                    grupos.add(new Grupo(palabras));
                }

                setPalabras();
                annadirPalabrasBD();
            }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
        });

    }

    /**
     * Almacena en la base de datos las palabras clave que le han tocado a un
     * determinado empleado
     */
    private void annadirPalabrasBD(){
        String palabras = "";
        for(String palabra : palabrasClave){
            palabras += palabra+" ";
        }
        database.getReference().child("JugadoresEnPartida").child(idPartida).child(user.getUid()).child("palabras").setValue(palabras);

    }

    /**
     * Poner la palabras clave en la interfaz, dependiendo del modo se muestra o las cinco palabras
     * o una cada 5 segundos (cinco veces)
     */
    private void setPalabras(){
        palabrasClave = new ArrayList<>();
        for(int i = 0 ; i < numPalabrasMostrar ; i++)
            Collections.shuffle(grupos.get(i).getPalabras());
        if(mode == 0) {
            for(int i = 0 ;  i < texts.size() ; i++) {
                texts.get(i).setText(grupos.get(i).getPalabras().get(0));
                palabrasClave.add(grupos.get(i).getPalabras().get(0));
            }
        }
        else if(mode == 1){
            new CountDownTimer(50000,10000){
                int j = 0;
                @Override
                public void onTick(long millis){
                    palabraSec.setText(grupos.get(j).getPalabras().get(0));
                    j++;
                    j %= texts.size();
                    palabrasClave.add(grupos.get(j).getPalabras().get(0));
                }
                @Override
                public void onFinish(){

                }
            }.start();

        }
    }

    /**
     * Reconocedor de voz, pasa de voz a texto
     */
    private void setSpeechRecognizer(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                if(editTextTextMultiLine.getText().length() != 0)
                    editTextTextMultiLine.append(". ");
                editTextTextMultiLine.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editTextTextMultiLine.append(data.get(0));
                Log.d("Valor del string escuchado " , data.toString());
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Al pulsar el boton de enviar se pide confirmacion y en caso afirmativo se almacena la
     * historia en la base de datos.
     * @param v
     */
    public void onClickSend(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Esta seguro de que quiere enviar la historia?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendHistory();
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

    /**
     * Se almacena la historia de un empleado con su puntuacion objetiva calculada mediante
     * algoritmos
     */
    public void sendHistory(){
        long mom_final = System.currentTimeMillis();
        double puntuacionPorVeces, puntuacionPorPalabras, puntuacionPorTiempo, puntuacionPorNumPalabras;
        long tiempo = mom_final - mom_comienzo;
        Log.d("Tiempo:", tiempo+"milisegundos");
        String historia = editTextTextMultiLine.getText().toString();
        historia = historia.toLowerCase();
        Map<String, Integer> diccionario = new HashMap<>();
        int numeroDePalabras = cuentaPalabrasHistoria(historia, diccionario);
        puntuacionPorVeces = puntuacionPorVecesPorPalabra(diccionario, numeroDePalabras, 10);
        puntuacionPorPalabras = puntuacionPorPalabrasClave(diccionario);
        puntuacionPorTiempo = puntuacionPorTiempo(tiempo);
        puntuacionPorNumPalabras = puntuacionPorNumeroPalabras(numeroDePalabras);

        double puntuacion = 0.2 * puntuacionPorPalabras + 0.3 * puntuacionPorTiempo + 0.2 * puntuacionPorVeces + 0.3 * puntuacionPorNumPalabras;
        puntuacion = Math.round(puntuacion * 100.0) / 100.0;

        database.getReference().child("JugadoresEnPartida").child(idPartida).child(user.getUid()).child("historia").setValue(historia);
        database.getReference().child("JugadoresEnPartida").child(idPartida).child(user.getUid()).child("puntuacionObjetiva").setValue(puntuacion);
        Intent intent = new Intent(getApplicationContext(), EsperarGrupo.class);
        intent.putExtra("idPartida", idPartida);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private int cuentaPalabrasHistoria(String historia, Map<String, Integer> diccionario){
        List<String> historiaEnPalabras = Arrays.asList(historia.split("[.,;: ]"));
        int numeroDePalabras = 0;
        Integer cont;
        for (String palabra: historiaEnPalabras) {
            cont = diccionario.getOrDefault(palabra, 1);
            diccionario.put(palabra, cont);
            numeroDePalabras++;
        }
        return numeroDePalabras;
    }

    /**
     * Dependiendo del numero de veces que se repita una palabra y del número de palabras que haya
     * se resta cierta puntuacion
     * @param diccionario
     * @param numeroDePalabras
     * @param maxThreshold
     * @return
     */
    private double puntuacionPorVecesPorPalabra(Map<String, Integer> diccionario, int numeroDePalabras, int maxThreshold){
        double puntuacion = 5.0;
        double puntuacionPorPalabra = 4.0 / numeroDePalabras;
        int veces;
        for (Entry<String,Integer> entry : diccionario.entrySet()){
            veces = entry.getValue();
            if(veces > 1 && veces < maxThreshold)
                puntuacion -= (puntuacionPorPalabra * (veces - 1)) / maxThreshold;
            else if(veces >= maxThreshold)
                puntuacion -= puntuacionPorPalabra * veces;
        }
        return puntuacion;
    }

    private double puntuacionPorPalabrasClave(Map<String, Integer> diccionario){
        double puntuacion= 5.0;
        for (String palabraClave: palabrasClave)
            if (!diccionario.containsKey(palabraClave))
                puntuacion -= 0.8;
        return puntuacion;
    }

    /**
     * Se utiliza una distribucion normal de media y desviacion estandar de 60 segundos
     * @param tiempo
     * @return
     */
    private double puntuacionPorTiempo(long tiempo){

        NormalDistribution normalDistribution = new NormalDistribution(60000,60000);
        double prob = 0.0;
        if(tiempo <= 60000){
            prob = normalDistribution.cumulativeProbability(tiempo);
        }else{
            prob = 1.0 - normalDistribution.cumulativeProbability(tiempo);
        }

        prob *= 2;
        double puntuacion = prob * 4 + 1;

        Log.d("Puntuacion por tiempo:", puntuacion+" en "+tiempo+" milisegundos");


        return puntuacion;
    }

    /**
     * Se puntua segun el numero de palabras, si hay pocas palabras se penaliza
     * @param numeroPalabras
     * @return
     */
    private double puntuacionPorNumeroPalabras(int numeroPalabras){
        double puntuacion = 5.0;
        if (numeroPalabras < 100){
            puntuacion = (double) (25 + numeroPalabras) / 25;
        }
        Log.d("Puntuacion por numero de palabras: ",puntuacion+" con "+numeroPalabras+" palabras");
        return puntuacion;
    }

    @Override
    public void onBackPressed() {

    }
}