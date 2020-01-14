## mybatis复习
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.muggle.mybatis_test.mapper.CourseMapper">
    <resultMap id="course" type="com.muggle.mybatis_test.entity.Course">
        <!--https://www.cnblogs.com/tongxuping/p/7134113.html-->
        <id property="courseNo" column="c_no" jdbcType="BIGINT" javaType="String"/>
        <result property="courceName" column="cname" jdbcType="VARCHAR" javaType="String"/>
        <result property="teacherNo" column="t_no" javaType="String" jdbcType="VARCHAR"/>
        <association property=""><!--关联对象--></association>
        <collection property="" ofType="" fetchType="lazy" >
            <id column="" property=""/>
            <result property="" column="" typeHandler=""/>

        </collection>
    </resultMap>
    <!--parameterType ，入参的全限定类名或类型别名-->
    <insert id="insert" parameterType="java.util.List" useGeneratedKeys="true" keyProperty="courseNo">
        insert into course (cname,t_no)values
        <foreach collection="list" item="item" index="index" separator=",">
            (#{item.courceName},#{item.teacherNo})
        </foreach>


    </insert>
    <!--todo   如何插入Date 类型数据，jdbc 的数据类型 Date（日期） Time（时间） Timestamp（时间戳）数据库时间数据类型 DATE、DATETIME、TIMESTAMP 一般在pojo里date，jdbcType 为TIMESTAMP，数据库为DATETIME-->
    <!-- <insert id="insertA" parameterType="cn.shencom.model.Datetime">
         /*在这里踩了个坑 jdbcType 要大写 插入数据jdbcType=... 没有引号*/
         insert into datetime set update_time=#{date,jdbcType=TIMESTAMP}
     </insert>-->
    <!--statementType：STATEMENT（非预编译），PREPARED（预编译）或CALLABLE中的任意一个-->
    <!--https://www.xuebuyuan.com/2856426.html TypeHandler-->
    <!--不能拿到主键值的原因是回填主键是直接赋值给对象了，并不是返回给你主键值。用来接收的变量只是影响行数 -->
    <insert id="insert2" parameterType="course" useGeneratedKeys="true" keyProperty="courseNo">
        <!-- <selectKey keyProperty="courseNo" resultType="String" order="BEFORE">
             select if(max(c_no) is null , 1 , max(c_no) + 1) as newId from course
         </selectKey>-->


        insert into course (cname,t_no)values (#{courceName},#{teacherNo})

    </insert>
    <select id="select" parameterType="String" resultMap="course">
        select * from course
        <where>

            <if test="id !=null and id!=''">
                AND  c_no=#{id}
            </if>
        </where>
    </select>
    <select id="select2" parameterType="course" resultMap="course">
        select * from course
        <where>
            <if test="courseNo !=null and courseNo!=''">
                AND c_no=#{courseNo}
            </if>
            <if test="courceName !=null and courceName!=''">
                AND cname=#{courceName}
            </if>
        </where>
    </select>
    <select id="select3" parameterType="list" resultMap="course">
        select * from course
        <where>
            <foreach collection="list" open="and id=" close=""
        </where>
    </select>
    <update id="" parameterType="" >
        update  cousre set
        <foreach collection="list" item="ietm" index="" separator="">

        </foreach>
    </update>
</mapper>
```
```
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.url=jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Shanghai
mybatis.mapper-locations=com/muggle/mybatis_test/mapper/*.xml
mybatis.type-aliases-package=com/muggle/mybatis_test/entity

```
