import java.lang.annotation.Documented;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbHandler {
    private String dbFile = "";

    public DbHandler(String dbFile) {
        this.dbFile = dbFile;
    }

    private Connection connect()
    {
        Connection conn = null;//定义数据库连接对象
        try {
            String url = "jdbc:sqlite:"+dbFile;   //定义连接数据库的url(url:访问数据库的URL路径),test为数据库名称
            Class.forName("org.sqlite.JDBC");//加载数据库驱动
            conn = DriverManager.getConnection(url);    //获取数据库连接
        }
        //捕获异常信息
        catch (ClassNotFoundException | SQLException e) {
            System.out.println("数据库连接失败！"+e.getMessage());
        }
        return conn;//返回一个连接
    }

    public List getAllTableNames(){
        Connection con = this.connect();
        List tableNames = new ArrayList();
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet rest = dbmd.getTables(null,null,null,new String[]{"TABLE"});
            while (rest.next()){
                String tableName = rest.getString("TABLE_NAME");
                System.out.println(tableName);
                tableNames.add(tableName);
            }
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
            try {
                con.close();
            }catch (SQLException e2){
                e2.printStackTrace();
            }
        }
        return tableNames;
    }

    public List<Map<String,Object>> getAllColumns( String tableName){
        List<Map<String,Object>> columnsInfo = new ArrayList<>();
        Connection conn =  this.connect();
        ResultSet rs = null;
        try{
            DatabaseMetaData dbmd = conn.getMetaData();
            /**
             * 获取可在指定类别中使用的表列的描述。
             * 方法原型:ResultSet getColumns(String catalog,String schemaPattern,String tableNamePattern,String columnNamePattern)
             * catalog - 表所在的类别名称;""表示获取没有类别的列,null表示获取所有类别的列。
             * schema - 表所在的模式名称(oracle中对应于Tablespace);""表示获取没有模式的列,null标识获取所有模式的列; 可包含单字符通配符("_"),或多字符通配符("%");
             * tableNamePattern - 表名称;可包含单字符通配符("_"),或多字符通配符("%");
             * columnNamePattern - 列名称; ""表示获取列名为""的列(当然获取不到);null表示获取所有的列;可包含单字符通配符("_"),或多字符通配符("%");
             */
            rs =dbmd.getColumns(null, null, tableName, null);

            while(rs.next()){
                Map<String,Object> map = new HashMap<>();
                String tableCat = rs.getString("TABLE_CAT");  //表类别（可能为空）
                String tableSchemaName = rs.getString("TABLE_SCHEM");  //表模式（可能为空）,在oracle中获取的是命名空间,其它数据库未知
                String tableName_ = rs.getString("TABLE_NAME");  //表名
                String columnName = rs.getString("COLUMN_NAME");  //列名
                int dataType = rs.getInt("DATA_TYPE");     //对应的java.sql.Types的SQL类型(列类型ID)
                String dataTypeName = rs.getString("TYPE_NAME");  //java.sql.Types类型名称(列类型名称)
                int columnSize = rs.getInt("COLUMN_SIZE");  //列大小
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");  //小数位数
                int numPrecRadix = rs.getInt("NUM_PREC_RADIX");  //基数（通常是10或2） --未知
                /**
                 *  0 (columnNoNulls) - 该列不允许为空
                 *  1 (columnNullable) - 该列允许为空
                 *  2 (columnNullableUnknown) - 不确定该列是否为空
                 */
                int nullAble = rs.getInt("NULLABLE");  //是否允许为null
                String remarks = rs.getString("REMARKS");  //列描述
                String columnDef = rs.getString("COLUMN_DEF");  //默认值
                int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");    // 对于 char 类型，该长度是列中的最大字节数
                int ordinalPosition = rs.getInt("ORDINAL_POSITION");   //表中列的索引（从1开始）
                /**
                 * ISO规则用来确定某一列的是否可为空(等同于NULLABLE的值:[ 0:'YES'; 1:'NO'; 2:''; ])
                 * YES -- 该列可以有空值;
                 * NO -- 该列不能为空;
                 * 空字符串--- 不知道该列是否可为空
                 */
                String isNullAble = rs.getString("IS_NULLABLE");

                map.put("TABLE_CAT",tableCat);
                map.put("TABLE_SCHEM",tableSchemaName);
                map.put("TABLE_NAME",tableName_);
                map.put("COLUMN_NAME",columnName);
                map.put("DATA_TYPE",dataType);
                map.put("TYPE_NAME",dataTypeName);
                map.put("COLUMN_SIZE",columnSize);
                map.put("DECIMAL_DIGITS",decimalDigits);
                map.put("NUM_PREC_RADIX",numPrecRadix);
                map.put("NULLABLE",nullAble);
                map.put("REMARKS",remarks);
                map.put("COLUMN_DEF",columnDef);
                map.put("CHAR_OCTET_LENGTH",charOctetLength);
                map.put("ORDINAL_POSITION",ordinalPosition);
                map.put("IS_NULLABLE",isNullAble);

                /*System.out.println(tableCat + " - " + tableSchemaName + " - " + tableName_ + " - " + columnName +
                        " - " + dataType + " - " + dataTypeName + " - " + columnSize + " - " + decimalDigits + " - "
                        + numPrecRadix + " - " + nullAble + " - " + remarks + " - " + columnDef + " - " + charOctetLength
                        + " - " + ordinalPosition + " - " + isNullAble );*/

                columnsInfo.add(map);
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
            try {
                conn.close();
            }catch (SQLException e2){
                e2.printStackTrace();
            }
        }
        return columnsInfo;
    }

    public List<Map<String,Object>> getAllValues(String tableName){
        List<Map<String,Object>> valuesInfo = new ArrayList<>();
        Connection conn = null;
        try {
            int count = 0;
            conn = this.connect();
            List<Map<String,Object>> columnsInfo = getAllColumns(tableName);

            String sql="Select * from "+tableName;
            Statement stmt = conn.createStatement();//得到Statement实例
            ResultSet rs = stmt.executeQuery(sql);//执行SQL语句返回结果集
            // 当返回的结果集不为空时，并且还有记录时，循环输出记录
            while (rs.next()) {
                //输出获得记录中的"name","sex","age"字段的值
//                System.out.println(rs.getInt(1) + "\t" + rs.getInt(2)+ "\t" +rs.getInt(3));
                Map<String,Object> map = new HashMap();
                for (Map<String,Object> m :columnsInfo){
                    String columnName = (String)m.get("COLUMN_NAME");
                    map.put(columnName,rs.getObject(columnName));
                }
                count ++;
                valuesInfo.add(map);
            }
            System.out.println(count);
            conn.close();
        }
        catch (SQLException e) {
            System.out.println("查询数据时出错！"+e.getMessage());
            try {
                conn.close();
            }catch (SQLException e1){
                e1.printStackTrace();
            }
        }
        return valuesInfo;
    }

    public boolean transformAll(String filepath,String fileName,String shpType,String shpField) {
        boolean flag = false;
        try {
            List<String> tableNames = getAllTableNames();
            for (String tableName :tableNames){
                List<Map<String,Object>> columnsInfo = getAllColumns(tableName);
                List<Map<String,Object>> valuesInfo = getAllValues(tableName);
                ShapeHandler shapeHandler = new ShapeHandler();
                /*String filepath = "F:\\db2shp\\data";
                String fileName = "ppp";
                String shpType = "POLYGON";
                String shpField ="POLYGON";*/
                shapeHandler.write(filepath,fileName,shpType,shpField,columnsInfo,valuesInfo);
            }
            flag = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static void main(String[] args) {
//        DbHandler db = new DbHandler();
//        Connection connect = db.connect();
//        db.getAllTableNames();
//        db.transformAll();
    }
}
