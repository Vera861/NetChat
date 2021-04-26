import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private static MyServer server;

    public static MyServer getServer() {
        return server;
    }

    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public MyServer() {
        server = this;
        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Ждем подключения клиента");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMsg(msg);
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendMsgToClient(ClientHandler fromClient, String nickTo, String msg) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nickTo)) {
                client.sendMsg("от пользователя " + fromClient.getName() + ": " + msg);
                fromClient.sendMsg("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        fromClient.sendMsg("Участник с ником \"" + nickTo + "\" не найден");
    }

    public AuthService getAuthService() {
        return authService;
    }
}
