package br.edu.infnet.indiestage.eventos.domain;
import java.time.LocalDateTime;
import jakarta.persistence.*;
@Entity
@Table(name="eventos")
public class Evento {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String nome;
    @Column(nullable=false,length=2000) private String descricao;
    @Column(nullable=false,length=100) private String categoria;
    @Column(nullable=false) private LocalDateTime dataHora;
    // Referência por identificador: não existe associação JPA com o domínio de Espaços.
    @Column(nullable=false) private Long espacoId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private SituacaoEvento situacao;
    protected Evento() {}
    public Evento(String nome,String descricao,String categoria,LocalDateTime dataHora,
            Long espacoId,SituacaoEvento situacao) { atualizar(nome,descricao,categoria,dataHora,espacoId,situacao); }
    public void atualizar(String nome,String descricao,String categoria,LocalDateTime dataHora,
            Long espacoId,SituacaoEvento situacao) {
        this.nome=nome;this.descricao=descricao;this.categoria=categoria;
        this.dataHora=dataHora;this.espacoId=espacoId;this.situacao=situacao;
    }
    public Long getId(){return id;}
    public String getNome(){return nome;}
    public String getDescricao(){return descricao;}
    public String getCategoria(){return categoria;}
    public LocalDateTime getDataHora(){return dataHora;}
    public Long getEspacoId(){return espacoId;}
    public SituacaoEvento getSituacao(){return situacao;}
}