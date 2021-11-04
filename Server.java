import java.security.*;
import java.nio.channels.ClosedByInterruptException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

public class Server extends UnicastRemoteObject implements ServerInterface {
    static Map <String,ClientData> clientesCadastrados;
    static Map<Integer,Enquete> enquetesCadastradas;
    static EnquetesTimeOut enquetesTimeOut;
    static Integer numeroDeEnquetesCadastradas;

    public Server() throws RemoteException {
        numeroDeEnquetesCadastradas = 0;
        clientesCadastrados = new HashMap<String,ClientData>();
        enquetesCadastradas = new HashMap<Integer,Enquete>();
    }

    public String CadastrarUsuario(String userName, PublicKey publicKey, ClientInterface clientInterface) throws RemoteException{
        try{
            clientesCadastrados.put(userName, new ClientData(publicKey, clientInterface));
            System.out.println("Usuario " + userName + " cadastrado com sucesso.\n");
            clientesCadastrados.get(userName).print();
            return "Usuario cadastrado com sucesso.\n";
        } catch (Exception e) {
            return "Não foi possível cadastrar novo usuário.\n";
        }
    } 

    public String CadastrarEnquete(String userName, String tituloDaEnquete, String localDoEvento, LocalDateTime[] propostasDeHorario, LocalDateTime dataDeEncerramento) throws RemoteException{
        numeroDeEnquetesCadastradas++;
        Enquete novaEnquete = new Enquete(numeroDeEnquetesCadastradas, userName, tituloDaEnquete, localDoEvento, propostasDeHorario, dataDeEncerramento);
        for ( String cli : clientesCadastrados.keySet() ) {
            if(!cli.equals(userName)){
                try{
                    ClientData cliente = clientesCadastrados.get(cli);
                    cliente.client.notificaNovaEnquete(novaEnquete.nomeDaEnquete, novaEnquete.donoDaEnquete, novaEnquete.horariosPropostos, novaEnquete.dataEncerramento, localDoEvento, numeroDeEnquetesCadastradas);
                    novaEnquete.adicionaParticipante(cli);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

        enquetesCadastradas.put(numeroDeEnquetesCadastradas, novaEnquete);
        enquetesTimeOut.adicionaNovoTimeOut(numeroDeEnquetesCadastradas, dataDeEncerramento);

        return "Enquete " + numeroDeEnquetesCadastradas + " cadastrada com sucesso.\n";
    }

    public String CadastrarVoto(String userName, Integer idDaEnquete, Integer[] propostasDeHorario) throws RemoteException{
        try{
            if(!enquetesCadastradas.containsKey(idDaEnquete)) return "Enquete nao encontrada.\n";
            String response = enquetesCadastradas.get(idDaEnquete).cadastraVotos(userName,propostasDeHorario);
            if(enquetesCadastradas.get(idDaEnquete).todosVotaram()){
                FinalizaEnquete(idDaEnquete);
            }
            return response;
        } catch (Exception e){
            System.out.print(e);
            return "Ocorreu um erro ao registrar seu voto.\n";
        }
    }

    public String ConsultarEnquete(String userName, Integer idDaEnquete) throws RemoteException{
        return "";
    }

    public void FinalizaEnquete(Integer idDaEnquete) throws RemoteException{
        Enquete enquete = enquetesCadastradas.get(idDaEnquete);
        String response = "-------------------------------------------------------------\n" +
        "Enquete " + enquete.nomeDaEnquete + " finalizada. \n";
        String horarioFinal = enquete.horarioMaisVotado();
        if(horarioFinal == ""){
            response += "Nenhum voto contabilizado. \n";
        }
        else{
            response += "O horario escolhido foi "  + horarioFinal + ".";
        }

        for( String name:enquete.participantesPorVoto.keySet()){
            if(enquete.participantesPorVoto.get(name)){
                clientesCadastrados.get(name).client.notificaEncerramentoEnquete(response);
            }
        }
        
        clientesCadastrados.get(enquete.donoDaEnquete).client.notificaEncerramentoEnquete(response);

        enquetesCadastradas.remove(idDaEnquete);
        enquetesTimeOut.removeTimeOut(idDaEnquete);

        System.out.println("Enquete "+idDaEnquete+" finalizada com sucesso\n");
    }

    public static void main (String[] args) throws RemoteException{
        try {
			Server s = new Server();
			Registry registry = LocateRegistry.createRegistry(10099);
			registry.bind("Enquete",s);
			System.out.println("Servidor pronto.");

            enquetesTimeOut = new EnquetesTimeOut(s);
		} catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

class ClientData {
    PublicKey publicKey;
    ClientInterface client;

    public ClientData(PublicKey _publicKey, ClientInterface _client){
        this.client = _client;
        this.publicKey = _publicKey;
    }
    public void print(){
        System.out.println(this.publicKey.toString());
    }
}

class EnquetesTimeOut extends Thread {
    static Map<Integer, LocalDateTime> enquetePorDataDeEncerramento;
    static Server s;

    public EnquetesTimeOut (Server _s){
        enquetePorDataDeEncerramento = new HashMap<Integer, LocalDateTime>();
        s = _s;
        this.start();
    }
    public void run() {
        try{
            System.out.println("Thread rodando.");
            while(true){
                TimeUnit.SECONDS.sleep(1);
                for(Integer idDaEnquete:enquetePorDataDeEncerramento.keySet()){
                    if(enquetePorDataDeEncerramento.get(idDaEnquete).isBefore(LocalDateTime.now())){
                        try{
                            s.FinalizaEnquete(idDaEnquete);
                        } catch (Exception e) {
                            System.err.println("Server exception: " + e.toString());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch(Exception e){System.out.println(e);}
    }
    public void adicionaNovoTimeOut(Integer  idDaEnquete, LocalDateTime dataDeEncerramento){
        enquetePorDataDeEncerramento.put(idDaEnquete, dataDeEncerramento);
        System.out.println("Novo timeout adicionado.");
    }
    public void removeTimeOut(Integer idDaEnquete){
        enquetePorDataDeEncerramento.remove(idDaEnquete);
        System.out.println("Timeout removido.");
    }
}