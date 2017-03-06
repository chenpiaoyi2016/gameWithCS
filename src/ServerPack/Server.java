package ServerPack;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    ServerSocket server = null; // 创建服务器端套接字
    ArrayList<Socket> clients ;
    private final int port = 12345;// 定义服务器端口号

    HashMap<String, User> userMap=new HashMap<>();
    HashMap<String, Bet> betMap =new HashMap<>();

    ArrayList<Bet> betList =new ArrayList<>();          //1

    public void sendMsgToAll(String msg){   //群发的
        try{
            for( Socket ss: clients) {
                PrintWriter pw=new PrintWriter(ss.getOutputStream(),true);
                pw.println(msg);
            }
        }catch(Exception e) {
            System.out.println(e);
        }
    }
    public static void main(String args[]) {
        new Server();
    }

    public Server() {
        try {
            clients=new ArrayList<Socket>();
            server=new ServerSocket(port);
            System.out.println("服务器成功启动");
        }
        catch (Exception e){
            System.out.println("服务器启动失败，原因：\n"+e);
        }



        RandomThread rt=new RandomThread(clients,userMap,betMap, betList);
        System.out.println("本局key为： "+rt.key);
        sendMsgToAll("开始啦！大家快下注啦！赌大小啊！翻倍赢啊！");
        Timer timer1 = new Timer();
        timer1.schedule(rt, 30000, 30000);

        try {
            while (true) {
                Socket socket = server.accept(); //保持与一个client的链接
                System.out.println("新用户链接+1");
                clients.add(socket);
                new ServerThread(socket,clients,userMap,betMap, betList).start();
            }
        }catch (Exception e){
            System.out.println( e);
        }
    }
}

class Bet{
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    private String name;                                    //2

    public int getRiskMoney() {
        return riskMoney;
    }
    public void setRiskMoney(int riskMoney) {
        this.riskMoney = riskMoney;
    }
    private int riskMoney;

    public char getC() {
        return c;
    }
    public void setC(char c) {
        this.c = c;
    }
    private char c;        //D 大  X小


    public  Bet( int riskMoney, char c){
        this.riskMoney=riskMoney;
        this.c=c;
    }

    public  Bet( String name, int riskMoney, char c){
        this.name=name;
        this.riskMoney=riskMoney;
        this.c=c;
    }
}

class User{
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    private String name;

    public int getCounter() {
        return counter;
    }
    public void setCounter(int counter) {
        this.counter = counter;
    }
    private int counter;

    public Socket getSock() {
        return sock;
    }
    public void setSock(Socket sock) {
        this.sock = sock;
    }
    private Socket sock;

    public  User(String name,   Socket socket){
        this.counter=100;
        this.name=name;
        this.sock=socket;
    }

}