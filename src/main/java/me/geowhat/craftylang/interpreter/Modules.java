package me.geowhat.craftylang.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Modules {

    public static Map<String, String> modules = new HashMap<>();

    public static void add(String name, String code) {
        modules.putIfAbsent(name, code);
    }

    public static void replace(String name, String code) {
        modules.replace(name, code);
    }

    public static String get(String module) {
        return modules.get(module);
    }
}
