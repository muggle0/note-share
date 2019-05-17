//package wigi;
//
//import org.apache.flink.api.common.functions.FoldFunction;
//import org.apache.flink.api.java.functions.KeySelector;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.datastream.KeyedStream;
//import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.windowing.time.Time;
//import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
//import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;
//
//
//public class flinkMain {
//    public static void main(String[] args) throws Exception {
//        StreamExecutionEnvironment see=StreamExecutionEnvironment.getExecutionEnvironment();
////        添加数据源 维基百科pom
//        DataStream<WikipediaEditEvent> editEventDataStream=see.addSource(new WikipediaEditsSource());
//        KeyedStream<WikipediaEditEvent,String> keyedStream=editEventDataStream.keyBy((KeySelector<WikipediaEditEvent,String>) event->{
////            修改事件的作者
//            return event.getUser();
//        });
////        时间窗口 5s
//        DataStream<Tuple2<String, Long>> fold = keyedStream.timeWindow(Time.seconds(5)).fold(new Tuple2<>("", 0), new FoldFunction<WikipediaEditEvent, Tuple2<String,Long>>() {
//            @Override
//            public Tuple2<String,Long> fold(Tuple2<String, Long> tuple2, WikipediaEditEvent o){
//                tuple2.f0 = o.getUser();
//                Long f1 = (Long) tuple2.f1;
//                f1 += o.getByteDiff();
//                return tuple2;
//            }
//        });
////        fold.print();
//        see.execute();
//    }
//}
