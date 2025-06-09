package model;

import java.time.LocalDateTime;

public class Usuario {
    private Long id;
    private String nome;
    private String cpfCnpj;
    private LocalDateTime dataCriacao;
    private String tipo; // PESSOA_FISICA ou PESSOA_JURIDICA

    // Construtor vazio
    public Usuario() {}

    // Construtor completo
    public Usuario(Long id, String nome, String cpfCnpj, LocalDateTime dataCriacao, String tipo) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.dataCriacao = dataCriacao;
        this.tipo = tipo;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}