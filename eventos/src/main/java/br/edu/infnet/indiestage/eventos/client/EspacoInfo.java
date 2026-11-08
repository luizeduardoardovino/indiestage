package br.edu.infnet.indiestage.eventos.client;
public record EspacoInfo(Long id, String status, String nome, String cidade,
        Integer capacidade, Boolean disponivel) {
    public static EspacoInfo indisponivel(Long id) {
        return new EspacoInfo(id,"TEMPORARIAMENTE_INDISPONIVEL",null,null,null,null);
    }
    public static EspacoInfo inexistente(Long id) {
        return new EspacoInfo(id,"NAO_ENCONTRADO",null,null,null,null);
    }
}