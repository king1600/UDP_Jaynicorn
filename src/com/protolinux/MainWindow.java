package com.protolinux;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class MainWindow extends VBox {

    /* Widgets */
    public Text title;
    public TextField target_box;
    public TextField port_box;
    public Slider pkt_slider;
    public Text pkt_size_text;
    public CheckBox isRandCheck;
    public TextField delay_box;
    public TextField thread_box;
    public TextField sock_box;
    public Button attack_btn;
    public Text data_sent;

    public UDPFlooder udpflooder;
    public boolean isRunning = false;

    public MainWindow() {
        setSpacing(10);
        setPadding(new Insets(10, 10, 10, 10));
        //setStyle("-fx-background-color: #383838;");

        // create udp flooder class
        udpflooder = new UDPFlooder( );

        // create widgegts
        create_widgets();

        // start data updater thread
        Thread data_updater = new Thread(new Runnable() {
            @Override
            public void run() {
                update_data();
            }
        });
        data_updater.setDaemon(true);
        data_updater.start();
    }

    public void create_widgets() {

        /* Title Box */
        HBox title_layout = new HBox();
        title_layout.setAlignment(Pos.CENTER);

        title = new Text("UDP Jaynicorn (mini)");
        title.setFont( Font.font("Tahoma", FontWeight.NORMAL, 20));

        title_layout.getChildren().add( title );
        getChildren().add( title_layout );

        /*------------------------*/

        /* Target Box */
        HBox target_layout = new HBox();
        target_layout.setSpacing(10);
        target_layout.setAlignment(Pos.CENTER);

        target_box = new TextField("127.0.0.1");
        port_box   = new TextField("80");
        port_box.setMaxWidth(80);
        setIntOnly( port_box );

        target_layout.getChildren().addAll(new Label("Target:"), target_box);
        addHBoxStrech( target_layout );
        target_layout.getChildren().addAll(new Label("Port:"), port_box);

        getChildren().add( target_layout );

        /*------------------------*/

        /* Packet Box */
        HBox pkt_layout = new HBox();
        pkt_layout.setSpacing(10);
        pkt_layout.setAlignment(Pos.CENTER);

        HBox rand_layout = new HBox();
        addHBoxStrech( rand_layout );

        pkt_slider = new Slider();
        pkt_slider.setMin(1);
        pkt_slider.setMax(10);
        pkt_slider.setValue( udpflooder.pkt_size_kb );
        pkt_slider.setShowTickMarks(true);
        pkt_slider.setBlockIncrement(1);
        pkt_layout.setHgrow( pkt_slider , Priority.ALWAYS);

        pkt_size_text = new Text(String.valueOf(udpflooder.pkt_size_kb)+" kb");
        isRandCheck = new CheckBox("Random size");

        pkt_slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed
            (ObservableValue<? extends Number> ov,Number old_val, Number new_val) {
                udpflooder.pkt_size_kb = (int) pkt_slider.getValue();
                String _kb = Double.toString( udpflooder.pkt_size_kb );
                pkt_size_text.setText( _kb + " kb");
            }
        });
        isRandCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed
            (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                udpflooder.isRandom = newValue;
                if (udpflooder.isRandom) {
                    pkt_slider.setDisable(true);
                } else {
                    pkt_slider.setDisable(false);
                }
            }
        });

        pkt_layout.getChildren().add( new Label("Packet Size:"));
        pkt_layout.getChildren().addAll( pkt_slider , pkt_size_text);
        rand_layout.getChildren().add( isRandCheck );

        getChildren().addAll( pkt_layout, rand_layout);

        /*------------------------*/

        /* Settings Box */

        delay_box = new TextField();
        thread_box = new TextField();
        sock_box = new TextField();

        // Generate text areas
        for (TextField t : new TextField[]{delay_box,thread_box,sock_box}) {
            HBox setting_layout = new HBox();
            setting_layout.setSpacing(10);
            setting_layout.setAlignment(Pos.CENTER);

            setIntOnly( t );

            Label l;
            if (t == delay_box) {
                l = new Label("Delay:");
                t.setText( String.valueOf(udpflooder.delay_ms) );
            }else if (t == thread_box ) {
                l = new Label("Threads:");
                t.setText( String.valueOf(udpflooder.threads) );
            }else {
                l = new Label("Socks per thread:");
                t.setText( String.valueOf(udpflooder.socks_per_thread) );
            }

            setting_layout.getChildren().addAll( l, t );
            getChildren().add( setting_layout );
        }

        /*------------------------*/

        /* Attack box */
        HBox atk_layout = new HBox();
        atk_layout.setAlignment(Pos.CENTER);

        attack_btn = new Button("Attack!");
        attack_btn.setMinHeight( 50 );
        attack_btn.setMinWidth( 120 );
        atk_layout.getChildren().add( attack_btn );

        attack_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (isRunning) {
                        isRunning = false;
                        log("Stopping Flood!");
                        udpflooder.stop();
                        attack_btn.setText("Attack!");
                    } else {
                        isRunning = true;
                        log("Starting Flood!");
                        _start();
                        attack_btn.setText("Stop");
                    }
                }catch (Exception e) { System.out.print(e.getMessage()); }
            }
        });

        addVBoxStrech( this );
        getChildren().add( atk_layout );

        /*------------------------*/

        /* Sent Box */

        data_sent = new Text("0 Kb");

        HBox last_box = new HBox();
        last_box.getChildren().addAll( new Label("Data Sent:  "), data_sent );
        addHBoxStrech( last_box );

        getChildren().add( last_box );
    }

    public void _start() {
        try {
            int thread_num = Integer.valueOf(thread_box.getText());
            udpflooder.threads = thread_num;

            Object[] args = new Object[6];
            args[0] = target_box.getText();
            args[1] = Integer.valueOf( port_box.getText() );
            args[2] = Integer.valueOf( delay_box.getText() );
            args[3] = Integer.valueOf( sock_box.getText() );
            args[4] = (int) pkt_slider.getValue();
            args[5] = udpflooder.isRandom;

            udpflooder.start( args );
        } catch (Exception e) { log("MSG: " +e.getMessage()); }
    }

    /* Update the data sent */
    public void update_data() {
        while (true) {
            try {
                Thread.sleep(100);
                data_sent.setText( String.valueOf(udpflooder.data_sent) + " kb" );
            }catch (Exception e) {}
        }
    }

    /* Set TextFields for int only */
    public void setIntOnly( TextField textbox ) {
        textbox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    textbox.setText(newValue.replaceAll("[^\\\\d]", ""));
                }
            }
        });
    }

    /* Qt-style addStretch to layout */
    public void addHBoxStrech( HBox _layout ) {
        Region spacer = new Region();
        spacer.setMinWidth( Region.USE_PREF_SIZE );
        _layout.setHgrow( spacer , Priority.ALWAYS);
        _layout.getChildren().add( spacer );
    }

    /* Qt-style addStretch to layout */
    public void addVBoxStrech( VBox _layout ) {
        Region spacer = new Region();
        spacer.setMinWidth( Region.USE_PREF_SIZE );
        _layout.setVgrow( spacer , Priority.ALWAYS);
        _layout.getChildren().add( spacer );
    }

    /* For debugging */
    public void log(Object s) {
        System.out.print( s );
        System.out.print("\n");
    }
}
