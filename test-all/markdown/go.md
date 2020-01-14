# go 语言编程

main包为程序入口代码所在包，main方法程序入口。程序结尾不需要分号

编译：go  build xxx.go 运行：xxx.exe

![1559477729390](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1559477729390.png)

break default func interface select case defer go map struct chan else goto package switch const fallthrouigh if range type continue for import return var 

```go
package main

func main(){
   //x :=10;
   //println(x)
   /*for x :=0;x<10; x++{
      println(x)
      if x==3{

      }
      var a int
      println(a)
      fmt.Println(a)
      s :=10
      fmt.Print(s)
   }*/

   /*a,b :=10,9
   fmt.Println(a+b)
   c,d :=10,9
   c,d=d,c
   fmt.Println(c)
   fmt.Println(d)
   var temp int
   temp,_=10,9;
   fmt.Println(temp)*/

   /*var q,x int
   q,x,_=test();
   fmt.Println(q)
   fmt.Println(x)*/
   /*const  a  =10
   fmt.Println(10)
   println("test debug")*/
   /*var (
      a int
      b float64
   )
   a,b=10,20
   fmt.Println(a)
   fmt.Println(b)*/
   /*const(
      a=1
      b=2
   )*/
   /*fmt.Println(a)
   fmt.Println(b)*/
   /**
   迭代器iota 遇到常量 重置
   */
   /*const (
      a=iota
      b=iota
      c=iota
      d=iota
   )
   fmt.Println(a)
   fmt.Println(b)
   fmt.Println(c)*/
   const (
      a1 =iota
      a2
      a3
   )
}

func test()(a,b,c int){
   return 10,9,8;
}
```

![1559580829520](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1559580829520.png)

复数类型

```go
var t complex128
t=1+1i

fmt.Println(t*t)
var s int
fmt.Scanf("%d",&s)
fmt.Println(s)
```

类型别名 type

```go
fmt.Println(s)
type bigint int64
var a bigint
a=11
```

```go
str :="sssss"
for i,data :=range str  {
      fmt.Println(i)
      fmt.Println(data)
}
for i,_ :=range str  {
   fmt.Println(i)
}
```

```go
fmt.Println("ssss")
goto end
fmt.Println(">>>>>>>>>>>>>>>")
end:
   fmt.Println("SSSSS")
```

```go
func Myfunc1(args ... int)()  {
   // 不定参数只能放在形参最后
}
```

func类型使用

匿名函数