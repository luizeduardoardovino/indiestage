package br.edu.infnet.indiestage.espacos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "espacos")
public class Espaco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private String cidade;
    @Column(nullable = false)
    private int capacidade;
    @Column(nullable = false)
    private boolean disponivel;

    protected Espaco() {
        // Construtor utilizado pela JPA.
    }

    public Espaco(String nome, String cidade, int capacidade, boolean disponivel) {
        this.nome = nome;
        this.cidade = cidade;
        this.capacidade = capacidade;
        this.disponivel = disponivel;
    }

    public void atualizar(String nome, String cidade, int capacidade, boolean disponivel) {
        this.nome=nome; this.cidade=cidade; this.capacidade=capacidade; this.disponivel=disponivel;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
    public int getCapacidade() { return capacidade; }
    public boolean isDisponivel() { return disponivel; }
}