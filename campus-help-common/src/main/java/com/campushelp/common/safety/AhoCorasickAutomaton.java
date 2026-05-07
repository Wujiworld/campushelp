package com.campushelp.common.safety;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

/**
 * 多模式匹配（Aho-Corasick），用于敏感词检测；模式与文本均为小写。
 */
final class AhoCorasickAutomaton {

    static final class Node {
        final Map<Character, Node> next = new HashMap<>();
        Node fail;
        /** 在此节点结束的模式长度列表 */
        final List<Integer> out = new ArrayList<>();
    }

    private final Node root;

    AhoCorasickAutomaton(Iterable<String> patternsLowercase) {
        root = new Node();
        for (String p : patternsLowercase) {
            if (p == null || p.isEmpty()) {
                continue;
            }
            Node n = root;
            for (int i = 0; i < p.length(); i++) {
                char c = p.charAt(i);
                n = n.next.computeIfAbsent(c, k -> new Node());
            }
            n.out.add(p.length());
        }
        buildFailLinks();
    }

    private void buildFailLinks() {
        Queue<Node> q = new ArrayDeque<>();
        for (Node ch : root.next.values()) {
            ch.fail = root;
            q.add(ch);
        }
        while (!q.isEmpty()) {
            Node state = q.poll();
            for (Map.Entry<Character, Node> e : state.next.entrySet()) {
                char c = e.getKey();
                Node child = e.getValue();
                Node f = state.fail;
                while (f != root && !f.next.containsKey(c)) {
                    f = f.fail;
                }
                child.fail = f.next.getOrDefault(c, root);
                child.out.addAll(child.fail.out);
                q.add(child);
            }
        }
    }

    /**
     * @return 合并后的匹配区间 [start, end)，基于 {@code textLower} 下标（与原始文本等长，已小写）。
     */
    List<int[]> matchIntervals(String textLower) {
        if (textLower == null || textLower.isEmpty() || root.next.isEmpty()) {
            return Collections.emptyList();
        }
        List<int[]> raw = new ArrayList<>();
        Node cur = root;
        for (int i = 0; i < textLower.length(); i++) {
            char c = textLower.charAt(i);
            while (cur != root && !cur.next.containsKey(c)) {
                cur = cur.fail;
            }
            cur = cur.next.getOrDefault(c, root);
            for (int len : cur.out) {
                raw.add(new int[]{i - len + 1, i + 1});
            }
        }
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        raw.sort(Comparator.comparingInt((int[] a) -> a[0]).thenComparingInt(a -> a[1]));
        List<int[]> merged = new ArrayList<>();
        int cs = raw.get(0)[0];
        int ce = raw.get(0)[1];
        for (int k = 1; k < raw.size(); k++) {
            int s = raw.get(k)[0];
            int e = raw.get(k)[1];
            if (s < ce) {
                ce = Math.max(ce, e);
            } else {
                merged.add(new int[]{cs, ce});
                cs = s;
                ce = e;
            }
        }
        merged.add(new int[]{cs, ce});
        return merged;
    }

    static String lower(String text) {
        return text == null ? null : text.toLowerCase(Locale.ROOT);
    }
}
