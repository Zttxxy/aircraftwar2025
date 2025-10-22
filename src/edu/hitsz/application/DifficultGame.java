package edu.hitsz.application;
import java.awt.image.BufferedImage;

public class DifficultGame extends Game {
    public DifficultGame() {
        super(ImageManager.BACKGROUND_IMAGE_DIFFICULT); // 困难模式背景
        setDifficulty(Difficulty.HARD);
    }
}