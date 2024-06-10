
---
## 1 Preparation
````
➜ docker exec postgres psql -U user -d studentdb -c \
"CREATE TABLE public.student
(
    id integer NOT NULL,
    address character varying(255),
    email character varying(255),
    name character varying(255),
    CONSTRAINT student_pkey2 PRIMARY KEY (id)
);"
CREATE TABLE
````

````
➜ docker exec postgres psql -U user -d studentdb -c \
"INSERT INTO
    STUDENT(ID, NAME, ADDRESS, EMAIL)
    VALUES('1','Jack','Dallas, TX','jack@gmail.com');"
INSERT 0 1
````

````
➜ docker exec postgres psql -U user -d studentdb -c \
"SELECT * from STUDENT;"
 id |  address   |     email      | name
----+------------+----------------+------
  1 | Dallas, TX | jack@gmail.com | Jack
(1 row)
````

---
## Running the Debezium embedded project

---
## 2 Next Operations
### 2.1 READ
````
curl -s http://localhost:9200/student/_search | jq '.hits.hits'
[
  {
    "_index": "student",
    "_id": "1",
    "_score": 1.0,
    "_source": {
      "_class": "com.tutorial.cdc.elasticsearch.entity.Student",
      "id": 1,
      "name": "Jack",
      "address": "Dallas, TX",
      "email": "jack@gmail.com"
    }
  }
]
````

### 2.2 INSERT/CREATE
````
➜ docker exec postgres psql -U user -d studentdb -c \
"INSERT INTO
    STUDENT(ID, NAME, ADDRESS, EMAIL)
    VALUES('3','Tom','Chicago, IL','tom@gmail.com');"
INSERT 0 1
````

````
➜ docker exec postgres psql -U user -d studentdb -c \
"SELECT * from STUDENT;"
 id |   address   |     email      | name
----+-------------+----------------+------
  1 | Dallas, TX  | jack@gmail.com | Jack
  3 | Chicago, IL | tom@gmail.com  | Tom
(2 rows)
````

````
➜ curl -s http://localhost:9200/student/_search | jq '.hits.hits'
[
  {
    "_index": "student",
    "_id": "1",
    "_score": 1.0,
    "_source": {
      "_class": "com.tutorial.cdc.elasticsearch.entity.Student",
      "id": 1,
      "name": "Jack",
      "address": "Dallas, TX",
      "email": "jack@gmail.com"
    }
  },
  {
    "_index": "student",
    "_id": "3",
    "_score": 1.0,
    "_source": {
      "_class": "com.tutorial.cdc.elasticsearch.entity.Student",
      "id": 3,
      "name": "Tom",
      "address": "Chicago, IL",
      "email": "tom@gmail.com"
    }
  }
]
````

### 2.3 UPDATE
````
➜ docker exec postgres psql -U user -d studentdb -c \
"UPDATE STUDENT 
    SET EMAIL='jill@gmail.com', NAME='Jill' 
    WHERE ID = 1;"
UPDATE 1
````

````
➜ docker exec postgres psql -U user -d studentdb -c \
"SELECT * from STUDENT;"
 id |   address   |     email      | name
----+-------------+----------------+------
  3 | Chicago, IL | tom@gmail.com  | Tom
  1 | Dallas, TX  | jill@gmail.com | Jill
(2 rows)
````

````
➜ curl -s http://localhost:9200/student/_search | jq '.hits.hits'
[
  {
    "_index": "student",
    "_id": "3",
    "_score": 1.0,
    "_source": {
      "_class": "com.tutorial.cdc.elasticsearch.entity.Student",
      "id": 3,
      "name": "Tom",
      "address": "Chicago, IL",
      "email": "tom@gmail.com"
    }
  },
  {
    "_index": "student",
    "_id": "1",
    "_score": 1.0,
    "_source": {
      "_class": "com.tutorial.cdc.elasticsearch.entity.Student",
      "id": 1,
      "name": "Jill",
      "address": "Dallas, TX",
      "email": "jill@gmail.com"
    }
  }
]
````

### 2.4 DELETE
````
➜ docker exec postgres psql -U user -d studentdb -c \
"DELETE FROM STUDENT WHERE ID = 1;"
DAELETE 1
````

````
➜ docker exec postgres psql -U user -d studentdb -c \
"SELECT * from STUDENT;"
 id |   address   |     email     | name
----+-------------+---------------+------
  3 | Chicago, IL | tom@gmail.com | Tom
(1 row)
````

````
➜ curl -s http://localhost:9200/student/_search | jq '.hits.hits'
[
  {
    "_index": "student",
    "_id": "3",
    "_score": 1.0,
    "_source": {
      "_class": "com.tutorial.cdc.elasticsearch.entity.Student",
      "id": 3,
      "name": "Tom",
      "address": "Chicago, IL",
      "email": "tom@gmail.com"
    }
  }
]
````
