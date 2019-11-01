import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.gdal.osr.CoordinateTransformation;


public class BaidudituAPI {

    // CoordinateTransformation coordinateTransformation: Other srs to WGS84，GCJ02
    // int from: http://lbsyun.baidu.com/index.php?title=webapi/guide/changeposition
    // int to: 同上
    // double[] coord_bd09: out, length = 2
    public static void OtherSRSToBd09(CoordinateTransformation otherSrsToStdSrs, int from, int to, double[] coord_bd09, double x, double y, String ak) {
        double[] srsStdOut = new double[3];
        otherSrsToStdSrs.TransformPoint(srsStdOut, x, y); // 转化到WGS84或GCJ02大地坐标
        // http://api.map.baidu.com/geoconv/v1/?coords=114.21892734521,29.575429778924&from=1&to=5&ak=zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL
        String queryBd09CoordJson = null;
        try {
            String url = "http://api.map.baidu.com/geoconv/v1/?coords=" + srsStdOut[0] + "," + srsStdOut[1]
                    + "&from=" + from + "&to=" + to + "&ak=" + ak;
            queryBd09CoordJson = GetBaiduResult(url);
        } catch (IOException e) { e.printStackTrace(); }
        //System.out.println(queryBd09CoordJson);
        coord_bd09[0] = Double.parseDouble(Tools.GetJsonValue(queryBd09CoordJson, "x"));
        coord_bd09[1] = Double.parseDouble(Tools.GetJsonValue(queryBd09CoordJson, "y"));
    }

    public static String QueryGeocoderJsonFromBd09LngLat(double lng, double lat, String ak) {
        String addrJson = null;
        try {
            addrJson = GetBaiduResult("http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location="
                    + lat + "," + lng + "&output=json&ak=" + ak);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addrJson;
    }

    // attrType见百度地图 全球逆地理编码服务 返回json格式
    public static String QueryGeocoderInfoFromBd09LngLat(double lng, double lat, String attrType, String ak) {
        String addrJson = QueryGeocoderJsonFromBd09LngLat(lng, lat, ak);
        String addr = addrJson != null ? Tools.GetJsonValue(addrJson, attrType) : "null";
        return addr;
    }

    public static String GetBaiduResult(String url) throws IOException {
        URL myUrl = new URL(url);
        URLConnection httpsConn = null;
        InputStreamReader insr = null;
        BufferedReader br = null;
        String data = null;
        try {
            httpsConn = (URLConnection) myUrl.openConnection();
            if (httpsConn != null) {
                insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");
                br = new BufferedReader(insr);
                data = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (insr != null) {
                insr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return data;
    }

    public static String GetBaiduResultInJSON(String addr) throws IOException {
        String address = null;
        try{
            address = java.net.URLEncoder.encode(addr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "http://api.map.baidu.com/geocoder/v2/?address=" + address + "&output=json&ak=zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL";
        String data = GetBaiduResult(url);
        return data;
    }

    // http://api.map.baidu.com/geocoder/v2/?address=仙桥金华汽车城横一路&output=json&ak=zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL
    // 如果找到：
    // {"status":0,"result":{"location":{"lng":119.7052526082608,"lat":29.15314477922925},"precise":0,"confidence":50,"comprehension":43,"level":"道路"}}
    // {
    //    "status": 0,
    //    "result": {
    //        "location": {
    //            "lng": 119.69822431715649,
    //            "lat": 29.148487790283409
    //        },
    //        "precise": 1,
    //        "confidence": 75,
    //        "comprehension": 100,
    //        "level": "汽车服务"
    //    }
    // }
    // 如果找不到：
    // {"status":1,"msg":"Internal Service Error:无相关结果","results":[]}
    // {
    //    "status": 1,
    //    "msg": "Internal Service Error:无相关结果",
    //    "results": [
    //
    //    ]
    // }
    // 如果不输入：
    // {"status":2,"msg":"Request Parameter Error: lack address or location","results":[]}
    // {
    //    "status": 2,
    //    "msg": "Request Parameter Error: lack address or location",
    //    "results": [
    //
    //    ]
    //}
    public static double[] getLngLat(String json) {
        double stateCode = Double.valueOf(json.substring(json.indexOf("\"status\"") + 9, json.indexOf(",")));
        if (stateCode != 0.0) {
            if (stateCode == 1.0) {
                System.out.println("无相关结果");
            }
            else if (stateCode == 2.0) {
                System.out.println("未输入");
            }
            else {
                System.out.println("其他错误");
            }
            return new double[]{stateCode, 0.0, 0.0};
        }
        int lngBeginIndex = json.indexOf("\"lng\":") + 6;
        double lng = Double.valueOf(json.substring(lngBeginIndex, json.indexOf(",\"lat\"")));
        int latBeginIndex = json.indexOf("\"lat\":") + 6;
        double lat = Double.valueOf(json.substring(latBeginIndex, json.indexOf("},\"precise\"")));
        return new double[]{stateCode, lng, lat};
    }

    /**
     * @param addr 查询的地址
     * @return
     * @throws IOException
     */
//    public String[] getCoordinate(String addr) throws IOException {
//        String lng = null;//经度
//        String lat = null;//纬度
//        String address = null;
//        try {
//            address = java.net.URLEncoder.encode(addr, "UTF-8");
//        } catch (UnsupportedEncodingException e1) {
//            e1.printStackTrace();
//        }
//        //System.out.println(address);
//        String url = "http://api.map.baidu.com/geocoder/v2/?output=json&ak=您的ak&address=" + address;
//        URL myURL = null;
//
//        URLConnection httpsConn = null;
//        try {
//            myURL = new URL(url);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        InputStreamReader insr = null;
//        BufferedReader br = null;
//        try {
//            httpsConn = (URLConnection) myURL.openConnection();
//            if (httpsConn != null) {
//                insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");
//                br = new BufferedReader(insr);
//                String data = null;
//                while ((data = br.readLine()) != null) {
//                    JSONObject json = JSONObject.parseObject(data);
//                    lng = json.getJSONObject("result").getJSONObject("location").getString("lng");
//                    lat = json.getJSONObject("result").getJSONObject("location").getString("lat");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (insr != null) {
//                insr.close();
//            }
//            if (br != null) {
//                br.close();
//            }
//        }
//        return new String[]{lng, lat};
//    }

//    public String[] getAddr(String lng, String lat) throws IOException {
//
//        String url = "http://api.map.baidu.com/geocoder/v2/?output=json&ak=您的ak&location=" + lat + "," + lng;
//        URL myURL = null;
//        String city = "";
//        String qx = "";
//        URLConnection httpsConn = null;
//        try {
//            myURL = new URL(url);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        InputStreamReader insr = null;
//        BufferedReader br = null;
//        try {
//            httpsConn = (URLConnection) myURL.openConnection();// 不使用代理
//            if (httpsConn != null) {
//                insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");
//                br = new BufferedReader(insr);
//                String data = null;
//                while ((data = br.readLine()) != null) {
//                    JSONObject json = JSONObject.parseObject(data);
//                    city = json.getJSONObject("result").getJSONObject("addressComponent").getString("city");
//                    qx = json.getJSONObject("result").getJSONObject("addressComponent").getString("district");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (insr != null) {
//                insr.close();
//            }
//            if (br != null) {
//                br.close();
//            }
//        }
//        return new String[]{city, qx};
//    }
}