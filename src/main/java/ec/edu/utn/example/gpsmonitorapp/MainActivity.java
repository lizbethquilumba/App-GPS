package ec.edu.utn.example.gpsmonitorapp;

import android.Manifest;
import android.content.BroadcastReceiver; // Nueva importación
import android.content.Context; // Nueva importación
import android.content.Intent;
import android.content.IntentFilter; // Nueva importación
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager; // Nueva importación

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

import ec.edu.utn.example.app_monitoreogps.R;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView statusTextView, gpsDataTextView, ipAddressTextView;
    private Button startButton, stopButton;

    private Handler handler;
    private Runnable updateIPRunnable;

    // *** NUEVO: Receptor para los datos GPS del servicio ***
    private BroadcastReceiver gpsDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("gps_data_update".equals(intent.getAction())) {
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);
                long timestamp = intent.getLongExtra("timestamp", 0); // Si lo envías

                // Actualizar la interfaz de usuario con los nuevos datos GPS
                updateGpsData(latitude, longitude, timestamp);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        statusTextView = findViewById(R.id.statusTextView);
        gpsDataTextView = findViewById(R.id.gpsDataTextView);
        ipAddressTextView = findViewById(R.id.ipAddressTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        handler = new Handler();

        startButton.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                // Cambia el estado a "activa" solo si los permisos están OK y el servicio se intenta iniciar
                statusTextView.setText("Recolección activa");
                Toast.makeText(MainActivity.this, "Recolección iniciada", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, SensorDataCollectionService.class));
                startIPUpdate();
            } else {
                requestLocationPermission();
            }
        });

        stopButton.setOnClickListener(v -> {
            statusTextView.setText("Recolección detenida");
            Toast.makeText(MainActivity.this, "Recolección detenida", Toast.LENGTH_SHORT).show();
            stopService(new Intent(MainActivity.this, SensorDataCollectionService.class));
            stopIPUpdate();
            // Opcional: limpiar los datos GPS mostrados al detener
            gpsDataTextView.setText("Últimos datos GPS: N/A");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // *** NUEVO: Registrar el BroadcastReceiver cuando la actividad está visible ***
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsDataReceiver, new IntentFilter("gps_data_update"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // *** NUEVO: Desregistrar el BroadcastReceiver cuando la actividad no está visible ***
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsDataReceiver);
    }

    // Verificar si los permisos de ubicación están concedidos
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Solicitar permisos de ubicación si no están concedidos
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Este permiso es necesario para acceder a la ubicación", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Manejar la respuesta del usuario al solicitar los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                // Si el permiso es concedido, iniciar el servicio y la actualización de IP
                statusTextView.setText("Recolección activa"); // Actualizar estado aquí también
                startService(new Intent(MainActivity.this, SensorDataCollectionService.class));
                startIPUpdate();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Recolección detenida (Permiso denegado)"); // Mostrar estado claro
            }
        }
    }

    // Iniciar la actualización de IP local
    private void startIPUpdate() {
        // Asegurarse de que no haya un Runnable anterior para evitar duplicados
        if (updateIPRunnable != null) {
            handler.removeCallbacks(updateIPRunnable);
        }
        updateIPRunnable = new Runnable() {
            @Override
            public void run() {
                String ip = getLocalIpAddress();
                ipAddressTextView.setText("Dirección IP: " + ip);
                handler.postDelayed(this, 5000); // Actualizar cada 5 segundos
            }
        };
        handler.post(updateIPRunnable); // Iniciar la actualización de IP
    }

    // Detener la actualización de IP local
    private void stopIPUpdate() {
        if (updateIPRunnable != null) {
            handler.removeCallbacks(updateIPRunnable);
        }
    }

    // Obtener la dirección IP local del dispositivo
    private String getLocalIpAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress inetAddress : Collections.list(ni.getInetAddresses())) {
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) { // isSiteLocalAddress() para IPv4 local
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP_ADDRESS", "Error getting IP address", ex);
        }
        return "N/A";
    }

    // Método para actualizar los datos GPS (llamado desde el BroadcastReceiver)
    public void updateGpsData(double latitude, double longitude, long timestamp) {
        gpsDataTextView.setText(String.format("Últimos datos GPS: Lat: %.4f, Long: %.4f", latitude, longitude));
        // Opcional: Puedes añadir el timestamp si lo necesitas
        // gpsDataTextView.append("\nTimestamp: " + new java.util.Date(timestamp));
    }
}