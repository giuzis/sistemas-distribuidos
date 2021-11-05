import java.security.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.time.LocalDateTime;

public interface ClientInterface extends Remote{
    public void notifica(String infos) throws RemoteException;
}
