package ServerPack;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServerThread extends Thread {

    String username=" ";
    Socket clientConnect=null;  //与目标client的连接
    ArrayList<Socket> clients=null;   //与所有client的连接

    HashMap<String, User> userMap=null;
    HashMap<String, Bet> betMap =null;

    ArrayList<Bet> betList =new ArrayList<>();          //1


    BufferedReader br;
    //ERROR!
    //PrintWriter clientPw = new PrintWriter(clientConnect.getOutputStream(), true);//输出到目标client的流
    PrintWriter clientPw;
    public ServerThread(Socket client,ArrayList<Socket> clients,
                        HashMap<String, User> userMap, HashMap<String, Bet> betMap, ArrayList<Bet> betList) {
        try {
            this.clientConnect = client;
            this.clients = clients;
            this.userMap = userMap;
            this.betMap = betMap;
            this.betList=betList;
            this.br = new BufferedReader(new InputStreamReader(this.clientConnect.getInputStream()));
            this.clientPw = new PrintWriter(clientConnect.getOutputStream(), true); //输出到目标client的流
        }catch (Exception e){
            System.out.println( e);
        }
    }

    public boolean judge(String s){
        int n=s.length();
        if(n<=2) return false;
        if(s.charAt(n-1)!='D' && s.charAt(n-1)!='d'&&s.charAt(n-1)!='X'&& s.charAt(n-1)!='x')
            return false;
        if(s.charAt(n-2) != ' ') return  false;
        for(int i=n-3; i>=0; i--){ //0123456789
            if(s.charAt(i) -48<0 || s.charAt(i)-57>0 )
                return false;
        }
        return true;
    }
    public int getNum(String s){ //得到押注的数字  s默认已经符合要求
        int n=s.length();
        String s2=s.substring(0, n-2);
        return Integer.parseInt(s2);
    }

    public void run() {
        boolean flag=false;
        try {
            String content ;
            content="连接成功，请输入用户名：";
            clientPw.println(content);
            while( !flag ) {
                flag=true;
                content = br.readLine();
                username=content;
                for (char c : content.toCharArray())
                    if (c == (char) 9 || c == ' ') {
                        content = "无效输入，请重新输入用户名：";
                        clientPw.println(content);
                        flag=false;//重新输入
                        break;
                    }
                if( flag){  //输入有效 检查是否重复
                    if(  userMap.containsKey(content)){
                        flag=false;
                        content = "用户名已经存在，请更换一个新名字：";
                        clientPw.println(content);
                    }else{/////////////////////////////

                        userMap.put(username, new User(username,clientConnect) ); //加入哈希表中，送100欢乐豆
                        content = "您有100个筹码，请下注：";
                        clientPw.println(content);
                    }
                }
            }
//下注时间//////////////////////////////////////////////////////
            while( userMap.get(username).getCounter() >0 &&  clientConnect.isConnected()){ //////
                /*
                int riskMoney= betMap.containsKey(username)?betMap.get(username).getRiskMoney():0;
                int valid=userMap.get(username).getCounter()-riskMoney;
                */// 筹码更新太慢
                content = br.readLine();
                if(content.equals("quit")){
                    sendMsgToAll(username+"已经退出");
                    clientConnect.close();
                }

                int riskMoney= betMap.containsKey(username)?betMap.get(username).getRiskMoney():0;
                int valid=userMap.get(username).getCounter()-riskMoney;

                if( !judge(content) ){      //不合规格
                    content = "你说啥？要按套路出牌哦！您有"+valid+"个筹码，请下注：";
                    clientPw.println(content);
                }else {
                    int num=getNum(content);
                    if ( valid< num ) { //筹码不够
                        content = "你行不行啊？你有那么多筹码吗？您有"+valid+"个筹码，请下注：";
                        clientPw.println(content);
                    } else{             //输入的押注信息有效
                        char c=content.charAt(content.length()-1);//本轮下注的方向
                        String cc="";
                        if(c=='x'|| c=='X') {c='X'; cc="小";}
                        else if(c=='d'|| c=='D') {c='D'; cc="大";}


                        if( betMap.containsKey(username)){  //本轮下注了？
                            char c2=betMap.get(username).getC();
                            if(c2 == c ){
                                betMap.put(username,new Bet( riskMoney+num,c) );
                            }else { //押注大变押注小
                                if(num < betMap.get(username).getRiskMoney() )
                                    betMap.put(username, new Bet( riskMoney-num,c2) );
                                else if(num > betMap.get(username).getRiskMoney() )
                                    betMap.put(username, new Bet( num-riskMoney,c) );
                                else
                                    betMap.remove(username);
                            }
                        }else{ //这一轮还没下注，写入本次下注情况
                            betMap.put(username, new Bet( num,c));
                        }

                        ///////////////////////新加的
                        betList.add( new Bet(username,num, c));


                        content=username+"下注"+num+"个，押"+cc;
                        sendMsgToAll(content);

                    }
                }
            }

            if( clientConnect.isClosed() ){
                clients.remove(clientConnect);
                userMap.remove(username);
                if(betMap.containsKey(username) );
                    betMap.remove(username);
            }
        }catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendMsgToAll(String msg){
        try{
            for(Socket ss: clients ) {
                PrintWriter pw=new PrintWriter(ss.getOutputStream(),true);
                pw.println(msg);
                pw.flush();
            }
        }catch(Exception e) {
            System.out.println(e);
        }
    }
}