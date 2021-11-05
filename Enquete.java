import java.time.*;
import java.util.*;
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
    String statusDaEnquete;

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
        this.statusDaEnquete = "Em andamento.";

        for (int i = 0; i < this.horariosPropostos.length; i++){
            this.idDeHorariosPropostosPorVoto.put(i, 0);
        }

        System.out.println("--------- Cadastrando enquete -------------");
        System.out.println("ID: " + this.idDaEnquete);
        System.out.println("Nome da enquete: " + this.nomeDaEnquete);
        System.out.println("Local: " + this.localDoEvento);
        System.out.println("Quantidade de horarios propostos: " + String.format("%d", this.horariosPropostos.length));
        System.out.println("Data de encerramento: " + this.dataEncerramento.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

    }

    public void adicionaParticipante(String _nomeParticipante){
        this.participantesPorVoto.put(_nomeParticipante, false);
        System.out.println("Participante " + _nomeParticipante + " adicionado a enquete " + this.nomeDaEnquete + ".\n");
    }

    public String cadastraVotos(String name, Integer[] votos){
        try{
            if(!this.participantesPorVoto.containsKey(name)) return "Voce nao esta autorizado a votar nessa enquete.\n";
            if(this.statusDaEnquete.equals("Encerrada.")) return "Essa enquete ja foi encerrada.\n";
            if(this.participantesPorVoto.get(name)) return "Seu voto ja foi registrado.\n";
            if(Arrays.stream(votos).anyMatch(i -> i == this.idDeHorariosPropostosPorVoto.size())) return "Horario nao disponivel\n";
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

    public List<String> getParticipantesQueVotaram(){
        List<String> response = new ArrayList<String>();        ;
        for(String name : this.participantesPorVoto.keySet()){
            if(this.participantesPorVoto.get(name)) response.add(name);
        }
        return response;
    }

    public String getDonoDaEnquete(){
        return this.donoDaEnquete;
    }

    public String consultaEnquete (String userName){
        if(!(this.participantesPorVoto.containsKey(userName) || this.donoDaEnquete.equals(userName))) return "Voce nao tem permissao para acessar essa enquete.";
        String mensagem = "\n--------Informacoes da enquete---------" +
        "\nID: " + this.idDaEnquete +
        "\nNome da enquete: " + this.nomeDaEnquete +
        "\nLocal: " + this.localDoEvento +
        "\nStatus: " + this.statusDaEnquete +
        "\nHorarios propostos por voto: \n";
        for (int i = 0; i < horariosPropostos.length ; i++){
            mensagem += "\n\t" + horariosPropostos[i].format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + " tem " + idDeHorariosPropostosPorVoto.get(i) + " votos.";
        }
        mensagem += "\nJa votaram: ";
        for ( String name : this.participantesPorVoto.keySet()){
            if(this.participantesPorVoto.get(name)) mensagem += "\n\t" + name;
        }
        mensagem += "\nData de encerramento: " + this.dataEncerramento.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        mensagem += "\n---------------------------------------\n";
        return mensagem;
    }

    public String finalizaEnquete(){
        String response = "\n---------------------------------------\n" +
        "Enquete " + this.nomeDaEnquete + " finalizada. \n";
        String horarioFinal = this.horarioMaisVotado();
        if(horarioFinal == ""){
            response += "Nenhum voto contabilizado. \n";
        }
        else{
            response += "O horario escolhido foi "  + horarioFinal + ".";
        }
        response += "\n---------------------------------------\n";
        this.statusDaEnquete = "Encerrada.";
        return response;
    }

    public String mensagemCadastro(){
        String response = "\n---------------------------------------\n" +
        this.donoDaEnquete + " quer saber qual o melhor horario para o evento " + this.nomeDaEnquete + " no local " + this.localDoEvento + ".\n" +
        "\nUse o identificador '" + String.format("%d", this.idDaEnquete) + "' para votar.";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        response += "\nHorarios propostos:";
        for(int i = 0; i < this.horariosPropostos.length; i++){
            response += "\n\t" + String.format("%d", i) + ": " + this.horariosPropostos[i].format(formatter);
        }  
        response += "\nVoce tem ate " + this.dataEncerramento.format(formatter) + " para votar." +
        "\n---------------------------------------\n";
        return response;
    }

}
