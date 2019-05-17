import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

public class Test {

    public static void main(String[] args) {
        System.out.println(">>>>>>>>test<<<<<<<<");
        //获取环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//读取本地文件-放入到数据集合中
        DataSet<String> text =env.fromElements("sdasd/dds/wsdasz/asdf/sdawww/fas/ ss/ws/rasd/asd/xczx/fas/dsa/sdas/asd/asd/asd/dfdf/asasdas/////");
    }
}
