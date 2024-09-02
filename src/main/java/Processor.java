import java.io.File;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.ArrayList;

public class Processor {
    private String[] alphaGrad;
    private String[] blockGrad;
    private File res;
    private String returnText;
    private ImagePlus imgPlus;
    private ImageProcessor imgProcessor;
    private double outputScale;
    private int outputResX;
    private int outputResY;
    private ArrayList<ArrayList<Double>> imageMatrix;
    private int pixelCount;
    private int skipLineEvery = 0;

    public Processor() {
        alphaGrad = new String[]{"@", "#", "S", "%", "?", "*", "+", ";", ":", ",", ".", " "};
        blockGrad = new String[]{"█", "▓", "▒", "░", " "};
    }

    public void setRes(File resource) {
        res = resource;
        imgPlus = new ImagePlus(res.getPath());
        imgProcessor = imgPlus.getProcessor();
    }

    public void setOutputScale(double newOutputScale) {
        outputScale = newOutputScale;
        outputResX = (int) (imgProcessor.getWidth() * outputScale);
        outputResY = (int) (imgProcessor.getHeight() * outputScale);

        int skippedLines = 0;
        if (skipLineEvery > 0) {
            skippedLines = outputResY / (skipLineEvery + 1);
        }

        pixelCount = outputResX * (outputResY - skippedLines);
    }

    public int getOutputResX() {
        return outputResX;
    }

    public int getOutputResY() {
        return outputResY;
    }

    public double getOutputScale() {
        return outputScale;
    }

    public int getNumberOfCharacters() {
        return pixelCount;
    }

    public void setSkipLineEvery(int skip) {
        skipLineEvery = skip;
    }

    public String callIJConvert(double brightness, double contrast, format form) {
        imageMatrix = getBrightnessValueArray(brightness, contrast);

        int rowCount = 0;
        returnText = "";

        for (ArrayList<Double> matrix : imageMatrix) {
            if (skipLineEvery > 0 && (rowCount % (skipLineEvery + 1)) == skipLineEvery) {
                rowCount++;
                continue;
            }
            for (Double aDouble : matrix) {
                String character;
                if (aDouble == -1) {
                    character = " ";
                } else {
                    int index;
                    switch (form) {
                        case alpha -> {
                            index = (int) Math.max(0, Math.min(alphaGrad.length - 1, (aDouble / 255.0) * (alphaGrad.length - 1)));
                            character = alphaGrad[index];
                        }
                        case block -> {
                            index = (int) Math.max(0, Math.min(blockGrad.length - 1, (aDouble / 255.0) * (blockGrad.length - 1)));
                            character = blockGrad[index];
                        }
                        default -> {
                            character = " ";
                        }
                    }
                }
                returnText += character;
            }
            returnText += "\n";
            rowCount++;
        }
        return returnText;
    }

    private ArrayList<ArrayList<Double>> getBrightnessValueArray(double contrast, double brightness) {
        ArrayList<ArrayList<Double>> Vals = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < outputResY; i++) {
            ArrayList<Double> xVals = new ArrayList<Double>();
            for (int j = 0; j < outputResX; j++) {
                int originalX = (int) (j / outputScale);
                int originalY = (int) (i / outputScale);

                int p = imgProcessor.getPixel(originalX, originalY);
                Color color = new Color(p, true);
                double val;

                if (color.getAlpha() == 0) {
                    val = -1;
                } else {
                    val = getDeSat(color) + brightness;
                    val = adjustContrast(val, contrast);
                    val = Math.max(0, Math.min(255, val));
                }

                xVals.add(val);
            }
            Vals.add(xVals);
        }
        return Vals;
    }

    private double adjustContrast(double brightness, double contrast) {
        double averageBrightness = 128.0;
        return averageBrightness + (brightness - averageBrightness) * contrast;
    }

    private double getDeSat(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return ((0.2989 * r) + (0.5870 * g) + (0.1140 * b));
    }
}