package dev.tr7zw.mango_companion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

@Getter
public class Config {

    private Set<String> urls = new HashSet<>();
    private Map<String, String> folderOverwrites = new HashMap<>();
    private int sleepInMinutes = 60;
}
