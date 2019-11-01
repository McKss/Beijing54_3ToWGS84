import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class Tools {
    // test，返回json属性值
    public static String GetJsonValue(String json, String attr) {
        String a = new String("\"" + attr + "\":");
        if (!json.contains(a)) { return "erro 0: cannot find attr"; }
        int beg = json.indexOf(a) + attr.length() + 3; // 冒号后面
        while (json.charAt(beg) == ' ') { ++beg; } // 吃空格
        Stack<Character> s = new Stack<>();
        int cur = beg;
        while (cur == beg || !s.empty()) {
            switch (json.charAt(cur)) {
                case '{': {
                    s.push('{');
                }
                break;
                case '}': {
                    if (s.peek() == '{') { s.pop(); }
                }
                break;
                case '\"': {
                    if (!s.empty() && s.peek() == '\"') { s.pop(); }
                    else { s.push('\"'); }
                }
                break;
                default: {}
                break;
            }
            ++cur;
        }
        while (json.charAt(cur) != ',' && json.charAt(cur) != '}') { ++cur; }

        // 如果值是字符串，去除头尾引号(吃尾引号后面可能有空格)
        if (json.charAt(beg) == '\"') {
            ++beg;
            while (json.charAt(cur - 1) != '\"') { --cur; }
            --cur;
        }

        String value = json.substring(beg, cur);
        return value;
    }

    public static void WriteToFile(String fileName, String content) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
        FileWriter fw = new FileWriter(fileName);
        fw.write(content);
        fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
