package com.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadosSimulacion {
    private Double promedioPermanencia = null;
    private Double porcentajeOcupacionServidor = null;
    private Integer cantidadFilas = null;
    private List<FilaVector> filasPaginadas = null;
    private ArrayList<EquipoCRK> datosEquiposRK = null;
    private Double nSuma = null;
    private Double nExpo = null;

    public void calcularPromedioPermanencia(Integer cantidadEquipos, Double tiempoPermanenciaEquipoAcum){
        this.promedioPermanencia = tiempoPermanenciaEquipoAcum / cantidadEquipos;
    }

    public void calcularPorcentajeOcupacion(Double tiempoSimulacion, Double tiempoOcupacionAcum){
        this.porcentajeOcupacionServidor = (tiempoOcupacionAcum / tiempoSimulacion) * 100;
    }
}
