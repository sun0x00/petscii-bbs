package eu.sblendorio.bbs.tenants;

import eu.sblendorio.bbs.core.Hidden;
import eu.sblendorio.bbs.core.HtmlUtils;
import eu.sblendorio.bbs.core.PetsciiThread;
import org.apache.commons.text.WordUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

import static eu.sblendorio.bbs.core.Keys.*;
import static eu.sblendorio.bbs.core.Colors.*;
import static eu.sblendorio.bbs.core.Utils.filterPrintable;
import static eu.sblendorio.bbs.core.Utils.filterPrintableWithNewline;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.*;

@Hidden
public class WordpressProxy extends PetsciiThread {

    static class Post {
        long id;
        String title;
        String date;
        String content;
        String excerpt;
    }

    protected String domain = "https://wordpress.org/news";
    protected byte[] logo = LOGO_WORDPRESS;
    protected int pageSize = 10;
    protected int screenRows = 19;

    protected Map<Integer, Post> posts = emptyMap();
    protected int currentPage = 1;

    public WordpressProxy() {
        // Mandatory
    }

    public WordpressProxy(String domain) {
        this.domain = domain;
    }

    public WordpressProxy(String domain, byte[] logo) {
        this.domain = domain;
        this.logo = logo;
    }

    protected final String getApi() { return domain + "/wp-json/wp/v2/"; };

