package ec.edu.utn.example.gpsmonitorapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle; // Necesario para onStatusChanged
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager; // Nueva importación

import ec.edu.utn.example.app_monitoreogps.R;

public class SensorDataCollectionService extends Service {
    private static final String TAG = "SensorDataCollection";
    private static final String CHANNEL_ID = "GPSMonitorChannel";
    private static final int NOTIFICATION_ID = 123; // ID único para la notificación del Foreground Service

    private LocationManager locationManager;
    private LocationListener locationListener; // Declarar aquí para que sea accesible en onDestroy

    @Override
    public void onCreate() {
        super.onCreate();
        // Crear el canal de notificación para Android O (API 26) y superior
        createNotificationChannel();
        // Iniciar el servicio como Foreground Service
        startForeground(NOTIFICATION_ID, buildNotification());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Inicializar el LocationListener aquí
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                long timestamp = System.currentTimeMillis();
                String deviceId = android.os.Build.MODEL;

                Log.d(TAG, "Lat: " + latitude + " Long: " + longitude + " Timestamp: " + timestamp + " Device ID: " + deviceId);

                // *** AHORA ENVIAMOS LOS DATOS A MAINACTIVITY ***
                Intent intent = new Intent("gps_data_update"); // Acción única para nuestro broadcast
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("timestamp", timestamp);
                LocalBroadcastManager.getInstance(SensorDataCollectionService.this).sendBroadcast(intent);

                // Aquí puedes guardar los datos en la base de datos SQLite
                // Por ejemplo: databaseHelper.saveLocation(latitude, longitude, timestamp, deviceId);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Este método está obsoleto en API 29+, pero es buena práctica mantenerlo
                Log.d(TAG, "Provider status changed: " + provider + ", status: " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "Provider disabled: " + provider);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Verificar si el permiso de ubicación está concedido antes de solicitar actualizaciones
        if (checkLocationPermission()) {
            try {
                // *** LLAMADA ÚNICA A requestLocationUpdates ***
                // Solicitar actualizaciones de ubicación: cada 5 segundos, o si la distancia cambia 10 metros
                // Usar FusedLocationProviderClient es más moderno, pero con LocationManager también funciona.
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000, // minTimeMs: tiempo mínimo entre actualizaciones (5 segundos)
                        10,   // minDistanceM: distancia mínima entre actualizaciones (10 metros)
                        locationListener
                );
                Log.d(TAG, "Solicitando actualizaciones de ubicación.");

            } catch (SecurityException e) {
                Log.e(TAG, "No se tienen los permisos necesarios para acceder a la ubicación.", e);
                stopSelf(); // Detener el servicio si no hay permisos
            }
        } else {
            Log.e(TAG, "Permiso de ubicación no concedido al iniciar el servicio. Deteniendo servicio.");
            stopSelf(); // Detener el servicio si no hay permisos
        }

        return START_STICKY; // El sistema intentará recrear el servicio si se destruye
    }

    // Método para crear el canal de notificación (necesario para Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "GPS Monitor Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    // Método para construir la notificación del Foreground Service
    private Notification buildNotification() {
        // Puedes añadir un PendingIntent para que al hacer clic en la notificación, se abra la MainActivity
        // Intent notificationIntent = new Intent(this, MainActivity.class);
        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitoreo de GPS Activo")
                .setContentText("Recolectando datos de ubicación en segundo plano.")
                .setSmallIcon(R.mipmap.ic_launcher) // Asegúrate de tener un icono
                // .setContentIntent(pendingIntent) // Descomentar si quieres que la notificación abra la app
                .setPriority(NotificationCompat.PRIORITY_LOW) // Prioridad baja para que no sea intrusiva
                .build();
    }

    // Verificar si el permiso de ubicación está concedido
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Este servicio no será un Bound Service
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // *** IMPORTANTE: Detener las actualizaciones de ubicación al destruir el servicio ***
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            Log.d(TAG, "Actualizaciones de ubicación detenidas.");
        }
        Log.d(TAG, "SensorDataCollectionService destruido.");
    }

}
