---
toc: false
comments: false
title: Python 集合笔记
description: Python 集合笔记
tags:
  - Python
id: 60
categories:
  - Python
date: 2019-6-10
---


#### List : 使用中括号包裹,有序，元素可变，可以看作时Java的数组，有多维,可通过中括号+下标进行管理

```python
l = [1, 2, 3]
```

len()  : 求长

pop(i) : 删除指定位置元素，不传则删除最后一个数据,

append() : 添加数据

<!-- more -->

#### Tuple: 元组，括号包裹，有序，元素不可变，也可通过中括号+下标进行管理

```python
t = (1,'x',2)
```

t=(1,) : 定义只有一个元素的元组，逗号时必须

t=()  : 定义一个空的元组



#### Dict: 字典,类似 Java Map ，大括号包裹，冒号连接 Key 和 Value，无序,key 不可变，Value 可变

```python
d = {'Michael': 95, 'Bob': 75, 'Tracy': 85}
```



#### Set: 括号和中括号包裹，只存储 key ,不存储 value, 且 key 不能重复

```python
s = set([1, 2, 3, 3, 4, 4]) 
s.add(5) # 添加只能使用 add() 来添加元素，不能使用 append()
print(s) # {1, 2, 3, 4, 5}
# 注意：我们使用 set([])的形式时，输出的时候是一个会自动过滤重复元素的 dict, 输出类型是 dict

# 区别于以下
s = ([1, 2, 2, 2, 4, 4]) # 这是一个 dict ，而不是 set
s.append(5) # 添加只能使用 append() 
print(s) # [1, 2, 2, 2, 4, 4, 5]
# 注意: 当我们省略掉 set的时候，输出的是里面的 list,可以重复，输出 类型是 list

```



#### 可变和不可变

```python
a = ['a', 'c', 'b'] # list 可变(每个元素的内存地址上的内容可变)
a.sort() # 排序
print(a) # ['a', 'b', 'c']

b = 'abc' # str 不可变（该内存地址上的字符不可变）
b.replace('a', 'A') # 无效改变
c = b.replace('a', 'A') #  有效改变,可以理解为 重新声明了一个新的 str
# c.replace('b', 'B')
print(b)
print(c)
# 注意上面中 a,b,c 这三个都是变量，他们可以重新指向别的任何对象地址
```



#### 包含

```python
l = [1,1,2,2,3]
#print(l)

t = (1,'x',2)
# 可以包含 list
#print(t)


d = {'Michael': 95, 'Bob': 75, 'Tracy': 85}
#print(d)

s = set([1, 2, 3, 3, 4, 4])
s.add(5)
#print(s) # {1, 2, 3, 4, 5}

# 包含
l1 = [l]
l1 = [t]
l1 = [d] # [{'Michael': 95, 'Bob': 75, 'Tracy': 85}]
l1 = [s] # [{1, 2, 3, 4, 5}]
print(l1)

t1 = (l) #[1, 1, 2, 2, 3]
t1 = (t) # (1, 'x', 2)
t1 = (d) # {'Michael': 95, 'Bob': 75, 'Tracy': 85}
t1 = (s) # {1, 2, 3, 4, 5}
print(t1) # l里面是什么类型就输出什么类型

s2 = set([l]) #错误，不能包含 list
s2 = set([t])  #正确，可以包含 tuple，输出类型是 tuple
s3 = set([d]) #错误，不能包含 dict
s2 = set([s]) #错误，不能包含 set
print(s2)
```

