package com.example.helloWorld.service.Impl;


import com.example.helloWorld.model.CSOCttTrabajadorId;
import com.example.helloWorld.model.CSO_CTT_TRABAJADOR;
import com.example.helloWorld.repository.CsoCttTrabajadorRepository;
import com.example.helloWorld.service.CsoCttTrabajadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CsoCttTrabajadorServiceImpl implements CsoCttTrabajadorService {

    @Autowired
    private CsoCttTrabajadorRepository repository;

    public CSO_CTT_TRABAJADOR save(CSO_CTT_TRABAJADOR trabajador) {
        return repository.save(trabajador);
    }

    public CSO_CTT_TRABAJADOR updateFingerprintPath(CSOCttTrabajadorId id, String fingerprintPath) {
        CSO_CTT_TRABAJADOR trabajador = repository.findById(id).orElseThrow(() -> new RuntimeException("Trabajador not found"));
        trabajador.setRutHuellaTra(fingerprintPath);
        return repository.save(trabajador);
    }

    public List<CSO_CTT_TRABAJADOR> getAllTrabajadores() {
        return repository.findAll();
    }
}
