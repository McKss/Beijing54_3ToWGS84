import org.gdal.gdal.gdal;
import org.gdal.ogr.*;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import java.util.ArrayList;

import static org.gdal.ogr.ogrConstants.*;

public class QueryBaidudituBuildingInfo {
    public QueryBaidudituBuildingInfo() {
        QueryBaidudituBuildingInfo.InitGDAL();
    }

    public void OpenShpFile(String shpFileName) {
        this.shpFileName = shpFileName;
        dataSource = ogr.Open(shpFileName, 1);
        buildingLayer = dataSource.GetLayerByIndex(0);
        this.buildingCenter = new ArrayList<>();
    }

    public void SetAk(String ak) {
        this.ak = ak;
    }

    public static void ViewShpFile(String fileName, int layer, int firstNRow) {
        DataSource dataSource = ogr.Open(fileName);
        Layer mpLayer = dataSource.GetLayerByIndex(layer);
        for (int i = 0; i < firstNRow && i < mpLayer.GetFeatureCount(); i++) {
            Feature feature = mpLayer.GetFeature(i);
            for (int j = 0; j < feature.GetFieldCount(); j++) {
                System.out.print(feature.GetFieldAsString(j) + " | ");
            }
            System.out.println();
        }
    }

    // 房屋中心，粗略的
    private static void GetGeomCenter(double[][] vertices, double[] center) {
        double xSum = 0.0, ySum = 0.0;
        for (int i = 0; i < vertices.length; i++) {
            xSum += vertices[i][0];
            ySum += vertices[i][1];
        }
        center[0] = xSum / vertices.length;
        center[1] = ySum / vertices.length;
    }

    private void _print_center() {
        for (int i = 0; i < this.buildingCenter.size(); i++) {
            System.out.println(i + ", (" + this.buildingCenter.get(i)[0] + ", " + this.buildingCenter.get(i)[1] + ")");
        }
    }

    public void GetBuildingCenter() {
        for (int i = 0; i < buildingLayer.GetFeatureCount(); i++) {
            Feature building = buildingLayer.GetFeature(i);
            Geometry poly = building.GetGeometryRef();

//            double[][] vertices = poly.GetGeometryRef(0).GetPoints(2); // MULTISTRING不能GetPoints
//            double[] center = new double[2];
//            GetGeomCenter(vertices, center);

            double[] center = new double[]{poly.Centroid().GetX(), poly.Centroid().GetY()};
            this.buildingCenter.add(center);

            // 是对道路提取中心
//            if (poly.GetGeometryName().equals("MULTILINESTRING")) { // MULTILINESTRING的Geometry，poly.GetGeometryCount获取其中LINSTRING数目，poly.GetGeometryRef(i)获取LINESTRING
//                for (int j = 0; j < poly.GetGeometryCount(); j++) {
//                    double[] center = new double[2];
//                    GetGeomCenter(poly.GetGeometryRef(j).GetPoints(2), center);
//                    this.buildingCenter.add(center);
//                }
//            }
//            else {
//                double[][] vertices = poly.GetPoints(2); // MULTISTRING不能GetPoints
//                double[] center = new double[2];
//                GetGeomCenter(vertices, center);
//                this.buildingCenter.add(center);
//            }
        }
        //_print_center();
    }

    public void CreateBuildingCenterLayer() {
        buildingCenterLayer = dataSource.CreateLayer("BUILDING_CENTER", this.buildingLayer.GetSpatialRef(), wkbPoint); // geom_type到底是什么？
        FieldDefn xDefn = new FieldDefn("X", OFTReal);
        buildingCenterLayer.CreateField(xDefn);
        FieldDefn yDefn = new FieldDefn("Y", OFTReal);
        buildingCenterLayer.CreateField(yDefn);
        FieldDefn addrDefn = new FieldDefn("Address", OFTString); // 是否手动设定长度？
        buildingCenterLayer.CreateField(addrDefn);
        FieldDefn buildingNumDefn = new FieldDefn("BuildingNo", OFTString);
        buildingCenterLayer.CreateField(buildingNumDefn);
    }

    public void SetBuildingCenterFeatureInfo() {
        FeatureDefn featureDefn = buildingCenterLayer.GetLayerDefn();
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
        CoordinateTransformation BJ54ToWGS84Trans = CoordinateTransformation.CreateCoordinateTransformation(beijing54_3degree, wgs84);

        // 测试bd09坐标具体位置
        String coord_bd09_txt = new String("x\ty\tgeocoder_json\r\n");
        String csvFileName = "coord_bd09.txt";

        for (int i = 0; i < this.buildingCenter.size(); i++) {
            Feature featurePoint = new Feature(featureDefn);
            double x = this.buildingCenter.get(i)[0], y = this.buildingCenter.get(i)[1];
            featurePoint.SetField("X", x);
            featurePoint.SetField("Y", y);
            Geometry geomPoint = new Geometry(wkbPoint);
            geomPoint.SetPoint(0, x, y);
            featurePoint.SetGeometry(geomPoint);

            double[] coord_bd09 = new double[2];
            BaidudituAPI.OtherSRSToBd09(BJ54ToWGS84Trans,1, 5, coord_bd09, x, y, this.ak);
//            System.out.println(coord_bd09[0] + ", " + coord_bd09[1]);
            coord_bd09_txt += coord_bd09[0] + ", " + coord_bd09[1] + ", ";

            // ？直接记录json太长不能保存
            String addrJson = BaidudituAPI.QueryGeocoderJsonFromBd09LngLat(coord_bd09[0], coord_bd09[1], this.ak);
            coord_bd09_txt += addrJson + "\r\n";
//            featurePoint.SetField("Address", addrJson);

            String addr = BaidudituAPI.QueryGeocoderInfoFromBd09LngLat(coord_bd09[0], coord_bd09[1], "sematic_description", this.ak);
            //System.out.println(addr);
            featurePoint.SetField("Address", addr);

            String buildingNumStr = ModifyFeature.semantic_description2buildingnum(addr);
            if (buildingNumStr != "-1") {
                featurePoint.SetField("BuildingNo", buildingNumStr);
            }

            buildingCenterLayer.CreateFeature(featurePoint);
        }
        Tools.WriteToFile(csvFileName, coord_bd09_txt);
    }

    public void Save() {
        buildingCenterLayer.SyncToDisk();
        dataSource.SyncToDisk();
        dataSource.delete();
    }

    private String shpFileName;
    private String ak;
    private DataSource dataSource;
    private Layer buildingLayer;
    private ArrayList<double[]> buildingCenter; // point
    private Layer buildingCenterLayer;
    //private String[] buildingAddress; // 按顺序对应记录buildingCenter的地址

    private static void InitGDAL() {
        if (!init) {
            ogr.RegisterAll();
            // 为了支持中文路径，请添加下面这句代码
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
            // 为了使属性表字段支持中文，请添加下面这句
            gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
            init = true;
        }
    }

    private static boolean init = false;
}
