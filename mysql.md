[最全mysql面试题及答案](https://www.cnblogs.com/lijiasnong/p/9963905.html)

# 一、事务隔离级别

## 1.1 事务的基本要素：ACID

A：Atomicity [ˌætəˈmɪsəti]，原子性

C：Consistency，已执行

I：Isolation，隔离性

D：Durability [ˌdjʊərəˈbɪlɪti]，持久性

## 1.2 事务的并发问题

1. 脏读：事务A读取了事务B更新的数据，然后B回滚操作，那么A读取到的数据是脏数据
2. 不可重复读：事务 A 多次读取同一数据，事务 B 在事务A多次读取的过程中，对数据作了更新并提交，导致事务A多次读取同一数据时，结果 不一致。
3. 幻读：系统管理员A将数据库中所有学生的成绩从具体分数改为ABCDE等级，但是系统管理员B就在这个时候插入了一条具体分数的记录，当系统管理员A改结束后发现还有一条记录没有改过来，就好像发生了幻觉一样，这就叫幻读。

> 小结：不可重复读的和幻读很容易混淆，不可重复读侧重于修改，幻读侧重于新增或删除。解决不可重复读的问题只需锁住满足条件的行，解决幻读需要锁表

## 1.3 事务隔离级别

| 事务隔离级别                 | 脏读可能性 | 不可重复读可能性 | 幻读可能性 | 加锁读 |
| ---------------------------- | ---------- | ---------------- | ---------- | ------ |
| 未提交读（read-uncommitted） | 是         | 是               | 是         | 否     |
| 提交读（read-committed）     | 否         | 是               | 是         | 否     |
| 可重复读（repeatable-read）  | 否         | 否               | 是         | 否     |
| 串行化（serializable）       | 否         | 否               | 否         | 是     |

补充：

 　1. 事务隔离级别为读提交时，写数据只会锁住相应的行
 　2. 事务隔离级别为可重复读时，InnoDB 通过 MVCC 解决幻读
 　3. 事务隔离级别为串行化时，读写数据都会锁住整张表
 　4. 隔离级别越高，越能保证数据的完整性和一致性，但是对并发性能的影响也越大

# 二、索引

[MySQL 索引及查询优化总结](https://cloud.tencent.com/developer/article/1004912)

## 2.1 索引类型

### 2.1.1 B-Tree索引

![](https://blog-10039692.file.myqcloud.com/1493034013688_4555_1493034014013.jpg)

技术上是B+Tree，在上图中，如果要查找数据项29，那么首先会把磁盘块1由磁盘加载到内存，此时发生一次IO，在内存中用二分查找确定29在17和35之间，锁定磁盘块1的P2指针，内存时间因为非常短（相比磁盘的IO）可以忽略不计，通过磁盘块1的P2指针的磁盘地址把磁盘块3由磁盘加载到内存，发生第二次IO，29在26和30之间，锁定磁盘块3的P2指针，通过指针加载磁盘块8到内存，发生第三次IO，同时内存中做二分查找找到29，结束查询，总计三次IO。真实的情况是，3层的b+树可以表示上百万的数据，如果上百万的数据查找只需要三次IO，性能提高将是巨大的，如果没有索引，每个数据项都要发生一次IO，那么总共需要百万次的IO，显然成本非常非常高。

**适用于**：

1. 全值匹配
2. 匹配最左前缀
3. 匹配列前缀
4. 匹配范围值
5. 精确匹配某一列并范围匹配另一列
6. 只访问索引的查询

**限制**：

1. 如果不是按照索引的最左列开始查找，则无法使用索引
2. 不能跳过索引中的列
3. 如果查询中有某个列的范围查询，则其右边所有列都无法使用索引优化查找

### 2.1.2 哈希索引

基于哈希表创建的索引

### 2.1.3 空间数据索引

MyISAM支持这个索引

### 2.1.4 全文索引

在相同的列上同时创建全文索引和基于值的B-Tree索引不会有冲突，全文索引适用于MATCH AGAINST操作，而不是普通的WHERE操作。

## 2.2 B-TREE索引类型

### 2.2.1 普通索引
这是最基本的索引类型，而且它没有唯一性之类的限制。普通索引可以通过以下几种方式创建：
（1）创建索引: CREATE INDEX 索引名 ON 表名(列名1，列名2,...);
（2）修改表: ALTER TABLE 表名ADD INDEX 索引名 (列名1，列名2,...);
（3）创建表时指定索引：CREATE TABLE 表名 ( [...], INDEX 索引名 (列名1，列名 2,...) );

### 2.2.2 UNIQUE索引
表示唯一的，不允许重复的索引，如果该字段信息保证不会重复例如身份证号用作索引时，可设置为unique：
（1）创建索引：CREATE UNIQUE INDEX 索引名 ON 表名(列的列表);
（2）修改表：ALTER TABLE 表名ADD UNIQUE 索引名 (列的列表);
（3）创建表时指定索引：CREATE TABLE 表名( [...], UNIQUE 索引名 (列的列表) );

### 2.2.3 主键：PRIMARY KEY索引
主键是一种唯一性索引，但它必须指定为“PRIMARY KEY”。
（1）主键一般在创建表的时候指定：“CREATE TABLE 表名( [...], PRIMARY KEY (列的列表) ); ”。
（2）但是，我们也可以通过修改表的方式加入主键：“ALTER TABLE 表名ADD PRIMARY KEY (列的列表); ”。
每个表只能有一个主键。 （主键相当于聚合索引，是查找最快的索引）
`注：不能用CREATE INDEX语句创建PRIMARY KEY索引`

## 2.3 索引相关

### 2.3.1 聚簇索引

聚簇索引不是一种单独的索引类型，而是一种数据存储放式

**对于InnoDB：**

聚簇索引的每一个叶子节点都包含了主键值、事务ID、用于事务和MVCC的回滚指针以及所有的剩余列

如果有主键，则通过主键聚集索引；如果没有主键，则选择一个唯一非空索引代替；否则会隐式定义一个主键来作为聚簇索引。

二级索引（非聚簇索引）叶子节点存放的是主键的值，主键需要顺序递增，否则插入新行时，会导致频繁的页分裂，增加很多额外的工作

### 2.3.2 覆盖索引

如果一个索引包含（或者说覆盖）所有需要查询的字段的值，就称之为覆盖索引

## 2.4 创建索引原则

### 2.4.1 最左前缀匹配原则

对于多列索引，总是从索引的最前面字段开始，接着往后，中间不能跳过。比如创建了多列索引(name,age,sex)，会先匹配name字段，再匹配age字段，再匹配sex字段的，中间不能跳过。mysql会一直向右匹配直到遇到范围查询(>、<、between、like)就停止匹配。

一般，在创建多列索引时，where子句中使用最频繁的一列放在最左边

### 2.4.2 选择区分度高的列作为索引

比如，我们会选择学号做索引，而不会选择性别来做索引。

### 2.4.3 =和in可以乱序

比如a = 1 and b = 2 and c = 3，建立(a,b,c)索引可以任意顺序，mysql的查询优化器会帮你优化成索引可以识别的形式。

### 2.4.4 索引列不能参与计算，保持列“干净”

比如：Flistid+1>‘2000000608201108010831508721‘。原因很简单，假如索引列参与计算的话，那每次检索时，都会先将索引计算一次，再做比较，显然成本太大。

### 2.4.5 尽量的扩展索引，不要新建索引。

比如表中已经有a的索引，现在要加(a,b)的索引，那么只需要修改原来的索引即可。

## 2.5 EXPLAIN

[MySQL 性能优化神器 Explain 使用分析](https://segmentfault.com/a/1190000008131735)

```mysql
mysql> explain select * from user_info where id = 2\G
*************************** 1. row ***************************
           id: 1
  select_type: SIMPLE
        table: user_info
   partitions: NULL
         type: const
possible_keys: PRIMARY
          key: PRIMARY
      key_len: 8
          ref: const
         rows: 1
     filtered: 100.00
        Extra: NULL
1 row in set, 1 warning (0.00 sec)
```

各列的含义如下:

- id: SELECT 查询的标识符. 每个 SELECT 都会自动分配一个唯一的标识符.
- select_type: SELECT 查询的类型.
- table: 查询的是哪个表
- partitions: 匹配的分区
- type: join 类型
- possible_keys: 此次查询中可能选用的索引
- key: 此次查询中确切使用到的索引.
- ref: 哪个字段或常数与 key 一起被使用
- rows: 显示此查询一共扫描了多少行. 这个是一个估计值.
- filtered: 表示此查询条件所过滤的数据的百分比
- extra: 额外的信息

# 三、性能优化

1. 对查询进行优化，应尽量避免全表扫描，首先应考虑在 where 及 order by涉及的列上建立索引。

2. 应尽量避免在 where 子句中使用 !=或<> 操作符，否则引擎将放弃使用索引而进行全表扫描。

3. 应尽量避免在 where 子句中对字段进行 null 值 判断，否则将导致引擎放弃使用索引而进行全表扫描，如：

   select id from t where num is null

   可以在num上设置默认值0，确保表中num列没有null值，然后这样查询：

   select id from t where num=0

4. 应尽量避免在 where 子句中使用 or 来连接条件，否则将导致引擎放弃使用索引而进行全表扫描，如

   select id from t where num=10 or num=20

   可以这样查询：

   select id from t where num=10

   union all

   select id from t where num=20 

5. 下面的查询也将导致全表扫描：
   select id from t where name like '%abc%'

   对于 like '..%' (不以 % 开头)，可以应用 colunm上的index  

6. in 和 not in 也要慎用，否则会导致全表扫描，如：

   select id from t where num in(1,2,3)

   对于连续的数值，能用 between 就不要用 in 了：

   select id from t where num between 1 and 3

7. 如果在 where 子句中使用参数，也会导致全表扫描。因为SQL只有在运行时才会解析局部变量，但优化程序不能将访问计划的选择推迟到运行时;它必须在编译时进行选择。然而，如果在编译时建立访问计划，变量的值还是未知的，因而无法作为索引选择的输入项。如下面语句将进行全表扫描：

   select id from t where num=@num 

   可以改为强制查询使用索引：select id from t with(index(索引名)) where num=@num

8. 应尽量避免在 where 子句中对字段进行表达式操作，这将导致引擎放弃使用索引而进行全表扫描。如：

   select id from t where num/2=100

   应改为:

   select id from t where num=100*2  

9. 应尽量避免在where子句中对字段进行函数操作，这将导致引擎放弃使用索引而进行全表扫描。如：

   select id from t where substring(name,1,3)='abc'--name以abc开头的id

   select id from t where datediff(day,createdate,'2005-11-30')=0--'2005-11-30'生成的id

   应改为:

   select id from t where name like 'abc%'

   select id from t where createdate>='2005-11-30' and createdate<'2005-12-1'

10. 不要在 where 子句中的“=”【左边】进行函数、算术运算或其他表达式运算，否则系统将可能无法正确使用索引。

11. 在使用索引字段作为条件时，如果该索引是【复合索引】，那么必须使用到该索引中的【第一个字段】作为条件时才能保证系统使用该索引，否则该索引将不会被使用。并且应【尽可能】的让字段顺序与索引顺序相一致。（字段顺序也可以不与索引顺序一致，但是一定要包含【第一个字段】。）

12. 不要写一些没有意义的查询，如需要生成一个空表结构：

    select col1,col2 into #t from t where 1=0

    这类代码不会返回任何结果集，但是会消耗系统资源的，应改成这样：create table #t(...)  

13. 很多时候用 exists 代替 in 是一个好的选择：

    select num from a where num in(select num from b)

    用下面的语句替换：

    select num from a where exists(select 1 from b where num=a.num)

14. 并不是所有索引对查询都有效，SQL是根据表中数据来进行查询优化的，当索引列有大量数据重复时，SQL查询可能不会去利用索引，如一表中有字段sex，male、female几乎各一半，那么即使在sex上建了索引也对查询效率起不了作用。

15. 索引并不是越多越好，索引固然可以提高相应的 select 的效率，但同时也降低了 insert 及 update 的效率，因为 insert 或 update 时有可能会重建索引，所以怎样建索引需要慎重考虑，视具体情况而定。一个表的索引数最好不要超过6个，若太多则应考虑一些不常使用到的列上建的索引是否有必要。

16. 应尽可能的避免更新 clustered 索引数据列，因为 clustered 索引数据列的顺序就是表记录的物理存储顺序，一旦该列值改变将导致整个表记录的顺序的调整，会耗费相当大的资源。若应用系统需要频繁更新 clustered 索引数据列，那么需要考虑是否应将该索引建为 clustered 索引。

17. 尽量使用数字型字段，若只含数值信息的字段尽量不要设计为字符型，这会降低查询和连接的性能，并会增加存储开销。这是因为引擎在处理查询和连接时会逐个比较字符串中每一个字符，而对于数字型而言只需要比较一次就够了

18. 尽可能的使用 varchar/nvarchar 代替 char/nchar ，因为首先变长字段存储空间小，可以节省存储空间，其次对于查询来说，在一个相对较小的字段内搜索效率显然要高些。

19. 任何地方都不要使用 select * from t ，用具体的字段列表代替“*”，不要返回用不到的任何字段。

20. 尽量使用表变量来代替临时表。如果表变量包含大量数据，请注意索引非常有限(只有主键索引)。

21. 避免频繁创建和删除临时表，以减少系统表资源的消耗。

22. 临时表并不是不可使用，适当地使用它们可以使某些例程更有效，例如，当需要重复引用大型表或常用表中的某个数据集时。但是，对于一次性事件，最好使用导出表。

23. 在新建临时表时，如果一次性插入数据量很大，那么可以使用 select into 代替 create table，避免造成大量 log ，以提高速度;如果数据量不大，为了缓和系统表的资源，应先create table，然后insert。

24. 如果使用到了临时表，在存储过程的最后务必将所有的临时表显式删除，先 truncate table ，然后 drop table ，这样可以避免系统表的较长时间锁定。

25. 尽量避免使用游标，因为游标的效率较差，如果游标操作的数据超过1万行，那么就应该考虑改写。

26. 使用基于游标的方法或临时表方法之前，应先寻找基于集的解决方案来解决问题，基于集的方法通常更有效。

27. 与临时表一样，游标并不是不可使用。对小型数据集使用 FAST_FORWARD 游标通常要优于其他逐行处理方法，尤其是在必须引用几个表才能获得所需的数据时。在结果集中包括“合计”的例程通常要比使用游标执行的速度快。如果开发时间允许，基于游标的方法和基于集的方法都可以尝试一下，看哪一种方法的效果更好。

28. 在所有的存储过程和触发器的开始处设置 SET NOCOUNT ON ，在结束时设置 SET NOCOUNT OFF 。无需在执行存储过程和触发器的每个语句后向客户端发送 DONE_IN_PROC 消息。

29. 尽量避免向客户端返回大数据量，若数据量过大，应该考虑相应需求是否合理。

30. 尽量避免大事务操作，提高系统并发能力。

31. select Count (*)和Select Count(1)以及Select Count(column)区别

    一般情况下，Select Count (*)和Select Count(1)两着返回结果是一样的

    假如表沒有主键(Primary key), 那么count(1)比count(*)快，

    如果有主键的話，那主键作为count的条件时候count(主键)最快

    如果你的表只有一个字段的话那count(*)就是最快的

    count(*) 跟 count(1) 的结果一样，都包括对NULL的统计，而count(column) 是不包括NULL的统计

32. [limit优化](https://www.cnblogs.com/shiwenhu/p/5757250.html)

# 四 、复制







# 五、高可用











































