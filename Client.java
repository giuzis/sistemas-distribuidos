import java.util.Scanner;
import java.security.*;
import java.io.Console;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.time.*;
import java.time.format.*;

public class Client extends UnicastRemoteObject implements ClientInterface {
    static String name;

    public Client() throws RemoteException{}


    /* 
    Metodo utilizado pelo servidor para enviar notificacoes assincronas para o cliente.
    Parametros : (String) informacoes da notificacao
    Retorno : -
    */
    public void notifica(String infos) throws RemoteException{
        System.out.println(infos);
        printMenu();
    }

    static private void printMenu(){
        System.out.println("0 - Para sair");
        System.out.println("1 - Criar enquete");
        System.out.println("2 - Votar na enquete");
        System.out.println("3 - Consultar enquetes");
    }

    public static void main(String[] args) throws RemoteException{
        try {
            boolean finaliza = false;
            Scanner scan = new Scanner(System.in);                                  // inicializa o scanner

			ClientInterface c = new Client();	                                    // instancia novo cliente
            Registry registry = LocateRegistry.getRegistry(10099);                  
            ServerInterface s = (ServerInterface) registry.lookup("Enquete");       // procura referência do objeto remoto "Enquete"

             //Geração das chaves públicas e privadas
			Signature sig = Signature.getInstance("DSA");                           // instancia um objeto de criptografia DSA
	        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");             // instancia um gerador de chaves
	        SecureRandom secRan = new SecureRandom();                               // obtem um número aleatório que vai servir como seed
	        kpg.initialize(512,secRan);                                             // inicializa o gerador de chaves com o tamanho e a seed
	        KeyPair    keyP = kpg.generateKeyPair();                                // gera o par de chaves
	        PublicKey  pubKey = keyP.getPublic();                                   // pega a chave publica
	        PrivateKey priKey = keyP.getPrivate();                                  // pega a chave privada
            sig.initSign(priKey);                                                   // coloca a chave privada no inicializador
            
            System.out.println("Primeiramente digite seu nome para receber novas enquetes.");
            String name = scan.nextLine();
            System.out.println(s.CadastrarUsuario(name, pubKey, c));                // cadastra o usuario atraves do nome para receber enquetes
            
            int option = 8;

            while(!finaliza){

                printMenu();

				option = scan.nextInt();
                scan.nextLine();

                switch (option){
                    case 0: 
                        finaliza = true;
                        break;
                	case 1:
                        System.out.print("Entre com o nome da enquete: ");
                        String nomeDaEnquete = scan.nextLine();

                        System.out.print("Entre com o local do evento: ");
                        String localDoEvento = scan.nextLine();

                        System.out.print("Quantos horarios diferentes serao propostos: ");
                        int quantidadeDeHorarios = scan.nextInt();
                        scan.nextLine();

                        LocalDateTime[] horariosPropostos;
                        horariosPropostos = new LocalDateTime[quantidadeDeHorarios];
                        int j = 0;
                        while(j < quantidadeDeHorarios){
                            System.out.print("Digite o horario no formato dd-MM-yyyy HH:mm : ");
                            try{
                                horariosPropostos[j] = LocalDateTime.parse(scan.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                                j++;
                            } catch(Exception e){
                                System.out.println("Formato errado, tente novamente. ");
                            }
                        }

                        boolean saidoloop = false;
                        LocalDateTime dataDeEncerramento = LocalDateTime.now();
                        while(!saidoloop){
                            System.out.print("Entre a data de encerramento da enquete no formato dd-MM-yyyy HH:mm : ");
                            try{
                                dataDeEncerramento = LocalDateTime.parse(scan.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                                saidoloop = true;
                            } catch(Exception e){
                                System.out.println("Formato errado, tente novamente. ");
                            }
                        }
                        System.out.println(s.CadastrarEnquete(name, nomeDaEnquete, localDoEvento, horariosPropostos, dataDeEncerramento));  // invoca metodo do servidor
                        break;
                    case 2:
                        System.out.print("Digite o ID da enquete: ");
                        int id = scan.nextInt();
                        scan.nextLine();
                        System.out.print("Informe os IDs do horario que voce podera comparecer separados por espaco: ");
                        String stringHorariosVotados = scan.nextLine();
                        String[] horariosVotados = stringHorariosVotados.split(" ");
                        Integer[] intHorariosVotados = new Integer[horariosVotados.length];
                        for ( int i = 0; i < horariosVotados.length; i++){
                            intHorariosVotados[i] = Integer.parseInt(horariosVotados[i]);
                        }
                        System.out.println(s.CadastrarVoto(name, id, intHorariosVotados));
                        break;
                    case 3:
                        System.out.print("Digite o ID da enquete: ");
                        int id2 = scan.nextInt();
                        sig.update(name.getBytes()); 
                        byte[] assinatura = sig.sign();
                        System.out.println(s.ConsultarEnquete(name, id2, assinatura));
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