    @Override
    public void doLoop() throws Exception {
        write(LOWERCASE, CASE_LOCK);
        log("Wordpress entering (" + domain + ")");
        listPosts();
        while (true) {
            log("Wordpress waiting for input");
            write(WHITE);print("#"); write(GREY3);
            print(", [");
            write(WHITE); print("+-"); write(GREY3);
            print("]Page [");
            write(WHITE); print("H"); write(GREY3);
            print("]elp [");
            write(WHITE); print("R"); write(GREY3);
            print("]eload [");
            write(WHITE); print("."); write(GREY3);
            print("]");
            write(WHITE); print("Q"); write(GREY3);
            print("uit> ");
            resetInput();
            flush(); String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));
            if (".".equals(input) || "exit".equals(input) || "quit".equals(input) || "q".equals(input)) {
                break;
            } else if ("help".equals(input) || "h".equals(input)) {
                help();
                listPosts();
                continue;
            } else if ("+".equals(input)) {
                ++currentPage;
                posts = null;
                try {
                    listPosts();
                } catch (NullPointerException e) {
                    --currentPage;
                    posts = null;
                    listPosts();
                    continue;
                }
                continue;
            } else if ("-".equals(input) && currentPage > 1) {
                --currentPage;
                posts = null;
                listPosts();
                continue;
            } else if ("--".equals(input) && currentPage > 1) {
                currentPage = 1;
                posts = null;
                listPosts();
                continue;
            } else if ("r".equals(input) || "reload".equals(input) || "refresh".equals(input)) {
                posts = null;
                listPosts();
                continue;
            } else if (posts.containsKey(toInt(input))) {
                displayPost(toInt(input));
            } else if ("".equals(input)) {
                listPosts();
                continue;
            } else if ("clients".equals(input)) {
                listClients();
                continue;
            } else if (substring(input,0,5).equalsIgnoreCase("send ")) {
                long client = toLong(input.replaceAll("^send ([0-9]+).*$", "$1"));
                String message = input.replaceAll("^send [0-9]+ (.*)$", "$1");
                if (getClients().containsKey(client) && isNotBlank(message)) {
                    System.out.println("Sending '"+message+"' to #"+client);
                    int exitCode = send(client, message);
                    System.out.println("Message sent, exitCode="+exitCode+".");
                }
            } else if (substring(input,0,5).equalsIgnoreCase("name ")) {
                String newName = defaultString(input.replaceAll("^name ([^\\s]+).*$", "$1"));
                changeClientName(newName);
            } else if (substring (input, 0, 8).equalsIgnoreCase("connect ")) {
                final String oldDomain = domain;
                domain = defaultString(input.replaceAll("^connect ([^\\s]+).*$", "$1"));
                if (!domain.matches("(?is)^http.*"))
                    domain = "https://" + domain;
                log("new API: "+getApi());
                posts = null;
                currentPage = 1;
                try {
                    listPosts();
                } catch (Exception e) {
                    log("WORDPRESS FAILED: " + e.getClass().getName() + ": " + e.getMessage());
                    domain = oldDomain;
                    posts = null;
                    listPosts();
                }
            }
        }
        flush();
    }

    protected Map<Integer, Post> getPosts(int page, int perPage) throws Exception {
        if (page < 1 || perPage < 1) return null;
        Map<Integer, Post> result = new LinkedHashMap<>();
        JSONArray posts = (JSONArray) httpGetJson(getApi() + "posts?context=view&page="+page+"&per_page="+perPage);
        for (int i=0; i<posts.size(); ++i) {
            Post post = new Post();
            JSONObject postJ = (JSONObject) posts.get(i);
            post.id = (Long) postJ.get("id");
            post.content = ((String) ((JSONObject) postJ.get("content")).get("rendered")).replaceAll("(?is)(\\[/?vc_[^]]*\\])*", EMPTY);
            post.title = (String) ((JSONObject) postJ.get("title")).get("rendered");
            post.date = ((String) postJ.get("date")).replace("T", SPACE);
            post.excerpt = (String) ((JSONObject) postJ.get("excerpt")).get("rendered");
            result.put(i+1+(perPage*(page-1)), post);
        }
        return result;
    }

    protected void listPosts() throws Exception {
        cls();
        logo();
        if (isEmpty(posts)) {
            waitOn();
            posts = getPosts(currentPage, pageSize);
            waitOff();
        }
        for (Map.Entry<Integer, Post> entry: posts.entrySet()) {
            int i = entry.getKey();
            Post post = entry.getValue();
            write(WHITE); print(i + "."); write(GREY3);
            final int iLen = 37-String.valueOf(i).length();
            String line = WordUtils.wrap(filterPrintable(HtmlUtils.htmlClean(post.title)), iLen, "\r", true);
            println(line.replaceAll("\r", "\r " + repeat(" ", 37-iLen)));
        }
        newline();
    }

    protected List<String> wordWrap(String s) {
        String[] cleaned = filterPrintableWithNewline(HtmlUtils.htmlClean(s)).split("\n");
        List<String> result = new ArrayList<>();
        for (String item: cleaned) {
            String[] wrappedLine = WordUtils
                    .wrap(item, 39, "\n", true)
                    .split("\n");
            result.addAll(asList(wrappedLine));
        }
        return result;
    }

    protected void help() throws Exception {
        cls();
        logo();
        println();
        println();
        println("Press any key to go back to posts");
        readKey();
    }

    protected void displayPost(int n) throws Exception {
        cls();
        logo();
        final Post p = posts.get(n);
        final String head = p.title + "<br>Date: " + p.date + "<br>---------------------------------------<br>";
        List<String> rows = wordWrap(head);
        List<String> article = wordWrap(p.content);
        rows.addAll(article);

        int page = 1;
        int j = 0;
        boolean forward = true;
        while (j < rows.size()) {
            if (j>0 && j % screenRows == 0 && forward) {
                println();
                write(WHITE);
                print("-PAGE " + page + "-  SPACE=NEXT  -=PREV  .=EXIT");
                write(GREY3);

                resetInput(); int ch = readKey();
                if (ch == '.') {
                    listPosts();
                    return;
                } else if (ch == '-' && page > 1) {
                    j -= (screenRows *2);
                    --page;
                    forward = false;
                    cls();
                    logo();
                    continue;
                } else {
                    ++page;
                }
                cls();
                logo();
            }
            String row = rows.get(j);
            println(row);
            forward = true;
            ++j;
        }
        println();
    }

    protected void waitOn() {
        print("WAIT PLEASE...");
        flush();
    }

    protected void waitOff() {
        for (int i=0; i<14; ++i) write(DEL);
        flush();
    }

    public final static byte[] LOGO_WORDPRESS = new byte[] {
        -104, -84, 32, 32, -84, 32, 32, 32, 32, 32, 32, 32, 32, -84, -94, 13,
        -68, -69, 32, 18, -65, -110, -84, 18, -94, -110, -65, 18, -95, -94, -110, -69,
        18, -84, -110, -65, 18, -95, -110, 32, -95, 18, -84, -110, -65, 18, -95, -94,
        -110, -84, 18, -94, -110, -66, 18, -65, -94, -110, 32, -84, 18, -94, -110, -65,
        18, -95, -94, -110, -69, 18, -65, -94, -110, 13, 32, -65, -65, -66, 18, -95,
        -110, 32, 18, -95, -95, -94, -110, -69, -95, 18, -95, -95, -94, -110, 32, 18,
        -84, -110, -65, 18, -95, -110, -66, 32, 18, -94, -110, -69, -68, -65, 32, 18,
        -95, -110, 32, 18, -95, -95, -94, -110, -69, -95, 18, -69, -110, 13, 32, -68,
        -68, 32, 32, 18, -94, -110, -66, -68, 32, -66, 18, -94, -110, -66, -68, 32,
        32, -66, -68, -68, 18, -94, -110, -68, 18, -94, -110, 32, 18, -94, -110, -66,
        -68, 32, 18, -94, -110, -66, -68, 32, -66, -68, 18, -94, -110, 13
    };

    protected void logo() throws IOException {
        write(logo);
        write(GREY3);
    }

    protected void listClients() throws Exception {
        cls();
        println("You are #" + getClientId() + ": "+getClientName());
        newline();
        for (Map.Entry<Long, PetsciiThread> entry: clients.entrySet())
            if (entry.getKey() != getClientId())
                println("#" + entry.getKey() +": "+entry.getValue().getClientName());
        println();
    }
}