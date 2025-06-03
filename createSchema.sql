CREATE TABLE Cakes (
  cake_id 			CHAR(3) PRIMARY KEY,
  cake_name 		VARCHAR(100),
  price 			INT
);

CREATE INDEX cakes_index ON Cakes(cake_id);

CREATE TABLE Stores(
    store_id 		CHAR(5) PRIMARY KEY not null,
    store_name 		VARCHAR(50) not null, 
    manager_name 	VARCHAR(20),
    store_call 		VARCHAR(15),
    store_hour 		VARCHAR(30), 
    address 		VARCHAR(100),
    store_rate 		NUMERIC(2,1) NULL
);

CREATE INDEX stores_index ON Stores(store_id);

CREATE TABLE Customers (
    customer_id 	INT PRIMARY KEY,
    customer_name 	VARCHAR(100) NOT NULL, 
    address	      VARCHAR(255), 
    phone_number 	VARCHAR(20),
    email 			VARCHAR(100) 
);

CREATE INDEX customers_index on Customers(customer_id);

CREATE TABLE Orders(
    order_num       INT(5) PRIMARY KEY,
    order_date      DATETIME NOT NULL,
    customer_id     INT,
    cake_id	        CHAR(3),
    store_id        CHAR(5),
    price           INT,
    foreign key (customer_id) references Customers(customer_id) on delete set NULL on update CASCADE,
    foreign key (cake_id) references Cakes(cake_id) on delete set NULL on update CASCADE,
    foreign key (store_id) references Stores(store_id) on delete set NULL on update CASCADE
);

CREATE INDEX orders_index on Orders(order_num);

CREATE TABLE Reviews
	(review_num		CHAR(8) PRIMARY KEY, 
	 customer_id	INT,
	 review_rate	NUMERIC(2,1) NULL,
	 cake_id		CHAR(3),
	 order_num		INT (5),
	 store_id		CHAR(5),
	 foreign key (customer_id) references Customers(customer_id) on delete set NULL on update CASCADE,
	 foreign key (cake_id) references Cakes(cake_id) on delete set NULL on update CASCADE,
	 foreign key (order_num) references Orders(order_num) on delete set NULL on update CASCADE,
	 foreign key (store_id) references Stores(store_id) on delete set NULL on update CASCADE
);

CREATE INDEX reviews_index on Reviews(review_num);

-- view (cake rating 계산)
CREATE VIEW cake_rate_cal AS
SELECT c.cake_name AS cake_name, c.price AS price, ROUND(IFNULL(SUM(r.review_rate)/COUNT(r.cake_id),0.0)1) AS cake_rate
FROM Cakes c LEFT JOIN Reviews r ON c.cake_id=r.cake_id
GROUP BY c.cake_id;

-- view (customer_name으로 리뷰 조회: 구매자, 케이크명, 별점 나오도록)
CREATE VIEW review_info AS
SELECT cus.customer_name, r.review_rate, cake.cake_name, s.store_name
FROM Reviews r JOIN Customers cus ON r.customer_id=cus.customer_id JOIN Cakes cake ON r.cake_id=cake.cake_id JOIN Stores s ON r.store_id=s.store_id;