import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.time.format.*;

public class Enquete {
    Integer idDaEnquete;
    public String nomeDaEnquete;
    String localDoEvento;
    String donoDaEnquete;
    Map <Integer, Integer> idDeHorariosPropostosPorVoto;
    LocalDateTime[] horariosPropostos;
    LocalDateTime dataEncerramento;
    Map <String, Boolean> participantesPorVoto;

    public Enquete(Integer _idDaEnquete, String _donoDaEnquete, String _nomeDaEnquete, String _localDoEvento, LocalDateTime[] _horariosPropostos, LocalDateTime _dataEncerramento){
        this.idDaEnquete = _idDaEnquete;
        this.idDeHorariosPropostosPorVoto = new HashMap<Integer, Integer>();
        this.participantesPorVoto = new HashMap<String, Boolean>();
        this.horariosPropostos = new LocalDateTime[_horariosPropostos.length];
        this.horariosPropostos = _horariosPropostos;
        this.nomeDaEnquete = _nomeDaEnquete;
        this.localDoEvento = _localDoEvento;
        this.dataEncerramento = _dataEncerramento;
        this.donoDaEnquete = _donoDaEnquete;

        for (int i = 0; i < this.horariosPropostos.length; i++){
            this.idDeHorariosPropostosPorVoto.put(i, 0);
        }

        System.out.println("--------- Cadastrando enquete -------------");
        System.out.println("ID: " + this.idDaEnquete);
        System.out.println("Nome da enquete: " + this.nomeDaEnquete);
        System.out.println("Local: " + this.localDoEvento);
        System.out.println("Quantidade de horários propostos: " + String.format("%d", this.horariosPropostos.length));
        System.out.println("Data de encerramento: " + this.dataEncerramento.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

    }

    public void adicionaParticipante(String _nomeParticipante){
        this.participantesPorVoto.put(_nomeParticipante, false);
        System.out.println("Participante " + _nomeParticipante + " adicionado a enquete " + this.nomeDaEnquete + ".\n");
    }

    public String cadastraVotos(String name, Integer[] votos){
        try{
            if(!this.participantesPorVoto.containsKey(name)) return "Voce nao esta autorizado a votar nessa enquete.\n";
            if(this.participantesPorVoto.get(name)) return "Seu voto ja foi registrado.\n";
            for(Integer i : votos){
                if(i < this.idDeHorariosPropostosPorVoto.size()){
                    this.idDeHorariosPropostosPorVoto.put(i, this.idDeHorariosPropostosPorVoto.get(i) + 1);
                }
            }
            this.participantesPorVoto.put(name, true);
            return "Voto cadastrado com sucesso.\n";
        } catch (Exception e){
            System.out.println(e);
            return "Ocorreu um erro ao cadastrar o voto.\n";
        }
    }

    public String horarioMaisVotado(){
        Integer indice_do_maior = -1, maior_valor = -1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for(Integer i:this.idDeHorariosPropostosPorVoto.keySet()){
            Integer valor = this.idDeHorariosPropostosPorVoto.get(i);
            if(valor > maior_valor){
                maior_valor = valor;
                indice_do_maior = i;
            }
        }
        if(maior_valor == 0){
            return "";
        }
        else{
            return this.horariosPropostos[indice_do_maior].format(formatter);
        }
    }

    public boolean todosVotaram(){
        for( String name:this.participantesPorVoto.keySet()){
            if(!this.participantesPorVoto.get(name)){
                return false;
            }
        }
        return true;
    }

}
