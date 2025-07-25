package ec.edu.utn.example.gpsmonitorapp;

import fi.iki.elonen.NanoHTTPD;
import java.util.HashMap;

public class WebServerService extends NanoHTTPD {
    private HashMap<String, String> sensorData;
    private static final String TOKEN = "mi_token_secreto"; // Token fijo por simplicidad
    public WebServerService() {
        super(8080); // Puerto donde escuchará el servidor
        sensorData = new HashMap<>();
        sensorData.put("sensor_data", "example_sensor_data"); // Ejemplo de datos
    }

    @Override
    public Response serve(IHTTPSession session) {
        String authHeader = session.getHeaders().get("authorization");
        if (authHeader == null || !authHeader.equals("Bearer " + TOKEN)) {
            return newFixedLengthResponse("Unauthorized");
        }

        String uri = session.getUri();
        if (uri.equals("/api/sensor_data")) {
            return newFixedLengthResponse("Datos del Sensor: " + sensorData.get("sensor_data"));
        } else if (uri.equals("/api/device_status")) {
            return newFixedLengthResponse("Estado del Dispositivo: OK");
        }
        return newFixedLengthResponse("Endpoint no encontrado");
    }
}

