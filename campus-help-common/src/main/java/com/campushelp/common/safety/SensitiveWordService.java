package com.campushelp.common.safety;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SensitiveWordService {

    public enum Mode {
        MASK, REJECT
    }

    @Value("${campus.safety.sensitive.mode:MASK}")
    private String modeRaw;

    private Mode mode = Mode.MASK;
    private final List<String> words = new ArrayList<>();
    private AhoCorasickAutomaton automaton;

    @PostConstruct
    public void load() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("sensitive-words.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String w = line.trim();
                if (!w.isEmpty() && !w.startsWith("#")) {
                    words.add(w.toLowerCase(Locale.ROOT));
                }
            }
        } catch (Exception ignored) {
        }
        automaton = words.isEmpty() ? null : new AhoCorasickAutomaton(words);
        try {
            mode = Mode.valueOf(modeRaw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            mode = Mode.MASK;
        }
    }

    public Mode mode() {
        return mode;
    }

    public boolean containsSensitive(String text) {
        if (text == null || text.isBlank() || automaton == null) {
            return false;
        }
        return !automaton.matchIntervals(AhoCorasickAutomaton.lower(text)).isEmpty();
    }

    public String mask(String text) {
        if (text == null) {
            return null;
        }
        if (automaton == null) {
            return text;
        }
        String low = AhoCorasickAutomaton.lower(text);
        List<int[]> iv = automaton.matchIntervals(low);
        if (iv.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        for (int i = iv.size() - 1; i >= 0; i--) {
            int s = iv.get(i)[0];
            int e = iv.get(i)[1];
            int len = e - s;
            sb.replace(s, e, "*".repeat(Math.min(len, 8)));
        }
        return sb.toString();
    }
}

