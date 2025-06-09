package model;

import java.time.LocalDateTime;

public class Transacao {
    private Long id;
    private Long remetenteId;
    private Long destinatarioId;
    private double valor;
    private LocalDateTime dataHora;

    // Construtor vazio
    public Transacao() {}

    // Construtor completo
    public Transacao(Long id, Long remetenteId, Long destinatarioId, double valor, LocalDateTime dataHora) {
        this.id = id;
        this.remetenteId = remetenteId;
        this.destinatarioId = destinatarioId;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRemetenteId() { return remetenteId; }
    public void setRemetenteId(Long remetenteId) { this.remetenteId = remetenteId; }
    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}