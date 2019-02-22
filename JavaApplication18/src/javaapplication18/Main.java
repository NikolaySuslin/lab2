package javaapplication18;

import java.io.File;
import java.io.IOException;

import java.net.*;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

class AudioReceiver{

    public void work() throws IOException, LineUnavailableException {

        System.out.println("Client started");
        Socket socket = new Socket("", 6800);
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        SourceDataLine sLine = (SourceDataLine) AudioSystem.getLine(info);
        sLine.open(audioFormat);
        sLine.start();

        byte[] buffer = new byte[4096];
        DatagramSocket client = new DatagramSocket(socket.getLocalPort());
        try{
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                client.receive(packet);
                buffer = packet.getData();
                sLine.write(packet.getData(), 0, buffer.length);
            }
        } finally {
            client.close();
            sLine.close();
        }
    }
}

class AudioPlayerServer {

    private ServerSocket serverSocket;
    AudioPlayerServer(){
    }

    public void work() throws  IOException, UnsupportedAudioFileException, InterruptedException {
        serverSocket = new ServerSocket(6800);
        System.out.println("Server started");

        Socket socket = serverSocket.accept();
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:\\PROJECT\\JavaApplication18\\src\\javaapplication18\\test.wav"));
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);

        byte bytes[] =  new byte[4096];

        double sleepTime = (1024 / audioFormat.getSampleRate());
        long sleepTimeMillis = (long)(sleepTime * 1000);
        int sleepTimeNanos = (int)((sleepTime * 1000 - sleepTimeMillis) * 1000000);

        DatagramSocket server = new DatagramSocket();

        while ((audioInputStream.read(bytes, 0, bytes.length))!= -1) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, socket.getInetAddress(), socket.getPort());
            server.send(packet);
            Thread.sleep(sleepTimeMillis,sleepTimeNanos);
        }
        audioInputStream.close();
        server.close();
        System.out.println("No bytes anymore !");
        System.exit(0);
    }
}

public class Main {
    public static void main(String[] args) {
        AudioReceiver audioReceiver = new AudioReceiver();
        AudioPlayerServer audioPlayerServer = new AudioPlayerServer();

        new Thread(()->{
            try {
                audioPlayerServer.work();
            } catch (IOException | UnsupportedAudioFileException | InterruptedException ex) {
            }
        }).start();

        new Thread(()->{
            try {
                //Thread.sleep(5000);
                audioReceiver.work();
            } catch (IOException | LineUnavailableException ex) {
            }
        }).start();
    }
}