package com.danrus.bb4j.model.project;

import java.util.List;
import java.util.Map;

public class History {
    private List<HistoryEntry> history;
    private Integer historyIndex;

    public History() {}

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryEntry> history) {
        this.history = history;
    }

    public Integer getHistoryIndex() {
        return historyIndex;
    }

    public void setHistoryIndex(Integer historyIndex) {
        this.historyIndex = historyIndex;
    }

    public static class HistoryEntry {
        private Map<String, Object> before;
        private Map<String, Object> post;
        private String action;
        private Long time;

        public HistoryEntry() {}

        public Map<String, Object> getBefore() {
            return before;
        }

        public void setBefore(Map<String, Object> before) {
            this.before = before;
        }

        public Map<String, Object> getPost() {
            return post;
        }

        public void setPost(Map<String, Object> post) {
            this.post = post;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }
    }
}
