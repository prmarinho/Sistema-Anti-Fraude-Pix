package model;

import java.time.LocalDateTime;

public class ScoreConfianca {
    private Long id;
    private Long usuarioId;
    private int score;
    private LocalDateTime ultimaAtualizacao;

    // Construtor vazio
    public ScoreConfianca() {}

    // Construtor completo
    public ScoreConfianca(Long id, Long usuarioId, int score, LocalDateTime ultimaAtualizacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.score = score;
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { this.ultimaAtualizacao = ultimaAtualizacao; }
}