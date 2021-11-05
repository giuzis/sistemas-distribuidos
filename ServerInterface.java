import java.security.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.time.LocalDateTime;

public interface ServerInterface extends Remote {
    public String CadastrarUsuario(String userName, PublicKey publicKey, ClientInterface clientInterface) throws RemoteException;
    public String CadastrarEnquete(String userName, String tituloDaEnquete, String localDoEvento, LocalDateTime[] propostasDeHorario, LocalDateTime dataLimite) throws RemoteException;
    public String CadastrarVoto(String userName, Integer idDaEnquete, Integer[] propostasDeHorario) throws RemoteException;
    public String ConsultarEnquete(String userName, Integer idDaEnquete, byte[] mensagem) throws RemoteException;
}