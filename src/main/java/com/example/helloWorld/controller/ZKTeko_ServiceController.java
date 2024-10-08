package com.example.helloWorld.controller;

import com.example.helloWorld.mapping.FingerprintMapping;
import com.example.helloWorld.model.CSO_CTT_TRABAJADOR;
import com.example.helloWorld.model.Fingerprint;
import com.example.helloWorld.model.User;
import com.example.helloWorld.service.FTPService;
import com.example.helloWorld.service.Impl.CsoCttTrabajadorServiceImpl;
import com.example.helloWorld.service.Impl.FingerPrintServiceImpl;
import com.example.helloWorld.service.Impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api/fingerprint")
public class ZKTeko_ServiceController {

    @Autowired
    private FingerPrintServiceImpl fingerPrintService;

    @Autowired
    private FTPService ftpService;

    @Autowired
    private CsoCttTrabajadorServiceImpl csoCttTrabajadorService;


    @Autowired
    private UserServiceImpl userService;
    private FingerprintMapping fingerprintMapping;

    // Crear un nuevo eventofingerprint
    @GetMapping("/hello")
    public String helloWorld() {
        return "Hola Mundo";
    }

    @GetMapping("/check-ftp-connection")
    public String checkFTPConnection() {
        boolean isConnected = ftpService.checkFTPConnection();
        return isConnected ? "Connection successful" : "Connection failed";
    }

    @GetMapping("/capture")
    public ResponseEntity<String> captureFingerprint(@RequestParam("nombre") String fingerprint) {


        try {
            byte[] fingerprintTemplate = fingerPrintService.captureFingerprint(fingerprint);
            if (fingerprintTemplate == null) {
                return new ResponseEntity<>("Failed to capture fingerprint", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // Convert byte[] to Base64 string for easier representation
            String base64Fingerprint = java.util.Base64.getEncoder().encodeToString(fingerprintTemplate);
            String filePath = fingerprint+".bmp";

            System.out.println(filePath);
            // Leer el archivo BMP en un array de bytes
            File file = new File(filePath);
            ftpService.uploadFile(file);
            byte[] fileContent = Files.readAllBytes(file.toPath());

            // Convertir el array de bytes a Base64
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            // Imprimir la cadena Base64
            // System.out.println("Base64 String: " + encodedString);
            return ResponseEntity.ok(base64Fingerprint);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred while capturing the fingerprint", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/ftp")
    public ResponseEntity<FileSystemResource> ftpFingerprint(@RequestParam("nombre") String fingerprint) {

        String filePath = fingerprint+".bmp";

        // Leer el archivo BMP en un array de bytes
        File file = new File(filePath);

        if (file.exists()) {
            FileSystemResource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/bmp");
            try {
                ftpService.uploadFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);


        } else {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/trabajadores")
    public List<CSO_CTT_TRABAJADOR> getAllTrabajadores() {
        return csoCttTrabajadorService.getAllTrabajadores();
    }

    @GetMapping("/image")
    public ResponseEntity<FileSystemResource> imageFingerprint(@RequestParam("nombre") String fingerprint) {

        String filePath = fingerprint+".bmp";

        // Leer el archivo BMP en un array de bytes
        File file = new File(filePath);

        if (file.exists()) {
            FileSystemResource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/bmp");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } else {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users")
    public List<User> fetchAll() {
        return userService.getAllUsers();
    }


    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect() {
        Map<String, String> response = new HashMap<>();




        try {
            boolean success = fingerPrintService.initializeSensor();

            if (success) {
                response.put("message", "Sensor inicializado correctamente.");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("error", "Hubo un problema al inicializar el sensor. Por favor, contacte al soporte técnico.");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            // Captura cualquier excepción y proporciona detalles del error
            response.put("error", "Ocurrió un error inesperado al intentar inicializar el sensor.");
            response.put("details", e.getMessage()); // Mensaje de la excepción
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect() throws Exception {
        boolean success = fingerPrintService.closeSensor();
        Map<String, String> response = new HashMap<>();
        response.put("message", success ? "Sensor apagado" : "Hubo un problema con el servidor , porfavor contactese con el soporte de sistemas verticales ");
        return new ResponseEntity<>(response, success ? HttpStatus.OK: HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendFingerprint(@RequestParam("userid") long userId) {
        Optional<User> userOpt = userService.fetchById(userId);


        if (!userOpt.isPresent()) {
            System.out.println("User not found");
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        System.out.println(user);
        Fingerprint fingerprint = new Fingerprint();
        fingerprint.setUser(user);

        String filePath = "fingerprint.bmp";
        File file = new File(filePath);

        if (file.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                fingerprint.setFingerprintImage(imageBytes);
                fingerPrintService.keepFingerprintWithUser(fingerprint);
                return new ResponseEntity<>("Huella dactilar guardada exitosamente", HttpStatus.OK);
            } catch (IOException e) {
                return new ResponseEntity<>("Error reading fingerprint file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Fingerprint file not found", HttpStatus.NOT_FOUND);
        }
    }


}
