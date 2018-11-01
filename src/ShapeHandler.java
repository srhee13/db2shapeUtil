import com.vividsolutions.jts.geom.*;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.util.*;

public class ShapeHandler {

    public void write(String filepath, String filename,String shpType,String shpField, List<Map<String,Object>> schema,List<Map<String,Object>> values) throws IOException {

        File file = new File(filepath+"\\"+filename+".shp");
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );
        ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setCRS(DefaultGeographicCRS.WGS84);
        tb.setName(filename);
        Iterator<Map<String,Object>> iterable = schema.iterator();
        while (iterable.hasNext()){
            Map<String,Object> map = iterable.next();
            int nullAble = (Integer) map.get("NULLABLE");
            boolean flag = true;
            if(nullAble == 0){
                flag = false;
            }
            Class clazz = null;
            String dataTypeName = (String)map.get("TYPE_NAME");
            switch (dataTypeName.toLowerCase()){
                case "smallint":
                    clazz = Short.class;
                    break;
                case "integer":
                    clazz = Integer.class;
                    break;
                case "long":
                    clazz = Long.class;
                    break;
                case "decimal":
                case "float":
                    clazz = Float.class;
                    break;
                case "char":
                case "varchar":
                case "graphic":
                case "vargraphic":
                case "text":
                    clazz = String.class;
                    break;
                case "double":
                    clazz = Double.class;
                    break;
                case "date":
                case "time":
                case "timestamp":
                case "datetime":
                    clazz = Date.class;
                    break;
                case "blob":
                    clazz = Blob.class;
                    break;
                default:
                    clazz = String.class;
                    break;

            }
            tb.length((Integer) map.get("COLUMN_SIZE"))
                    .defaultValue(map.get("COLUMN_DEF"))
                    .description((String) map.get("REMARKS"))
                    .nillable(flag).add((String) map.get("COLUMN_NAME"),clazz);
        }
        if(shpType.equalsIgnoreCase("POINT")){
            tb.add("the_geom", Point.class);
        }else if(shpType.equalsIgnoreCase("POLYGON")){
            tb.add("the_geom", Polygon.class);
        }else if(shpType.equalsIgnoreCase("POLYLINE")){
            tb.add("the_geom", LineString.class);
        }else {
            tb.add("the_geom", Polygon.class);
        }

        ds.createSchema(tb.buildFeatureType());
        ds.setStringCharset(Charset.forName("GBK"));

        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);

        SimpleFeature feature = null;
        Iterator<Map<String,Object>>  iterator= values.iterator();
        while (iterator.hasNext()){
            Map<String,Object> map = iterator.next();
            feature = writer.next();
            for(Map<String,Object> m :schema){
                feature.setAttribute((String) m.get("COLUMN_NAME"),map.get(m.get("COLUMN_NAME")));
            }
            Geometry geo = this.getShape(shpType,map.get(shpField));

//                Coordinate[] coordinates = new Coordinate[]{new Coordinate(114.28,22.52),new Coordinate(114.88,23),new Coordinate(115.28,24.52),new Coordinate(114.28,22.52)};
            feature.setAttribute("the_geom",geo);
        }
        writer.write();
        writer.close();
        ds.dispose();

        ShpFiles shpFiles = new ShpFiles(filepath+"\\"+filename+".shp");
        ShapefileReader reader = new ShapefileReader(shpFiles, false, true, new GeometryFactory(), false);
        try {
            while (reader.hasNext()) {
                System.out.println(reader.nextRecord().shape());
            }
        } finally {
            reader.close();
        }
    }

    private Geometry getShape(String shpType,Object shpFieldValue){
        String value = (String) shpFieldValue;

        String[] points = value.split(";");
        Coordinate[] coordinates = new Coordinate[points.length];
        for(int i=0;i<points.length;i++){
            coordinates[i]=
                    new Coordinate(Float.parseFloat(points[i].split(",")[0]),Float.parseFloat(points[i].split(",")[1]));
        }
        Geometry geometry = null;
        GeometryFactory geometryFactory = new GeometryFactory();
        if(shpType.equalsIgnoreCase("POINT")){
            geometry = geometryFactory.createPoint(coordinates[0]);
        }else if(shpType.equalsIgnoreCase("POLYLINE")){
            geometry = geometryFactory.createLineString(coordinates);
        }else if(shpType.equalsIgnoreCase("POLYGON")){
            Coordinate firstC = coordinates[0];
            Coordinate lastC = coordinates[coordinates.length-1];
            if(firstC.x == lastC.x && firstC.y == lastC.y){
            }else {//首尾闭合、
                Coordinate[] tempArray = new Coordinate[coordinates.length+1];
                for(int i=0;i<coordinates.length;i++)
                {
                    tempArray[i]=coordinates[i];
                }
                tempArray[coordinates.length]=coordinates[0];
                coordinates = tempArray;
            }
            geometry = geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates),null);
        }
        return geometry;
    }
}
