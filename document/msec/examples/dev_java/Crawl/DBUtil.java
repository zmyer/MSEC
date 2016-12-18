package crawl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

//���ฺ����msec console�ĺ�̨���ݿ�򽻵�
public class DBUtil {
    //�û���
    private static final String USERNAME = "msec";
    private static final String PASSWORD = "msec@anyhost";
    private static final String DBNAME = "msec_test_java_db";

    private static final String DRIVER = "com.mysql.jdbc.Driver";

    //���ӵ�url���ر�ע�������utf8�ַ�����ָ��
    private String URL;
    private static final String URLFormat = "jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8";
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet resultSet;
    public DBUtil(String host) {
        try{
            URL=String.format(URLFormat, host, DBNAME);
            Class.forName(DRIVER);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //�������ݿ⣬ʧ�ܾͷ���null
    public Connection getConnection(){
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return connection;
    }


    //��ɾ�����ݿ⣬������Ӱ��ļ�¼������
    public int updateByPreparedStatement(String sql, List<Object> params)throws SQLException{
        boolean flag = false;
        int result = -1;
        pstmt = connection.prepareStatement(sql);
        int index = 1;
        if(params != null && !params.isEmpty()){
            for(int i=0; i<params.size(); i++){
                pstmt.setObject(index++, params.get(i));
            }
        }
        result = pstmt.executeUpdate();
        pstmt.close();
        return result;
    }

    //��ѯ���ݿ⣬ֻ����һ����¼���ü�¼���ֶα�����Map�ﷵ�أ��ֶ�����Ϊkey���ֶ�ֵ��Ϊvalue
    public Map<String, Object> findSimpleResult(String sql, List<Object> params) throws SQLException{
        Map<String, Object> map = new HashMap<String, Object>();
        int index  = 1;
        pstmt = connection.prepareStatement(sql);
        if(params != null && !params.isEmpty()){
            for(int i=0; i<params.size(); i++){
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int col_len = metaData.getColumnCount();
        if (resultSet.next()){
            for(int i=0; i<col_len; i++ ){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_value = resultSet.getObject(cols_name);
                if(cols_value == null){
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
        }
        resultSet.close();
        pstmt.close();
        return map;
    }

    //��ѯ���ݿ⣬�����������������м�¼��ÿ����¼����һ��map���ֶ�����Ϊkey���ֶ�ֵ��Ϊvalue
    public ArrayList<Map<String, Object>> findModeResult(String sql, List<Object> params) throws SQLException{
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int index = 1;
        pstmt = connection.prepareStatement(sql);
        if(params != null && !params.isEmpty()){
            for(int i = 0; i<params.size(); i++){
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while(resultSet.next()){
            Map<String, Object> map = new HashMap<String, Object>();
            for(int i=0; i<cols_len; i++){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_value = resultSet.getObject(cols_name);
                if(cols_value == null){
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
            list.add(map);
        }
        resultSet.close();
        pstmt.close();

        return list;
    }

    //��ѯ���ݿ⣬ֻ����һ����¼�� ʹ��java������ƣ�����¼ӳ�䵽java��T
    public <T> T findSimpleRefResult(String sql, List<Object> params,
                                     Class<T> cls )throws Exception{
        T resultObject = null;
        int index = 1;
        pstmt = connection.prepareStatement(sql);
        if(params != null && !params.isEmpty()){
            for(int i = 0; i<params.size(); i++){
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData  = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while(resultSet.next()){
            resultObject = cls.newInstance();
            for(int i = 0; i<cols_len; i++){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_value = resultSet.getObject(cols_name);
                if(cols_value == null){
                    cols_value = "";
                }
                Field field = cls.getDeclaredField(cols_name);
                field.setAccessible(true);
                field.set(resultObject, cols_value);
            }
        }
        resultSet.close();
        pstmt.close();
        return resultObject;

    }

    //��ѯ���ݿ⣬�����������������ļ�¼�� ʹ��java������ƣ�����¼ӳ�䵽java��T
    public <T> ArrayList<T> findMoreRefResult(String sql, List<Object> params,
                                              Class<T> cls )throws Exception {
        ArrayList<T> list = new ArrayList<T>();
        int index = 1;
        pstmt = connection.prepareStatement(sql);
        if(params != null && !params.isEmpty()){
            for(int i = 0; i<params.size(); i++){
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData  = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while(resultSet.next()){
            T resultObject = cls.newInstance();
            for(int i = 0; i<cols_len; i++){
                String cols_name = metaData.getColumnName(i+1);
                Object cols_value = resultSet.getObject(cols_name);

                if(cols_value == null){
                    cols_value = "";
                }
                Field field = cls.getDeclaredField(cols_name);
                field.setAccessible(true); //??javabean????????
                field.set(resultObject, cols_value);
            }
            list.add(resultObject);
        }
        resultSet.close();
        pstmt.close();
        return list;
    }

    //�������������ݿ������
    public void releaseConn(){
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (connection != null) {
                connection.close(); 
            }
        }
        catch (Exception e) {}
    }
}
