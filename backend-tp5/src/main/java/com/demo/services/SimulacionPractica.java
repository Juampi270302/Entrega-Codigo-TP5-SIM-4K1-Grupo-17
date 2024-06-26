package com.demo.services;

import com.demo.entities.*;
import com.demo.entities.Estados.EstadoEquipo;
import com.demo.entities.Estados.EstadoServidor;
import com.demo.entities.Estados.Eventos;
import com.demo.entities.Estados.Trabajo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionPractica extends Simulacion {

    private double tiempoSimulacion;
    private ArrayList<Double> probabilidadesTipoTrabajo;
    private ArrayList<Double> tiemposMediaTrabajo;
    private double limite_inferiorUniforme;
    private double limite_superiorUniforme;
    private double limInfUnifTC;
    private double limSupUnifTC;
    private double nSuma;
    private double nExpo;
    private double tiempoInicioResultado;
    private int cantidadItercaciones;

    private ArrayList<Trabajo> tipoTrabajos = new ArrayList<>(Arrays.asList(Trabajo.values()));
    private ArrayList<FilaVector> vectorDeEstados = new ArrayList<>();
    private ArrayList<Equipo> equipos = new ArrayList<>();
    private ArrayList<Equipo> colaComun = new ArrayList<>();
    private ArrayList<Equipo> colaTrabajosC = new ArrayList<>();
    private ArrayList<Equipo> equipos2doPlano = new ArrayList<>();
    private ArrayList<Evento> proximosEventos = new ArrayList<>();
    private FilaVector filaActual = null;
    private FilaVector filaAnterior = null;
    private int contadorIteraciones = 0;
    private int contadorIteracionesResultado = 1;
    private ArrayList<EquipoCRK> datosEquiposRK = new ArrayList<>();
    private RungeKutta calculadoraRK = new RungeKutta();

    private double reloj = 0;
    private Evento proximoEvento = null;
    private int contadorEquipos = 0;


    private void buscarProximoEvento() {

        Evento proximoEvento = null;
        Optional<Evento> proxEvento = proximosEventos.stream()
                .min(Comparator.comparing(evento ->
                        evento.getHoraEvento() - this.reloj));
        if (proxEvento.isPresent()) {
            proximoEvento = proxEvento.get();
        }
        this.proximosEventos.remove(proximoEvento);
        this.proximoEvento = proximoEvento;
    }

    public FilasPaginadas getFilasPaginadas(Integer page) {
        FilasPaginadas filasPaginadas = new FilasPaginadas();
        FilaVector ultimaFila = this.vectorDeEstados.getLast();
        if (this.vectorDeEstados.size() > 200) {
            int fromIndex = page * 200;
            int toIndex = Math.min((page + 1) * 200, this.vectorDeEstados.size());
            filasPaginadas.setFilas(this.vectorDeEstados.subList(fromIndex, toIndex));
        } else {
            filasPaginadas.setFilas(this.vectorDeEstados);
        }
        if (!filasPaginadas.getFilas().contains(ultimaFila)) {
            filasPaginadas.getFilas().add(ultimaFila);
        }
        if (filasPaginadas.getFilas().getFirst() == ultimaFila) {
            filasPaginadas.getFilas().remove(ultimaFila);
            filasPaginadas.getFilas().add(ultimaFila);
        }
        return filasPaginadas;
    }

    public ResultadosSimulacion cola(double tiempo_simulacion,
                                     ArrayList<Double> probabilidadesTipoTrabajo,
                                     ArrayList<Double> tiemposMediaTrabajo,
                                     double limite_inferiorUniforme,
                                     double limite_superiorUniforme,
                                     double limInfUnifTC,
                                     double limSupUnifTC,
                                     double nSuma,
                                     double nExpo,
                                     double tiempoInicioResultado,
                                     int cantidadItercaciones) {

        this.tiempoSimulacion = tiempo_simulacion;
        this.probabilidadesTipoTrabajo = probabilidadesTipoTrabajo;
        this.tiemposMediaTrabajo = tiemposMediaTrabajo;
        this.limite_inferiorUniforme = limite_inferiorUniforme / 60;
        this.limite_superiorUniforme = limite_superiorUniforme / 60;
        this.limInfUnifTC = limInfUnifTC;
        this.limSupUnifTC = limSupUnifTC;
        this.nSuma = nSuma;
        this.nExpo = nExpo;
        this.tiempoInicioResultado = tiempoInicioResultado;
        this.cantidadItercaciones = cantidadItercaciones;

        this.vectorDeEstados.clear();
        this.equipos.clear();
        this.colaComun.clear();
        this.colaTrabajosC.clear();
        this.equipos2doPlano.clear();
        this.proximosEventos.clear();
        this.datosEquiposRK.clear();

        this.filaActual = null;
        this.filaAnterior = null;
        this.contadorIteraciones = 0;
        this.contadorIteracionesResultado = 0;

        this.reloj = 0;
        this.proximoEvento = null;
        this.contadorEquipos = 0;
        this.vectorDeEstados.clear();

        double reloj = this.reloj;
        Llegada llegada_primera = new Llegada();
        llegada_primera.generarProximaLlegada(reloj);

        this.proximosEventos.add(new Evento(
                Eventos.Llegada,
                llegada_primera.getHoraProximaLlegada(),
                null
        ));

        ColaVector colaVectorInicio = new ColaVector(
                this.colaComun.size(),
                this.colaTrabajosC.size(),
                0,
                9);

        int contadorEquipos = this.contadorEquipos;

        FinTrabajo finTrabajo = new FinTrabajo();

        Servidor servidorInicio = new Servidor(EstadoServidor.Libre,
                0,
                0);


        this.filaActual = new FilaVector(
                Eventos.Inicio.toString(),
                this.reloj,
                llegada_primera,
                colaVectorInicio,
                contadorEquipos,
                0,
                0,
                null,
                finTrabajo,
                servidorInicio,
                clonarEquipos());

        if (this.reloj >= this.tiempoInicioResultado && this.contadorIteracionesResultado <= this.cantidadItercaciones) {
            this.vectorDeEstados.add(this.filaActual);
            this.contadorIteracionesResultado++;
        }

        this.contadorIteraciones++;

        while (this.reloj < this.tiempoSimulacion && this.contadorIteraciones <= 100000) {
            this.buscarProximoEvento();
            this.filaAnterior = this.filaActual;
            this.reloj = this.proximoEvento.getHoraEvento();

            if (this.proximoEvento.getTipoEvento().equals(Eventos.Llegada)) {
                this.eventoLlegada();
            }

            if (this.proximoEvento.getTipoEvento().equals(Eventos.Cambio)) {
                this.eventoCambioTrabajo();
            }

            if (this.proximoEvento.getTipoEvento().equals(Eventos.Reanudacion)) {
                this.eventoReanudacionTrabajo();
            }

            if (this.proximoEvento.getTipoEvento().equals(Eventos.FinTrabajo)) {
                this.eventoFinTrabajo();
            }

            if (this.reloj >= this.tiempoInicioResultado && this.contadorIteracionesResultado <= this.cantidadItercaciones) {
                this.contadorIteracionesResultado++;
                this.vectorDeEstados.add(this.filaActual);
            }
            this.contadorIteraciones++;
        }

        if (this.vectorDeEstados.getLast() != this.filaActual) {
            this.vectorDeEstados.add(this.filaActual);
        }

        ResultadosSimulacion resultados = new ResultadosSimulacion();
        resultados.calcularPorcentajeOcupacion(this.reloj, this.filaActual.servidor.getTiempoOcupacionAcum());
        resultados.calcularPromedioPermanencia(this.contadorEquipos, this.filaActual.servidor.getTiempoPermanenciaEquipoAcum());
        resultados.setCantidadFilas(this.vectorDeEstados.size());
        resultados.setDatosEquiposRK(this.datosEquiposRK);
        resultados.setNExpo(this.nExpo);
        resultados.setNSuma(this.nSuma);

        if (this.vectorDeEstados.size() > 200) {
            resultados.setFilasPaginadas(this.vectorDeEstados.subList(0, 200));
            resultados.getFilasPaginadas().add(this.vectorDeEstados.getLast());
        } else {
            resultados.setFilasPaginadas(this.vectorDeEstados);
        }
        FilaVector ultimaFila = this.vectorDeEstados.getLast();
        if (!resultados.getFilasPaginadas().contains(ultimaFila)) {
            resultados.getFilasPaginadas().add(ultimaFila);
        }
        return resultados;
    }

    private void eventoFinTrabajo() {
        ColaVector colasEstadoActual = this.traerColasFilaAnterior();

        Servidor servidorActual = this.traerServidorFilaAnterior();

        if (this.filaAnterior.getServidor().getEstado().equals(EstadoServidor.Ocupado)) {
            Double tiempoAAcumular = this.reloj - this.filaAnterior.getReloj();
            servidorActual.acumularIteracionAIteracion(tiempoAAcumular);
        }

        Equipo equipoFinalizacion = this.proximoEvento.getEquipo();
        FinTrabajo finTrabajo = new FinTrabajo();
        EquipoCRK equipoCRK = null;

        if (colasEstadoActual.getColaTrabajoC() > 0) {
            Equipo equipoEnColaCAAtender = this.colaTrabajosC.getFirst();
            this.colaTrabajosC.remove(equipoEnColaCAAtender);
            equipoEnColaCAAtender.setEquipo_estado(EstadoEquipo.Atendido);

            finTrabajo.setTiempoAtencion(equipoEnColaCAAtender.getValorN() / 60);
            finTrabajo.setHoraFinTrabajo(this.reloj + equipoEnColaCAAtender.getValorN() / 60);

            this.proximosEventos.add(
                    new Evento(
                            Eventos.FinTrabajo,
                            this.reloj + equipoEnColaCAAtender.getValorN() / 60,
                            equipoEnColaCAAtender
                    )
            );
        } else if (colasEstadoActual.getColaComun() > 0) {
            Equipo equipoEnColaComun = this.colaComun.getFirst();
            this.colaComun.remove(equipoEnColaComun);
            equipoEnColaComun.setEquipo_estado(EstadoEquipo.Atendido);

            finTrabajo.calcularHoraFinTrabajo(
                    equipoEnColaComun.getTipo_trabajo(),
                    this.tiemposMediaTrabajo,
                    this.reloj,
                    this.limite_inferiorUniforme,
                    this.limite_superiorUniforme
            );
            equipoEnColaComun.setHoraFinAtencionEstimada(finTrabajo.getHoraFinTrabajo());

            this.proximosEventos.add(
                    new Evento(
                            Eventos.FinTrabajo,
                            finTrabajo.getHoraFinTrabajo(),
                            equipoEnColaComun
                    )
            );

            if (equipoEnColaComun.getTipo_trabajo().equals(Trabajo.C)) {
                equipoCRK =
                        this.calculadoraRK.calculo_rungeKutta(
                                this.nSuma,
                                this.nExpo,
                                this.limInfUnifTC,
                                this.limSupUnifTC,
                                equipoEnColaComun.getId_equipo());
                double horaCambioTrabajoC = this.reloj + equipoCRK.getValorNEnHoras();
                this.proximosEventos.add(
                        new Evento(
                                Eventos.Cambio,
                                horaCambioTrabajoC,
                                equipoEnColaComun
                        )
                );
                equipoEnColaComun.setValorN(equipoCRK.getValorN());
                equipoEnColaComun.setHoraCambioTrabajoC(horaCambioTrabajoC);
                this.datosEquiposRK.add(equipoCRK);
            }

        } else {
            servidorActual.setEstado(EstadoServidor.Libre);
        }

        equipoFinalizacion.setHora_salida(this.reloj);
        equipoFinalizacion.setEquipo_estado(EstadoEquipo.Finalizado);

        double tiempoPermanencia = equipoFinalizacion.getHora_salida() - equipoFinalizacion.getHora_llegada();
        servidorActual.acumTiempoPermanenciaEquipoAcum(tiempoPermanencia);

        Llegada llegada = new Llegada();
        llegada.setHoraProximaLlegada(this.filaAnterior.llegada.getHoraProximaLlegada());

        this.actualizarColas(colasEstadoActual);

        double promedioPermanencia = servidorActual.getTiempoPermanenciaEquipoAcum() / this.contadorEquipos;
        double porcentajeOcupacion = servidorActual.getTiempoOcupacionAcum() / this.reloj * 100;

        this.filaActual = new FilaVector(
                Eventos.FinTrabajo + " E" + equipoFinalizacion.getId_equipo(),
                this.reloj,
                llegada,
                colasEstadoActual,
                this.contadorEquipos,
                promedioPermanencia,
                porcentajeOcupacion,
                equipoCRK,
                finTrabajo,
                servidorActual,
                clonarEquipos()
        );
    }

    private void eventoReanudacionTrabajo() {
        ColaVector colasEstadoActual = this.traerColasFilaAnterior();

        Servidor servidorActual = this.traerServidorFilaAnterior();

        if (this.filaAnterior.getServidor().getEstado().equals(EstadoServidor.Ocupado)) {
            Double tiempoAAcumular = this.reloj - this.filaAnterior.getReloj();
            servidorActual.acumularIteracionAIteracion(tiempoAAcumular);
        }

        FinTrabajo finTrabajo = new FinTrabajo();

        Equipo equipoReanudacion = this.proximoEvento.getEquipo();
        equipoReanudacion.setHoraReanudacionTrabajoC(null);
        this.equipos2doPlano.remove(equipoReanudacion);

//        if (equipoReanudacion.getValorN() == 0){
//            equipoReanudacion.setEquipo_estado(EstadoEquipo.Atendido);
//            servidorActual.setEstado(EstadoServidor.Ocupado);
//            finTrabajo.setHoraFinTrabajo(equipoReanudacion.getHoraFinAtencionEstimada());
//        } else
        if (servidorActual.getEstado().equals(EstadoServidor.Ocupado)) {
            equipoReanudacion.setEquipo_estado(EstadoEquipo.EncolaC);
            equipoReanudacion.setHoraFinAtencionEstimada(null);
            this.colaTrabajosC.add(equipoReanudacion);
            this.anularFinTrabajoC(equipoReanudacion.getId_equipo());
            finTrabajo.setHoraFinTrabajo(this.filaAnterior.finTrabajo.getHoraFinTrabajo());
        } else {
            equipoReanudacion.setEquipo_estado(EstadoEquipo.Atendido);
            servidorActual.setEstado(EstadoServidor.Ocupado);
            finTrabajo.setHoraFinTrabajo(equipoReanudacion.getHoraFinAtencionEstimada());
        }

        Llegada llegada = new Llegada();
        llegada.setHoraProximaLlegada(this.filaAnterior.llegada.getHoraProximaLlegada());

        this.actualizarColas(colasEstadoActual);

        double porcentajeOcupacion = servidorActual.getTiempoOcupacionAcum() / this.reloj * 100;

        this.filaActual = new FilaVector(
                Eventos.Reanudacion + " E" + equipoReanudacion.getId_equipo(),
                this.reloj,
                llegada,
                colasEstadoActual,
                this.contadorEquipos,
                this.filaAnterior.getPromedioPermanencia(),
                porcentajeOcupacion,
                null,
                finTrabajo,
                servidorActual,
                clonarEquipos()
        );

    }

    private void anularFinTrabajoC(Integer idEquipo) {
        for (Evento evento : this.proximosEventos) {
            if (evento.getTipoEvento().equals(Eventos.FinTrabajo) && evento.getEquipo().getId_equipo() == idEquipo) {
                this.proximosEventos.remove(evento);
                break;
            }
        }
    }


    private void eventoCambioTrabajo() {
        ColaVector colasEstadoActual = this.traerColasFilaAnterior();

        Servidor servidorActual = this.traerServidorFilaAnterior();

        if (this.filaAnterior.getServidor().getEstado().equals(EstadoServidor.Ocupado)) {
            Double tiempoAAcumular = this.reloj - this.filaAnterior.getReloj();
            servidorActual.acumularIteracionAIteracion(tiempoAAcumular);
        }

        Equipo equipoCambioTrabajo = this.proximoEvento.getEquipo();
        equipoCambioTrabajo.setEquipo_estado(EstadoEquipo.At2doplano);
        this.equipos2doPlano.add(equipoCambioTrabajo);


        double horaReanudacionTrabajoC =
                equipoCambioTrabajo.getHoraFinAtencionEstimada() - equipoCambioTrabajo.getValorN() / 60;
        equipoCambioTrabajo.setHoraCambioTrabajoC(null);
        equipoCambioTrabajo.setHoraReanudacionTrabajoC(horaReanudacionTrabajoC);

        if (equipoCambioTrabajo.getValorN() != 0) {
            this.proximosEventos.add(
                    new Evento(
                            Eventos.Reanudacion,
                            horaReanudacionTrabajoC,
                            equipoCambioTrabajo
                    )
            );
        } else {
            this.proximosEventos.addFirst(
                    new Evento(
                            Eventos.Reanudacion,
                            horaReanudacionTrabajoC,
                            equipoCambioTrabajo
                    )
            );
        }

        EquipoCRK equipoCRK = null;
        FinTrabajo finTrabajo = new FinTrabajo();

        if (colasEstadoActual.getColaTrabajoC() > 0) {

            Equipo equipoEnColaCAAtender = this.colaTrabajosC.getFirst();
            equipoEnColaCAAtender.setEquipo_estado(EstadoEquipo.Atendido);
            this.colaTrabajosC.remove(equipoEnColaCAAtender);

            finTrabajo.setTiempoAtencion(equipoEnColaCAAtender.getValorN() / 60);
            finTrabajo.setHoraFinTrabajo(this.reloj + equipoEnColaCAAtender.getValorN() / 60);

            this.proximosEventos.add(
                    new Evento(
                            Eventos.FinTrabajo,
                            this.reloj + equipoEnColaCAAtender.getValorN() / 60,
                            equipoEnColaCAAtender
                    )
            );

        } else if (colasEstadoActual.getColaComun() > 0) {

            Equipo equipoEnColaComunAAtender = this.colaComun.getFirst();
            equipoEnColaComunAAtender.setEquipo_estado(EstadoEquipo.Atendido);
            this.colaComun.remove(equipoEnColaComunAAtender);

            finTrabajo.calcularHoraFinTrabajo(
                    equipoEnColaComunAAtender.getTipo_trabajo(),
                    this.tiemposMediaTrabajo,
                    this.reloj,
                    this.limite_inferiorUniforme,
                    this.limite_superiorUniforme
            );
            this.proximosEventos.add(
                    new Evento(
                            Eventos.FinTrabajo,
                            finTrabajo.getHoraFinTrabajo(),
                            equipoEnColaComunAAtender
                    )
            );

            equipoEnColaComunAAtender.setHoraFinAtencionEstimada(finTrabajo.getHoraFinTrabajo());

            if (equipoEnColaComunAAtender.getTipo_trabajo().equals(Trabajo.C)) {
                equipoCRK =
                        this.calculadoraRK.calculo_rungeKutta(
                                this.nSuma,
                                this.nExpo,
                                this.limInfUnifTC,
                                this.limSupUnifTC,
                                equipoEnColaComunAAtender.getId_equipo());
                double horaCambioTrabajoC = this.reloj + equipoCRK.getValorNEnHoras();
                this.proximosEventos.add(
                        new Evento(
                                Eventos.Cambio,
                                horaCambioTrabajoC,
                                equipoEnColaComunAAtender
                        )
                );
                equipoEnColaComunAAtender.setValorN(equipoCRK.getValorN());
                equipoEnColaComunAAtender.setHoraCambioTrabajoC(horaCambioTrabajoC);
                this.datosEquiposRK.add(equipoCRK);
            }
        } else {
            servidorActual.setEstado(EstadoServidor.Libre);
            finTrabajo.setHoraFinTrabajo(this.filaAnterior.finTrabajo.getHoraFinTrabajo());
        }

        Llegada llegada = new Llegada();
        llegada.setHoraProximaLlegada(this.filaAnterior.llegada.getHoraProximaLlegada());

        this.actualizarColas(colasEstadoActual);

        double porcentajeOcupacion = servidorActual.getTiempoOcupacionAcum() / this.reloj * 100;

        this.filaActual = new FilaVector(
                Eventos.Cambio + " E" + equipoCambioTrabajo.getId_equipo(),
                this.reloj,
                llegada,
                colasEstadoActual,
                this.contadorEquipos,
                this.filaAnterior.getPromedioPermanencia(),
                porcentajeOcupacion,
                equipoCRK,
                finTrabajo,
                servidorActual,
                clonarEquipos()
        );
    }

    private ColaVector traerColasFilaAnterior() {
        return new ColaVector(
                this.filaAnterior.getColaVector().getColaComun(),
                this.filaAnterior.getColaVector().getColaTrabajoC(),
                this.filaAnterior.getColaVector().getTrabajoCSegundoPlano(),
                this.filaAnterior.getColaVector().getLugaresLibres());
    }

    private Servidor traerServidorFilaAnterior() {
        return new Servidor(
                this.filaAnterior.getServidor().getEstado(),
                this.filaAnterior.getServidor().getTiempoOcupacionAcum(),
                this.filaAnterior.getServidor().getTiempoPermanenciaEquipoAcum());
    }

    private void eventoLlegada() {
        ColaVector colasEstadoActual = this.traerColasFilaAnterior();

        Servidor servidorActual = this.traerServidorFilaAnterior();

        if (this.filaAnterior.getServidor().getEstado().equals(EstadoServidor.Ocupado)) {
            Double tiempoAAcumular = this.reloj - this.filaAnterior.getReloj();
            servidorActual.acumularIteracionAIteracion(tiempoAAcumular);
        }

        Llegada proximaLLegada = new Llegada();
        proximaLLegada.generarProximaLlegada(this.reloj);

        this.proximosEventos.add(
                new Evento(
                        Eventos.Llegada,
                        proximaLLegada.getHoraProximaLlegada(),
                        null)
        );

        EquipoCRK equipoCRK = null;
        FinTrabajo finTrabajo = new FinTrabajo();
        Equipo equipo = new Equipo();

        if (servidorActual.getEstado().equals(EstadoServidor.Ocupado)) {

            finTrabajo.setHoraFinTrabajo(this.filaAnterior.finTrabajo.getHoraFinTrabajo());

            if (colasEstadoActual.getLugaresLibres() > 0) {

                proximaLLegada.calcularTipoTrabajo(tipoTrabajos, probabilidadesTipoTrabajo);
                this.contadorEquipos++;

                equipo.setId_equipo(this.contadorEquipos);
                equipo.setHora_llegada(reloj);
                equipo.setTipo_trabajo(proximaLLegada.getTrabajo());
                equipos.add(equipo);

                this.colaComun.add(equipo);
                equipo.setEquipo_estado(EstadoEquipo.EnCola);
            }

        } else {
            this.contadorEquipos++;
            servidorActual.setEstado(EstadoServidor.Ocupado);

            proximaLLegada.calcularTipoTrabajo(tipoTrabajos, probabilidadesTipoTrabajo);

            finTrabajo.calcularHoraFinTrabajo(
                    proximaLLegada.getTrabajo(),
                    this.tiemposMediaTrabajo,
                    this.reloj,
                    this.limite_inferiorUniforme,
                    this.limite_superiorUniforme);

            equipo.setId_equipo(this.contadorEquipos);
            equipo.setEquipo_estado(EstadoEquipo.Atendido);
            equipo.setTipo_trabajo(proximaLLegada.getTrabajo());
            equipo.setHora_llegada(reloj);
            equipo.setHoraFinAtencionEstimada(finTrabajo.getHoraFinTrabajo());

            proximosEventos.add(
                    new Evento(
                            Eventos.FinTrabajo,
                            finTrabajo.getHoraFinTrabajo(),
                            equipo)
            );

            if (proximaLLegada.getTrabajo().equals(Trabajo.C)) {
                equipoCRK =
                        this.calculadoraRK.calculo_rungeKutta(
                                this.nSuma,
                                this.nExpo,
                                this.limInfUnifTC,
                                this.limSupUnifTC,
                                equipo.getId_equipo());
                double horaCambioTrabajoC = this.reloj + equipoCRK.getValorNEnHoras();
                proximosEventos.add(
                        new Evento(
                                Eventos.Cambio,
                                horaCambioTrabajoC,
                                equipo)
                );
                equipo.setHoraCambioTrabajoC(horaCambioTrabajoC);
                equipo.setValorN(equipoCRK.getValorN());
                this.datosEquiposRK.add(equipoCRK);
            }
            equipos.add(equipo);
        }

        this.actualizarColas(colasEstadoActual);

        double porcentajeOcupacion = servidorActual.getTiempoOcupacionAcum() / this.reloj * 100;

        this.filaActual = new FilaVector(
                Eventos.Llegada + " E" + equipo.getId_equipo(),
                this.reloj,
                proximaLLegada,
                colasEstadoActual,
                this.contadorEquipos,
                this.filaAnterior.getPromedioPermanencia(),
                porcentajeOcupacion,
                equipoCRK,
                finTrabajo,
                servidorActual,
                clonarEquipos());
    }


    private ArrayList<Equipo> clonarEquipos() {
        ArrayList<Equipo> equipos = new ArrayList<>();
        for (Equipo equipo : this.equipos) {
            if (!equipo.isYaTermino()) {
                Equipo equipoClon = new Equipo();
                equipoClon.setId_equipo(equipo.getId_equipo());
                equipoClon.setEquipo_estado(equipo.getEquipo_estado());
                equipoClon.setTipo_trabajo(equipo.getTipo_trabajo());
                equipoClon.setHora_llegada(equipo.getHora_llegada());
                equipoClon.setValorN(equipo.getValorN());
                equipoClon.setHoraCambioTrabajoC(equipo.getHoraCambioTrabajoC());
                equipoClon.setHoraReanudacionTrabajoC(equipo.getHoraReanudacionTrabajoC());
                equipoClon.setHoraFinAtencionEstimada(equipo.getHoraFinAtencionEstimada());
                equipoClon.setHora_salida(equipo.getHora_salida());
                equipos.add(equipoClon);
            }
            if (equipo.getEquipo_estado() == EstadoEquipo.Finalizado) {
                equipo.setYaTermino(true);
            }
        }
        return equipos;
    }

    private void actualizarColas(ColaVector colasEstadoActual) {
        colasEstadoActual.calcularLugaresLibres(
                this.colaComun.size(),
                this.equipos2doPlano.size(),
                this.colaTrabajosC.size()
        );
    }
}

