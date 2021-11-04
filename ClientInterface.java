import java.security.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.time.LocalDateTime;

public interface ClientInterface extends Remote{
    public void notificaNovaEnquete(String nomeDaEnquete, String donoDaEnquete, LocalDateTime[] horariosPropostos, LocalDateTime dataDeEncerramento, String localDoEvento, Integer idDaEnquete) throws RemoteException;
    public void notificaEncerramentoEnquete(String infos) throws RemoteException;
}
