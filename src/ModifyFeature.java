import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifyFeature {
    // 不符合要求，没有提取出形如“xx路201-2号”的地址
//    public static int addr2doornum(String addr) {
//        String rx = ".+(巷|路|街|弄|里|道|段)[0-9]{1,}号.*";
//        if (!addr.matches(rx)) { // 不含门牌号
//            return -1;
//        }
//        Pattern p = Pattern.compile("(巷|路|街|弄|里|道|段)[0-9]{1,}号");
//        Matcher m = p.matcher(addr);
//        m.find();
//        String substr = m.group();
//        String numstr = substr.substring(1, substr.length() - 1);
//        return Integer.parseInt(numstr);
//    }

    public static String addr2doornum(String addr) {
        String rx = ".+[巷路街弄里道段](([0-9]+)|([0-9]+[-]?[0-9]+))号.*";
        if (!addr.matches(rx)) { // 不含门牌号
            return "-1";
        }
        Pattern p = Pattern.compile("[巷路街弄里道段](([0-9]+)|([0-9]+[-]?[0-9]+))号");
        Matcher m = p.matcher(addr);
        m.find();
        String substr = m.group();
        String numstr = substr.substring(1, substr.length() - 1);
        return numstr;
    }

    // 从百度Geocoder服务返回的json中的semantic_description中提取楼栋号
    public static String semantic_description2buildingnum(String desc) {
        String rx = ".+[0-9]+(幢|栋|(号楼)).*";
        if (!desc.matches(rx)) {
            return "-1";
        }
        Pattern p = Pattern.compile("[0-9]+(幢|栋|(号楼))");
        Matcher m = p.matcher(desc);
        m.find();
        String substr = m.group();
        int cur = 0;
        while (Character.isDigit(substr.charAt(cur))) { ++cur; }
        String buildingnumstr = substr.substring(0,cur);
        return buildingnumstr;
    }

}
