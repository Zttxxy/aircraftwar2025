package edu.hitsz.application;

import javax.sound.sampled.*;
import java.io.*;

public class MusicThread extends Thread {

    private String filename;
    private AudioFormat audioFormat;
    private byte[] samples;
    private boolean loop = false;
    private boolean stopFlag = false;

    public MusicThread(String filename) {
        this.filename = filename;
        loadMusic();
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void stopMusic() {
        stopFlag = true;
    }

    private void loadMusic() {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
            audioFormat = stream.getFormat();
            samples = getSamples(stream);
            System.out.println("音乐加载成功：" + filename);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("音乐加载失败：" + filename);
        }
    }

    private byte[] getSamples(AudioInputStream stream) throws IOException {
        int size = (int) (stream.getFrameLength() * audioFormat.getFrameSize());
        byte[] samples = new byte[size];
        DataInputStream dataInputStream = new DataInputStream(stream);
        dataInputStream.readFully(samples);
        return samples;
    }

    private void play(InputStream source) {
        int size = (int) (audioFormat.getFrameSize() * audioFormat.getSampleRate());
        byte[] buffer = new byte[size];
        try {
            SourceDataLine dataLine = AudioSystem.getSourceDataLine(audioFormat);
            dataLine.open(audioFormat, size);
            dataLine.start();

            int numBytesRead = 0;
            while (!stopFlag) {
                numBytesRead = source.read(buffer, 0, buffer.length);
                if (numBytesRead == -1) break;
                dataLine.write(buffer, 0, numBytesRead);

                if (numBytesRead == -1 && loop) {
                    source = new ByteArrayInputStream(samples); // 循环
                    numBytesRead = 0;
                }
            }

            dataLine.drain();
            dataLine.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("播放出错：" + filename);
        }
    }

    @Override
    public void run() {
        InputStream stream = new ByteArrayInputStream(samples);
        play(stream);
    }
}
