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

    /* 
    Cadastra o usuario para receber novas enquetes
    Parametro : (String) nome do usuario
                (PublicKey) chave publica do cliente
                (ClientInterface) referencia de objeto remoto do cliente
    Retorno : (String) mensagem de confirmacao
    */
    public String CadastrarUsuario(String userName, PublicKey publicKey, ClientInterface clientInterface) throws RemoteException{
        try{
            clientesCadastrados.put(userName, new ClientData(publicKey, clientInterface));
            System.out.println("Usuario " + userName + " cadastrado com sucesso.\n");
            return "Usuario cadastrado com sucesso.\n";
        } catch (Exception e) {
            return "Não foi possível cadastrar novo usuário.\n";
        }
    } 

    /* 
    Cadastra nova enquete e informa usuarios participantes
    Parametro : (String) nome do usuario
                (String) titulo da enquete
                (String) local do evento
                (LocalDateTime[]) propostas de horario do dono da enquete
                (LocalDateTime) data de encerramento da enquete
    Retorno : (String) mensagem de confirmacao
    */
    public String CadastrarEnquete(String userName, String tituloDaEnquete, String localDoEvento, LocalDateTime[] propostasDeHorario, LocalDateTime dataDeEncerramento) throws RemoteException{
        numeroDeEnquetesCadastradas++;
        Enquete novaEnquete = new Enquete(numeroDeEnquetesCadastradas, userName, tituloDaEnquete, localDoEvento, propostasDeHorario, dataDeEncerramento);
        
        String response = novaEnquete.mensagemCadastro();
        
        for ( String cli : clientesCadastrados.keySet() ) {
            if(!cli.equals(userName)){
                try{
                    ClientData cliente = clientesCadastrados.get(cli);
                    cliente.client.notifica(response);
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

    /* 
    Cadastra voto na enquete e verifica se a enquete foi encerrada
    Parametro : (String) nome do usuario votante
                (Integer) id da enquete
                (Integer[]) id das propostas de horario escolhidas pelo votante
    Retorno : (String) mensagem de confirmacao
    */
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

    /* 
    Consulta informacoes sobre a enquete, fazendo uma verificacao atraves da assiantura digital de uma mensagem
    Parametro : (String) nome do usuario
                (Integer) id da enquete
                (byte[]) mensagem criptografada
    Retorno : (String) informacoes da enquete
    */
    public String ConsultarEnquete(String userName, Integer idDaEnquete, byte[] mensagem) throws RemoteException{
        try{
            Enquete enquete = enquetesCadastradas.get(idDaEnquete);
            Signature clientSig = Signature.getInstance("DSA");
            clientSig.initVerify(clientesCadastrados.get(userName).publicKey);  
            clientSig.update(userName.getBytes());
            if(clientSig.verify(mensagem)){
                return enquete.consultaEnquete(userName);
            }
            else{
                return "\nNao foi possivel verificar a assinatura.";
            }
        } catch (Exception e){
            System.out.println(e);
            return "\nNao foi possivel verificar a assinatura.";
        }
        
    }

    /* 
    Finaliza a enquete e envia notificao para os clientes cadastrados e o dono da enquete
    A enquete pode ser finalizada por timeout ou por finalziacao dos votos
    Parametro : (Integer) id da enquete
    Retorno : -
    */
    public void FinalizaEnquete(Integer idDaEnquete) throws RemoteException{
        Enquete enquete = enquetesCadastradas.get(idDaEnquete);
        
        String response = enquete.finalizaEnquete();

        for( String name:enquete.getParticipantesQueVotaram()){
            clientesCadastrados.get(name).client.notifica(response);
        }
        
        clientesCadastrados.get(enquete.getDonoDaEnquete()).client.notifica(response);

        enquetesTimeOut.removeTimeOut(idDaEnquete);

        System.out.println("Enquete "+idDaEnquete+" finalizada com sucesso.\n");
    }

    public static void main (String[] args) throws RemoteException{
        try {
			Server s = new Server();
			Registry registry = LocateRegistry.createRegistry(10099);
			registry.bind("Enquete",s);                                 // registra o nome e a referencia do servidor no servico de nomes
			System.out.println("Servidor pronto.");

            enquetesTimeOut = new EnquetesTimeOut(s);                   // inicializa thread que fica monitorando as enquetes
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