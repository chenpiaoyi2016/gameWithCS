package ServerPack;

import java.io.*;
import java.net.Socket;
import java.util.*;


public  class RandomThread extends TimerTask {
    int totalCounters=5000;//庄家的筹码
    int profit=0;
    public int key=(int)(Math.random()*6)+1;

    String msg;

    ArrayList<Socket> clients;   //与所有client的连接

    HashMap<String, User> userMap=new HashMap<>();
    HashMap<String, Bet> betMap =new HashMap<>();

    ArrayList<Bet> betList =new ArrayList<>();          //1



    public void sendMsgToAll(String msg){   //群发的
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

    public RandomThread( ArrayList<Socket> clients,HashMap<String, User> userMap,
                         HashMap<String, Bet> betMap , ArrayList<Bet> betList  )   {
        //super();
        this.clients = clients;
        this.userMap=userMap;
        this.betMap=betMap;
        this.betList=betList;
    }

    public void run() {
        sendMsgToAll("停止下注啦！都不要动啦！马上要开啦！开！开！开！");
        sendMsgToAll(key+"点！");

        try {
            // 新加的
            for( Bet bt: betList){
                String name=bt.getName();
                if (bt.getC() == 'D' && key > 3 || bt.getC() == 'X' && key < 4) {
                    msg = "你下注" + bt.getRiskMoney() + "个筹码，赢了";
                    PrintWriter pw2 = new PrintWriter(userMap.get(name).getSock().getOutputStream(), true);
                    pw2.println(msg);
                } else {
                    msg = "你下注" + bt.getRiskMoney() + "个筹码，输了";
                    PrintWriter pw2 = new PrintWriter(userMap.get(name).getSock().getOutputStream(), true);
                    pw2.println(msg);
                }
            }


            for (String name : userMap.keySet()) {
                if (betMap.containsKey(name)) { //本轮下注了
                    if (betMap.get(name).getC() == 'D' && key > 3 || betMap.get(name).getC() == 'X' && key < 4) {
                        //win
                        userMap.get(name).setCounter(userMap.get(name).getCounter() + betMap.get(name).getRiskMoney());
                        profit -= betMap.get(name).getRiskMoney();//庄家的盈利
                        msg = "你赢了，返还双倍共" + 2 * betMap.get(name).getRiskMoney() + "个筹码";
                        //send
                        PrintWriter pw2 = new PrintWriter(userMap.get(name).getSock().getOutputStream(), true);
                        pw2.println(msg);
                        pw2.flush();

                    } else {
                        //lose
                        userMap.get(name).setCounter(userMap.get(name).getCounter() - betMap.get(name).getRiskMoney());
                        profit += betMap.get(name).getRiskMoney();
                        msg = "你输了，" + betMap.get(name).getRiskMoney() + "个筹码都归了庄家";
                        PrintWriter pw2 = new PrintWriter(userMap.get(name).getSock().getOutputStream(), true);
                        pw2.println(msg);
                        pw2.flush();

                        if (userMap.get(name).getCounter() <= 0){
                            msg = "你输个精光，别玩儿了！";
                            pw2.println(msg);
                            pw2.flush();
                            sendMsgToAll(name + "输个精光，被一脚踢出！");
                        }
                    }
                }
            }

            if (profit >= 0) {
                totalCounters += profit;
                System.out.println("这一局庄家赢了" + profit + "个筹码、总共剩" + totalCounters + "个筹码");
            } else {
                totalCounters += profit;
                System.out.println("这一局庄家输了" + (0-profit) + "个筹码、总共剩" + totalCounters + "个筹码");
            }

            for(String betman: betMap.keySet()){
                if ( userMap.get(betman).getCounter()<=0){  //剔除
                    userMap.get(betman).getSock().close();
                    userMap.remove(betman);
                }
            }
            betMap.clear(); //清空上一轮的下注情况
            betList.clear();

            profit=0;
            if (totalCounters < 0) { //
                msg = "庄家运气怎么这么差，竟然输光了，掀桌子不玩儿了！大家散场啦！";
                sendMsgToAll(msg);
                msg = "bye";
                sendMsgToAll(msg);
                for (Socket sss : clients)
                    sss.close();

                //问题 开的那些线程如何退出 serversocket怎么关闭 客户端怎么退出
            }

            System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||" );

            key=(int)(Math.random()*6)+1;
            System.out.println("本局key为： "+key);
            sendMsgToAll("\n");

            sendMsgToAll("开始啦！大家快下注啦！赌大小啊！翻倍赢啊！");
        }catch (IOException e){
            System.out.println(e);
        }


    }

}
