import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import java.io.IOException;

public class MyTest {
    // 测试 北京54大地坐标 -> WGS84大地坐标 -> 百度09大地坐标
    public static void TestTransformation() {
        double x = 120.0, y = 30.0;
        SpatialReference beijing54 = new SpatialReference();
        beijing54.SetWellKnownGeogCS("EPSG:4214"); // 阅读geoserver layers中的wkt得大地坐标系为北京1954（不要用wkt构造SpatialReference，其代表投影坐标）
        SpatialReference wgs84 = new SpatialReference();
        int state2 = wgs84.SetWellKnownGeogCS("WGS84");
        double[] out = new double[3];
        CoordinateTransformation coordinateTransformation = CoordinateTransformation.CreateCoordinateTransformation(beijing54, wgs84);
        BaidudituAPI.OtherSRSToBd09(coordinateTransformation,1, 5, out, x, y, "zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL");
        System.out.println("state2 = " + state2 +"\nx = " + out[0] + ", y = " + out[1]);
    }

    // 测试从北京54三度带投影坐标 -> wgs84大地坐标 -> 百度09大地坐标
    public static void TestProjectCoordToGeodeticCoord() {
        double x = 509757.17690, y = 3247764.57959;
        SpatialReference beijing54_3degree = new SpatialReference("PROJCS[\"Beijing 1954 / 3-degree Gauss-Kruger CM 120E\", \n" +
                "  GEOGCS[\"Beijing 1954\", \n" +
                "    DATUM[\"Beijing 1954\", \n" +
                "      SPHEROID[\"Krassowsky 1940\", 6378245.0, 298.3, AUTHORITY[\"EPSG\",\"7024\"]], \n" +
                "      TOWGS84[15.8, -154.4, -82.3, 0.0, 0.0, 0.0, 0.0], \n" +
                "      AUTHORITY[\"EPSG\",\"6214\"]], \n" +
                "    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n" +
                "    UNIT[\"degree\", 0.017453292519943295], \n" +
                "    AXIS[\"Geodetic longitude\", EAST], \n" +
                "    AXIS[\"Geodetic latitude\", NORTH], \n" +
                "    AUTHORITY[\"EPSG\",\"4214\"]], \n" +
                "  PROJECTION[\"Transverse_Mercator\"], \n" +
                "  PARAMETER[\"central_meridian\", 120.0], \n" +
                "  PARAMETER[\"latitude_of_origin\", 0.0], \n" +
                "  PARAMETER[\"scale_factor\", 1.0], \n" +
                "  PARAMETER[\"false_easting\", 500000.0], \n" +
                "  PARAMETER[\"false_northing\", 0.0], \n" +
                "  UNIT[\"m\", 1.0], \n" +
                "  AXIS[\"Easting\", EAST], \n" +
                "  AXIS[\"Northing\", NORTH], \n" +
                "  AUTHORITY[\"EPSG\",\"2437\"]]");
        SpatialReference wgs84 = new SpatialReference();
        wgs84.SetWellKnownGeogCS("WGS84");
        CoordinateTransformation coordinateTransformation = CoordinateTransformation.CreateCoordinateTransformation(beijing54_3degree, wgs84);
        double[] out = new double[3];
        BaidudituAPI.OtherSRSToBd09(coordinateTransformation, 1, 5, out, x, y, "zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL");
        System.out.println(out[0]);
        System.out.println(out[1]);
    }

    public static void TestBaidudituURL() {
        String data = null;
        try {
            data = BaidudituAPI.GetBaiduResult("http://api.map.baidu.com/geoconv/v1/?coords=114.21892734521,29.575429778924&from=1&to=5&ak=zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(data);
    }

    public static void TestSemanticDesc2BuildingNumStr() {
        String desc1 = "银海二区内,银海2区-32幢附近38米";
        String desc2 = "银海二区内,银海2区-36号楼附近45米";
        String desc3 = "银海一区内,银海1区-13幢附近29米";


        String buildingNumStr = ModifyFeature.semantic_description2buildingnum(desc3);
        System.out.println(buildingNumStr);
    }
    public static void TestQueryBaidudituBuildingInfo() {
        // 处理数据
//        QueryBaidudituBuildingInfo queryBaidu = new QueryBaidudituBuildingInfo();
//        queryBaidu.OpenShpFile("D:\\files\\learn\\Beijing54_3ToWGS84\\DLG_SHP\\RES_PY_K_Clip.shp");
//        queryBaidu.SetAk("zVfGPtMqGUT4c6kFgNSiW7TGVuy0DtuL");
//        queryBaidu.GetBuildingCenter();
//        queryBaidu.CreateBuildingCenterLayer();
//        queryBaidu.SetBuildingCenterFeatureInfo();
//        queryBaidu.Save();

        // 显示结果
//        ogr.RegisterAll();
//        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
//        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
//        QueryBaidudituBuildingInfo.ViewShpFile("D:\\files\\learn\\Beijing54_3ToWGS84\\DLG_SHP\\BUILDING_CENTER.shp", 0, 20);
    }

    public static void main(String[] args) {
        TestQueryBaidudituBuildingInfo();
    }
}
