import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatServer extends Server implements Constant {
    private final String ENDE = "*bye*";


    public ChatServer() {
        super(2000); //h�rt auf Port 2000 - man sollte immer Ports �ber 1000 benutzen, um nicht mit Standard-Server Diensten zu kollidieren

        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your Port to listen is : " + 2000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void processNewConnection(String pClientIP, int pClientPort) {
    }

    public void processMessage(String pClientIP, int pClientPort, String pMessage) {
        if (pMessage.equals(ENDE)) {
            this.closeConnection(pClientIP, pClientPort);   // hier wird die Verbindung beendet. Methode aus der Oberklasse.
        } else {
            processSendID(pMessage, pClientIP, pClientPort);
        }

    }

    public void processClosingConnection(String pClientIP, int pClientPort) {
        this.send(pClientIP, pClientPort, pClientIP + " " + pClientPort + " auf Wiedersehen. ");
        this.send(pClientIP, pClientPort, ENDE);
    }

    void processSendID(String pString, String ip, int port) {
        final EncoderUtil enc = new EncoderUtil(pString);
        switch (enc.getID()) {
            case "ACC":
                AccountHandler.handleAccountMessage(enc, ip, port, s -> send(ip, port, s));
                break;
            case "CHT":
                ChatHandler.handleChat(enc, ip, port, (t, m) -> {
                    //Return to sender
                    //Forward to receiver
                    if (t == null) send(ip, port, m);
                    else USER_MANAGER.getUser(t).ifPresent(p -> send(p.getCurrentIp(), p.getPortOnline(), m));
                });
                break;
            case "USR":
                UserHandler.handleChat(enc, ip, port, s -> send(ip, port, s));
                break;
            case "PUB":
                PublicChatHandler.handleChat(enc, ip, port, (aBoolean, s) -> {
                    if (aBoolean) send(ip, port, s);
                    else
                        USER_MANAGER.getOnlineProfiles().forEach(clientProfile -> send(clientProfile.getCurrentIp(), clientProfile.getPortOnline(), s));
                });
                break;
            default:
                System.out.println("*Server* corrupted Message: " + ip + ": " + pString);
                break;
        }

    }

}
