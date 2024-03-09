package me.geowhat.craftylang.interpreter.syntax;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.geowhat.craftylang.client.CraftyLangClient;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ColorScheme {

    private int defaultColor;
    private int keywordColor;
    private int stringColor;
    private int commentColor;
    private int preprocessorColor;

    private String name;

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static ColorScheme fromJson(String filepath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filepath)) {
            return gson.fromJson(reader, ColorScheme.class);
        } catch (IOException e) {
            CraftyLangClient.logger.error(e);
            return new ColorScheme();
        }
    }

    public static ColorScheme fromString(String contents) {
        Gson gson = new Gson();
        return gson.fromJson(contents, ColorScheme.class);
    }

    public static ColorScheme fromJson(InputStream stream) throws IOException {
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        String contents = scanner.hasNext() ? scanner.next() : "";

        stream.close();
        scanner.close();

        return fromString(contents);
    }

    public static SyntaxColorPalette fromColorScheme(ColorScheme scheme) {
        return new SyntaxColorPalette(
                scheme.getDefaultColor(),
                scheme.getKeywordColor(),
                scheme.getStringColor(),
                scheme.getNumberColor(),
                scheme.getCommentColor(),
                scheme.getPreprocessorColor()
        );
    }

    public SyntaxColorPalette toSyntaxColorPalette() {
        return new SyntaxColorPalette(
                this.defaultColor,
                this.keywordColor,
                this.stringColor,
                this.numberColor,
                this.commentColor,
                this.preprocessorColor
        );
    }

    public void saveJsonFile(String filepath) {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(toJson());
        } catch (IOException e) {
            CraftyLangClient.logger.error(e);
        }
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public int getKeywordColor() {
        return keywordColor;
    }

    public void setKeywordColor(int keywordColor) {
        this.keywordColor = keywordColor;
    }

    public int getStringColor() {
        return stringColor;
    }

    public void setStringColor(int stringColor) {
        this.stringColor = stringColor;
    }

    public int getNumberColor() {
        return numberColor;
    }

    public void setNumberColor(int numberColor) {
        this.numberColor = numberColor;
    }

    private int numberColor;

    public int getCommentColor() {
        return commentColor;
    }

    public void setCommentColor(int commentColor) {
        this.commentColor = commentColor;
    }

    public int getPreprocessorColor() {
        return preprocessorColor;
    }

    public void setPreprocessorColor(int preprocessorColor) {
        this.preprocessorColor = preprocessorColor;
    }

    public ColorScheme setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}
