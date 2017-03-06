package test;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class client {

    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    String strStatus;


    public client() {

        try {
            socket = new Socket("127.0.0.1", 12345);
            in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);
        } catch (UnknownHostException e) {
            System.out.println("连接服务器失败! 未知主机");
            System.out.println(e);
        } catch (Exception e) {
            System.out.println("其他错误");
            System.out.println(e);
        }

        new Cthread(socket).start();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String strReceive = br.readLine();
            while (! strReceive.equals("bye")) {
                //对收到的信息进行解码打印
                System.out.println(strReceive);
                strReceive = br.readLine();
            }
            socket.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public static void main(String[] args)throws IOException {
        new client();
    }

}


class Cthread extends Thread
{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Cthread(Socket s) {
        try {
            this.socket = s;
        }catch (Exception E) {
            System.out.println(E);
        }
    }
    public void run() { //读取输入的消息 发送给socket 给客户端
        try {
            in = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
            String msg;
            msg = in.readLine();
            while ( !msg.equals("quit")) {
                out.println(msg);
                msg = in.readLine();
            }
            out.println(msg);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}







