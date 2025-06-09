package model;

import java.time.LocalDateTime;

public class Denuncia {
    private Long id;
    private Long transacaoId;
    private Long motivoId;
    private String observacao;
    private LocalDateTime dataOcorrencia;

    // Construtor vazio
    public Denuncia() {}

    // Construtor completo
    public Denuncia(Long id, Long transacaoId, Long motivoId, String observacao, LocalDateTime dataOcorrencia) {
        this.id = id;
        this.transacaoId = transacaoId;
        this.motivoId = motivoId;
        this.observacao = observacao;
        this.dataOcorrencia = dataOcorrencia;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransacaoId() { return transacaoId; }
    public void setTransacaoId(Long transacaoId) { this.transacaoId = transacaoId; }
    public Long getMotivoId() { return motivoId; }
    public void setMotivoId(Long motivoId) { this.motivoId = motivoId; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDateTime dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
}