import java.util.Scanner;
import java.security.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.time.*;
import java.time.format.*;

public class Client extends UnicastRemoteObject implements ClientInterface {
    static String name;

    public Client() throws RemoteException{}

    public void notificaNovaEnquete(String nomeDaEnquete, String donoDaEnquete, LocalDateTime[] horariosPropostos, LocalDateTime dataDeEncerramento, String localDoEvento, Integer idDaEnquete) throws RemoteException{
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println(donoDaEnquete + " quer saber qual o melhor horario para o evento " + nomeDaEnquete + " no local " + localDoEvento + ".");
        System.out.println("Use o identificador '" + String.format("%d", idDaEnquete) + "' para votar.");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        System.out.println("\nHorarios propostos:");
        for(int i = 0; i < horariosPropostos.length; i++){
            System.out.println("\t" + String.format("%d", i) + ": " + horariosPropostos[i].format(formatter));
        }   
    }

    public void notificaEncerramentoEnquete(String infos) throws RemoteException{
        System.out.println(infos);
    }

    public static void main(String[] args) throws RemoteException{
        try {
            boolean finaliza = false;
            Scanner scan = new Scanner(System.in);                         // inicializa o scanner

			ClientInterface c = new Client();	
            Registry registry = LocateRegistry.getRegistry(10099);
            ServerInterface s = (ServerInterface) registry.lookup("Enquete");

             //Geração das chaves públicas e privadas
			Signature sig = Signature.getInstance("DSA");                   // instancia um objeto de criptografia DSA
	        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");     // instancia um gerador de chaves
	        SecureRandom secRan = new SecureRandom();                       // obtem um número aleatório que vai servir como seed
	        kpg.initialize(512,secRan);                                     // inicializa o gerador de chaves com o tamanho e a seed
	        KeyPair    keyP = kpg.generateKeyPair();                        // gera o par de chaves
	        PublicKey  pubKey = keyP.getPublic();                           // pega a chave publica
	        PrivateKey priKey = keyP.getPrivate();                          // pega a chave privada
            
            System.out.println("Primeiramente digite seu nome para receber novas enquetes.");
            String name = scan.nextLine();
            System.out.println(s.CadastrarUsuario(name, pubKey, c));        // cadastra o usuário para receber enquetes
            
            int option = 8;

            while(!finaliza){

                System.out.println("0 - Para sair");
				System.out.println("1 - Criar enquete");
				System.out.println("2 - Votar na enquete");
				System.out.println("3 - Consultar enquetes");

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
                        for(int i = 0; i < quantidadeDeHorarios; i++){
                            System.out.print("Digite o horario no formato dd-MM-yyyy HH:mm : ");
                            horariosPropostos[i] = LocalDateTime.parse(scan.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                        }

                        System.out.print("Entre a data de encerramento da enquete no formato dd-MM-yyyy HH:mm : ");
                        LocalDateTime dataDeEncerramento = LocalDateTime.parse(scan.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

                        System.out.println(s.CadastrarEnquete(name, nomeDaEnquete, localDoEvento, horariosPropostos, dataDeEncerramento));
                        break;
                    case 2:
                        System.out.print("Digite o ID da enquete: ");
                        int id = scan.nextInt();
                        scan.nextLine();
                        System.out.print("Informe os IDs do horario que voce podera comparecer separados por espaço: ");
                        String stringHorariosVotados = scan.nextLine();
                        String[] horariosVotados = stringHorariosVotados.split(" ");
                        Integer[] intHorariosVotados = new Integer[horariosVotados.length];
                        for ( int i = 0; i < horariosVotados.length; i++){
                            intHorariosVotados[i] = Integer.parseInt(horariosVotados[i]);
                        }
                        System.out.println(s.CadastrarVoto(name, id, intHorariosVotados));
                        break;
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
